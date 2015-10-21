package io.github.drmashu.buri.template

import java.io.*
import java.util.*
import kotlin.text.Regex

/**
 * テンプレートプリコンパイラ.
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 */
public class PreCompiler {
    companion object {
        private val blank = """[ \t\r\n]"""
        private val NOT_BLANK = Regex("""[^ \t\r\n]""")

        private val FIRST_LINE = Regex("""@\(([^)]*)\)""")
        private val IF = Regex("$blank*if$blank*")
        private val FOR = Regex("$blank*for$blank*")
        private val ELSE = Regex("$blank*else$blank*")
        private val ELSE_IF = Regex("$blank*else$blank+if$blank*")

        private val at = '@'.toInt()
        private val block_start = '{'.toInt()
        private val block_end = '}'.toInt()
        private val lineCommentMark = '/'.toInt()
        private val cr = '\r'.toInt()
        private val nl = '\n'.toInt()
        private val blockCommentMark = '*'.toInt()
        private val bracket_start = '('.toInt()
        private val bracket_end = ')'.toInt()

        /** ファイル種別ごとのレンダラースーパークラス定義 */
        private val RENDERER_TYPE = mapOf(
            Pair("html", "HtmlAction")
        )
    }
    /**
     * 指定されたファイルをプリコンパイルする。
     * 対象のファイルが".kt.html"で終わっていない場合は、無視する。
     * @param packageName パッケージ名
     * @param srcFile 対象ファイル
     * @param destDir 出力先ディレクトリ
     */
    fun precompile(packageName:String, srcFile: File, destDir: File) {
        val name = srcFile.name
        for (rendererType in RENDERER_TYPE) {
            if (name.endsWith(".kt." + rendererType.key, true)) {
                if (!destDir.exists()) {
                    // 出力先がなければ作る
                    destDir.mkdirs()
                }
                val reader = InputStreamReader(FileInputStream(srcFile))
                val distFile = File(destDir, name.substring(0, name.length() - 5))
                val writer = OutputStreamWriter(FileOutputStream(distFile), "UTF-8")
                val className = name.substring(0, name.length() - 8)
                precompile(reader, writer, packageName, className, rendererType.value)
                writer.flush()
                writer.close()
                reader.close()
            }
        }
    }

