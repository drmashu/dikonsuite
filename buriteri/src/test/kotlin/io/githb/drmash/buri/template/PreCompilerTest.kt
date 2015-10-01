package io.githb.drmash.buri.template

import io.github.drmashu.buri.template.PreCompiler
import org.junit.Test
import java.io.BufferedReader
import java.io.LineNumberReader
import java.io.StringReader
import java.io.StringWriter
import java.util.*
import org.junit.Test as test
import org.junit.Before as before
import org.junit.After as after

import kotlin.test.assertEquals

/**
 * Created by tnagasaw on 2015/08/13.
 */
public class PreCompilerTest {
    /**
     *
     */
    @Test fun testPreCompileEmpty() {
        val compiler = PreCompiler()
        val reader = StringReader("""@()
""")
        val writer = StringWriter(1024)
        compiler.precompile(reader, writer, "test", "test", "HtmlAction")
        assertEqualDocuments("/** Generate source code by Buri Template PreCompiler at ${Date()} */\n"
                + "package test\n"
                + "import java.util.*\n"
                + "import java.io.Writer\n"
                + "import javax.servlet.http.*\n"
                + "import io.github.drmashu.buri.*\n"
                + "class test(request: HttpServletRequest, response: HttpServletResponse) : HtmlAction(request, response) {\n"
                + "\tpublic override fun get() {\n"
                + "/* 1 */writer.write(\"\"\"\"\"\")\n"
                + "\t}\n"
                + "}\n"
                , writer.toString())
    }
    fun assertEqualDocuments(doc1: String, doc2: String, message:String = "") {
        println(doc2)
        val reader1 = BufferedReader(StringReader(doc1))
        val reader2 = BufferedReader(StringReader(doc2))
        var line1 = reader1.readLine()
        var line2 = reader2.readLine()
        while(line1 != null || line2 != null) {
            assertEquals(line1, line2, message)
            line1 = reader1.readLine()
            line2 = reader2.readLine()
        }
    }
    @Test fun testPreCompile() {
        val compiler = PreCompiler()
        val reader = StringReader("""@(val list: List<String>)
<!DOCTYPE html>
<!-- -->
<html>
<head></head>
<body>
drmashu@@gmail.com
<br>
<ol>
@for(idx in 0..10) {@
    @if(idx % 3 == 0) {@
        <li> san!
    @} else if(idx % 2 == 0) {@
        <li> even
    @} else {@
        <li> odd
    @}@
@}@
@/comment
@for(text in list){@
    <li>@{text}
@}@
</ol>
<input name="aa" value="@{this.toString()}"/>
@*
ブロックコメント
@{text}
*@
</body>
</html>
""")
        val writer = StringWriter(1024)
        compiler.precompile(reader, writer, "test", "test", "HtmlAction")
        assertEqualDocuments("/** Generate source code by Buri Template PreCompiler at ${Date()} */\n"
                + "package test\n"
                + "import java.util.*\n"
                + "import java.io.Writer\n"
                + "import javax.servlet.http.*\n"
                + "import io.github.drmashu.buri.*\n"
                + "class test(request: HttpServletRequest, response: HttpServletResponse, val list: List<String>) : HtmlAction(request, response) {\n"
                + "\tpublic override fun get() {\n"
                + "/* 1 */writer.write(\"\"\"<!DOCTYPE html>\n"
                + "<!-- -->\n"
                + "<html>\n"
                + "<head></head>\n"
                + "<body>\n"
                + "drmashu\"\"\")\n"
                + "/* 6 */writer.write(\"@\")\n"
                + "/* 6 */writer.write(\"\"\"gmail.com\n"
                + "<br>\n"
                + "<ol>\n"
                + "\"\"\")\n"
                + "/* 9 */for(idx in 0..10) {\n"
                + "/* 10 */writer.write(\"\"\"\n"
                + "    \"\"\")\n"
                + "/* 10 */if(idx % 3 == 0) {\n"
                + "/* 11 */writer.write(\"\"\"\n"
                + "        <li> san!\n"
                + "    \"\"\")\n"
                + "/* 12 */} else if (idx % 2 == 0){\n"
                + "/* 13 */writer.write(\"\"\"\n"
                + "        <li> even\n"
                + "    \"\"\")\n"
                + "/* 14 */} else {\n"
                + "/* 15 */writer.write(\"\"\"\n"
                + "        <li> odd\n"
                + "    \"\"\")\n"
                + "/* 16 */}\n"
                + "/* 17 */writer.write(\"\"\"\n"
                + "\"\"\")\n"
                + "/* 17 */}\n"
                + "/* 18 */writer.write(\"\"\"\n"
                + "\"\"\")\n"
                + "/* 19 */writer.write(\"\"\"\"\"\")\n"
                + "/* 19 */for(text in list) {\n"
                + "/* 20 */writer.write(\"\"\"\n"
                + "    <li>\"\"\")\n"
                + "/* 20 */writer.write(encode(\"\${text}\"))\n"
                + "/* 21 */writer.write(\"\"\"\n"
                + "\"\"\")\n"
                + "/* 21 */}\n"
                + "/* 22 */writer.write(\"\"\"\n"
                + "</ol>\n"
                + "<input name=\"aa\" value=\"\"\"\")\n"
                + "/* 23 */writer.write(encode(\"\${this.toString()}\"))\n"
                + "/* 23 */writer.write(\"\"\"\"/>\n"
                + "\"\"\")\n"
                + "/* 28 */writer.write(\"\"\"\n"
                + "</body>\n"
                + "</html>\n"
                + "\"\"\")\n"
                + "\t}\n"
                + "}\n"
                , writer.toString())

        //org.jetbrains.kotlin.cli.jvm.K2JVMCompiler.main()
    }
}