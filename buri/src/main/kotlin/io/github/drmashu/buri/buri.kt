package io.github.drmashu.buri

import io.github.drmashu.dikon.Container
import io.github.drmashu.dikon.Dikon
import io.github.drmashu.dikon.Factory
import io.github.drmashu.dikon.Holder
import org.eclipse.jetty.servlet.DefaultServlet
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.regex.Pattern

/**
 * Buri バインダークラス.
 * URIとViewModelを結びつけるために定義を解釈し、リクエストに応じてViewModelのアクションを呼ぶ。
 * これを継承し、バインディング設定を行ったサブクラスを作成して使用する。
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 */
public abstract class Buri() : DefaultServlet() {
    companion object {
        val groupNamePattern = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]+)>")
    }

    /**
     * 設定
     */
    abstract val config: Map<String, Factory<*>>

    /**
     * Dikon
     */
    val dikon: Dikon = Dikon(config)

    /** パスとアクションを結びつけるマップ */
    val pathMap: Map<String, List<Pair<NamedPattern, Factory<*>>>>

    /**
     * 初期化
     */
    init {
        var result: MutableMap<String, MutableList<Pair<NamedPattern, Factory<*>>>> = HashMap()
        // "/"で始まるキーはビューまたはアクションとして扱う
        for (entry in dikon.objectMap) {
            if (!entry.key.startsWith("/")) continue
            var key = entry.key
            val methodIdx = key.lastIndexOf(":")
            val methods = if (methodIdx > 0) {
                val method = key.substring(methodIdx+1)
                key = key.substring(0, methodIdx)
                method.split(delimiters = ",")
            } else {
                listOf("GET", "POST")
            }
            val pattern = Pattern.compile(key)
            val names: MutableList<String> = arrayListOf()
            val patternStr = pattern.pattern()
            val matcher = groupNamePattern.matcher(patternStr)
            while (matcher.find()) {
                names.add(matcher.group(1))
            }
            val value = Pair(NamedPattern(pattern, names.toTypedArray()), entry.value)
            for (method in methods) {
                var list = result.get(method)
                if (list == null) {
                    list = ArrayList()
                    result.put(method, list)
                }
                list.add(value)
            }
        }
        pathMap = result
    }

    /**
     * サービス実行.
     * パス/メソッドに応じたアクション/ビューを呼び出す
     */
    public override final fun service(req: HttpServletRequest, res: HttpServletResponse) {
        val list = pathMap[req.method]
        if (list != null) {
            for(item in list) {
                val pattern = item.first.pattern
                val matcher = pattern.matcher(req.pathInfo)
                if (matcher.matches()) {
                    val paramMap: MutableMap<String, Factory<*>> = hashMapOf(
                            Pair("request", Holder(req)),
                            Pair("response", Holder(res))
                    )
                    for(name in item.first.names) {
                        paramMap.put(name, Holder(matcher.group(name)))
                    }
                    // デフォルトのコンテントタイプをhtmlにする
                    res.contentType = "text/html"
                    val factory = item.second
                    callAction(factory, paramMap, req)
                    return
                }
            }
        }

        super.service(req, res)
    }

    /**
     *
     */
    fun callAction(factory: Factory<*>, paramMap: MutableMap<String, Factory<*>>, req: HttpServletRequest) {
        val action = factory.get(ParamContainer(dikon, paramMap))
        when (action) {
            is HttpAction -> {
                action.___buri = this
                when (req.method) {
                    "GET" -> action.get()
                    "POST" -> action.post()
                    "PUT" -> action.put()
                    "DELETE" -> action.delete()
                    else -> action.get()
                }
            }
            is Action ->{

            }
            else -> throw InvalidTargetException()
        }
    }
}

/**
 *
 */
public class InvalidTargetException : Exception()

/**
 * 名前付きグループの名前をパターンと一緒に保持する
 */
public class NamedPattern(val pattern: Pattern, val names: Array<String>)

/**
 * パスのパラメータと、Dikonの両方から値を取得するコンテナ
 */
public class ParamContainer(val dikon: Dikon, val params: Map<String, Factory<*>>): Container {
    override fun get(name: String): Any? {
        val result = params[name]
        if (result != null) {
            return result.get(dikon)
        }
        return dikon[name]
    }
}