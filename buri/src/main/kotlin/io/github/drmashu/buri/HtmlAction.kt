package io.github.drmashu.buri

import org.apache.logging.log4j.LogManager
import java.io.Writer
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * HTML用のレンダラクラス.
 * アクションはコンストラクタインジェクションをされるので、
 * 継承するクラスでwriter、request、responseを使用したい場合は、
 * コンストラクタ引数の名称を変えてはならない。
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 */
public abstract class HtmlAction(context: ServletContext, request: HttpServletRequest, response: HttpServletResponse): HttpAction(context, request, response){

    companion object{
        val logger = LogManager.getLogger(HtmlAction::class.java)
    }
    /**
     * エンコード処理
     */
    protected override fun encode(str :String) :String {
        logger.entry(str)
        var buf = StringBuffer()
        for(ch in str) {
            buf.append(
                    when(ch) {
                        '<' ->  "&lt;"
                        '>' ->  "&gt;"
                        '&' ->  "&amp;"
                        '"' ->  "&quot;"
                        '\'' ->  "&#39;"
                        else ->  ch
                    }
            )
        }
        logger.exit(buf)
        return buf.toString()
    }
}