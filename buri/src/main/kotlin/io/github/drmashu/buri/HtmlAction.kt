package io.github.drmashu.buri

import java.io.Writer
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * HTML用のレンダラクラス.
 * アクションはコンストラクタインジェクションをされるので、
 * 継承するクラスでwriter、request、responseを使用したい場合は、
 * コンストラクタ引数の名称を変えてはならない。
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 */
public abstract class HtmlAction(request: HttpServletRequest, response: HttpServletResponse): HttpAction(request, response){

    /**
     * エンコード処理
     */
    protected override fun encode(str :String) :String {
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
        return buf.toString()
    }
}