    /**
     * プリコンパイル処理
     * @param _reader 入力元バッファ
     * @param writer 出力先バッファ
     * @param packageName パッケージ名
     * @param className 出力クラス名
     * @param typeName スーパークラス名
     */
    fun precompile(_reader: Reader, writer: Writer, packageName:String, className: String, typeName: String) {

        var reader = LineNumberReader(_reader)
        val firstLine :String? = reader.readLine()
        var lineIdx = 1
        var charIdx = 1

        //一行目専用処理
        var param: String?
        // 一行目の先頭が"@"で始まっていたら、このレンダラーのパラメータが指定されるということ
        if (firstLine != null && firstLine.startsWith("@")) {
            // カッコ内をとりだして、レンダラーメソッドのパラメータにする
            val match = FIRST_LINE.match(firstLine)
            param = match?.groups?.get(1)?.value
        } else {
            // 先頭行がパラメータ指定で始まっていないとエラー
            throw BuriLexicalException("$lineIdx,$charIdx : 先頭行はパラメータ指定である必要があります")
        }
        if (param == null) {
            //取り出せなければ、パラメータは空
            param = ""
        } else if (!param.isEmpty()) {
            param = ", " + param
        }

        //先頭のコメント
        writer.write("/** Generate source code by Buri Template PreCompiler at ${Date()} */\n")
        writer.write("package $packageName\n")
        writer.write("import java.util.*\n")
        writer.write("import java.io.Writer\n")
        writer.write("import javax.servlet.http.*\n")
        writer.write("import io.github.drmashu.buri.*\n")

        // クラス名
        writer.write("class $className(request: HttpServletRequest, response: HttpServletResponse$param) : $typeName(request, response) {\n")

        // GETメソッドに実装
        writer.write("\tpublic override fun get() {\n")
        val mode = Stack<Mode>()
        // インサートモード
        val insert = object : Mode(writer) {
            override fun process(char: Int) {
                when(char) {
                    block_end -> {
                        writer.write("}\"))\n")
                        // インサートモードから抜ける
                        mode.pop()
                        // マジックモードから抜ける
                        mode.pop()
                    }
                    else -> writer.write(char)
                }
            }
        }
        // 条件モード
        val conditions = object : Mode(writer) {
            override fun process(char: Int) {
                writer.write(char)
                when(char) {
                    bracket_end -> {
                        // モードから抜ける
                        mode.pop()
                    }
                    bracket_start -> {
                        mode.push(this)
                    }
                }
            }
        }
        // コマンドモード
        val command = object : Mode(writer) {
            var buf: StringBuffer? = null
//            var needBlock = false
            override fun process(char: Int) {
                if (buf == null) {
                    buf = StringBuffer()
                }
                when(char) {
                    at -> {
                        if (buf != null && buf!!.length() > 0) {
                            buf = null
                            // コマンドのパラメータ/ブロック開始の前に @ があるとエラー
                            throw BuriLexicalException("$lineIdx,$charIdx: @ の出現位置が不正です")
                        }
//                        needBlock = false
                        // コマンドモードから抜ける
                        mode.pop()
                        // マジックモードから抜ける
                        mode.pop()
                    }
                    bracket_start -> {
                        val commandName = buf.toString()
                        buf = null
//                        needBlock = true
                        if (IF.matches(commandName)) {
                            writer.write("/* $lineIdx */if")
                        } else if (FOR.matches(commandName)) {
                            writer.write("/* $lineIdx */for")
                        } else {
                            // 提供されているコマンド以外の場合はエラー
                            throw BuriLexicalException("$lineIdx,$charIdx: 不正なコマンドです")
                        }
                        writer.write("(")
                        mode.push(conditions)
                    }
                    block_start -> {
                        if (buf != null && NOT_BLANK.matches(buf!!.toString())) {
                            throw BuriLexicalException("$lineIdx,$charIdx: ブロック開始の前に不正な文字があります")
                        }
                        writer.write(" {\n")
                        buf = null;
                    }
                    else -> {
                        buf?.append(char.toChar())
                    }
                }
            }
        }
        // ブロック終了モード
        val blockEnd = object : Mode(writer) {
            var buf: StringBuffer? = null
            var elif = false
            override fun process(char: Int) {
                if (buf == null) {
                    buf = StringBuffer()
                }
                when(char) {
                    at -> {
                        if (elif) {
                            // else if の後 { の前に @ が出現してはダメ
                            throw BuriLexicalException("$lineIdx,$charIdx: @ の出現位置が不正です")
                        }
                        if (0 != buf?.length()) {
                            // なんらかのコマンドがあるのに@が出現している
                            throw BuriLexicalException("$lineIdx,$charIdx: @ の出現位置が不正です")
                        }
                        writer.write("/* $lineIdx */}\n")
                        // @で終了
                        buf = null
                        elif = false
                        // ブロック終了から抜ける
                        mode.pop()
                        // マジックモードから抜ける
                        mode.pop()
                    }
                    bracket_start -> {
                        val commandName = buf.toString()
                        if (!ELSE_IF.matches(commandName)) {
                            elif = false
                            // else if以外は許さない
                            throw BuriLexicalException("$lineIdx,$charIdx: 条件パラメータの出現位置が不正です")
                        }
                        writer.write("/* $lineIdx */} else if (")
                        mode.push(conditions)
                        buf = null
                        elif = true
                    }
                    block_start -> {
                        try {
                            val commandName = buf.toString()
                            val isElse = ELSE.matches(commandName)
                            if (!elif && !isElse) {
                                // else か else if 以外は許さない
                                throw BuriLexicalException("$lineIdx,$charIdx: 不正なコマンドです")
                            }
                            if (isElse) {
                                writer.write("/* $lineIdx */} else ")
                            }
                            writer.write("{\n")
                            if(reader.read() != at) {
                                // 次は at 以外は許さない
                                throw BuriLexicalException("$lineIdx,$charIdx: 不正な文字があります")
                            }
                            // ブロック終了から抜ける
                            mode.pop()
                            // マジックモードから抜ける
                            mode.pop()
                        } finally {
                            buf = null
                            elif = false
                        }
                    }
                    else -> {
                        buf?.append(char.toChar())
                    }
                }
            }
        }
        // 行コメントモード
        val lineComment = object : Mode(writer) {
            override fun process(char: Int) {
                when(char) {
                    cr , nl -> {
                        if (char == cr) {
                            // CRの次はNLが来るので読み飛ばす
                            reader.read()
                        }
                        // 行コメントから抜ける
                        mode.pop()
                        // マジックモードから抜ける
                        mode.pop()
                        writer.write("/* $lineIdx */writer.write(\"\"\"")
                    }
                }
            }
        }
        // ブロックコメントモード
        val blockComment = object : Mode(writer) {
            var commentEnd = false
            override fun process(char: Int) {
                when(char) {
                    blockCommentMark -> commentEnd = true
                    at -> {
                        if (commentEnd) {
                            // ブロックコメントから抜ける
                            mode.pop()
                            // マジックモードから抜ける
                            mode.pop()
                        }
                    }
                    else -> commentEnd = false
                }
            }
        }
        // マジックモード
        val magic = object : Mode(writer) {
            override fun process(char: Int) {
                when(char) {
                    block_start -> {
                        // インサート
                        mode.push(insert)
                        writer.write("/* $lineIdx */writer.write(encode(\"\${")
                    }
                    at -> {
                        // エスケープ
                        writer.write("/* $lineIdx */writer.write(\"@\")\n")
                        mode.pop()
                    }
                    lineCommentMark -> {
                        // 行コメント
                        mode.push(lineComment)
                    }
                    blockCommentMark -> {
                        // ブロックコメント
                        mode.push(blockComment)
                    }
                    block_end -> {
                        // ブロック終了
                        mode.push(blockEnd)
                    }
                    else -> {
                        // ID
                        mode.push(command)
                        command.process(char)
                    }
                }
            }
        }
        // ノーマルモード
        val normal = object : Mode(writer) {
            var inMode = false
            override fun process(char: Int) {
                when(char) {
                    at -> {
                        writer.write("\"\"\")\n")
                        mode.push(magic)
                        inMode = true
                    }
                    else -> {
                        if (inMode) {
                            writer.write("/* $lineIdx */writer.write(\"\"\"")
                            inMode = false
                        }
                        writer.write(char)
                    }
                }
            }
        }
        mode.push(normal)

        writer.write("/* $lineIdx */writer.write(\"\"\"")
        while(reader.ready()) {
            var char = reader.read()
            if (char == -1) {
                break;
            }
            charIdx++
            if (char == cr) {
                char = reader.read()
            }
            if (char == nl) {
                lineIdx++
                charIdx = 1
            }
            mode.peek().process(char)
        }
        writer.write("\"\"\")\n")
        writer.write("\t}\n")
        writer.write("}\n")
    }

    abstract class Mode(val writer: Writer) {
        abstract fun process(char: Int);
    }
}
