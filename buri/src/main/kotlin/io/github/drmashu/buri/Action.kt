package io.github.drmashu.buri

import io.github.drmashu.dikon.Dikon
import io.github.drmashu.dikon.Factory
import io.github.drmashu.dikon.Holder
import java.io.Writer
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * アクションのスーパークラス.
 * アクションはコンストラクタインジェクションをされるので、
 * 継承するクラスでwriter、request、responseを使用したい場合は、
 * コンストラクタ引数の名称を変えてはならない。
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 */
public abstract class Action(val context: ServletContext, val request: HttpServletRequest, val response: HttpServletResponse) {
    var ___buri: Buri? = null
    val writer: Writer
        get() = response.writer
    val __dikon: Dikon
        get() = ___buri!!.dikon
    /**
     * 各言語ごとのエスケープ処理を実装する.
     */
    protected abstract fun encode(str :String) :String
}
