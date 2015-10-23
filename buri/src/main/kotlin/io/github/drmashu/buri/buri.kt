package io.github.drmashu.buri

import io.github.drmashu.dikon.Container
import io.github.drmashu.dikon.Dikon
import io.github.drmashu.dikon.Factory
import io.github.drmashu.dikon.Holder
import org.apache.logging.log4j.LogManager
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.util.log.Log
import org.eclipse.jetty.util.log.Slf4jLog
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
        val logger = LogManager.getLogger(Buri::class.java)
    }

    /**
     * 設定
     */
    abstract val config: Map<String, Factory<*>>

    /**
     * Dikon
     */
    public val dikon: Dikon = Dikon(config)

    /** パスとアクションを結びつけるマップ */
    val pathMap: Map<String, List<Pair<NamedPattern, Factory<*>>>>

    /**
     * 初期化
     */
    init {
        logger.entry()
        var result: MutableMap<String, MutableList<kotlin.Pair<NamedPattern, Factory<*>>>> = HashMap()
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
            val names = ArrayList<String>()
            val patternStr = pattern.pattern()
            val matcher = groupNamePattern.matcher(patternStr)
            while (matcher.find()) {
                names.add(matcher.group(1))
            }
            val value = NamedPattern(pattern, names.toArray(arrayOfNulls<String>(names.size))) to entry.value
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
        logger.exit()
    }

    /**
     * サービス実行.
     * パス/メソッドに応じたアクション/ビューを呼び出す
     */
    public override final fun service(req: HttpServletRequest, res: HttpServletResponse) {
        logger.entry(req, res)
        val list = pathMap[req.method]
        if (list != null) {
            for(item in list) {
                val pattern = item.first.pattern
                val matcher = pattern.matcher(req.pathInfo)
                if (matcher.matches()) {
                    val paramMap: MutableMap<String, Factory<*>> = hashMapOf(
                            "request" to Holder(req),
                            "response" to Holder(res),
                            "context" to Holder(servletContext)
                    )
                    for(name in item.first.names) {
                        paramMap.put(name, Holder(matcher.group(name)))
                    }
                    // デフォルトのコンテントタイプをhtmlにする
                    res.contentType = "text/html"
                    res.characterEncoding = "UTF-8"
                    val factory = item.second
                    callAction(factory, paramMap, req)
                    return
                }
            }
        }

        super.service(req, res)
        logger.exit()
    }

    /**
     *
     */
    fun callAction(factory: Factory<*>, paramMap: MutableMap<String, Factory<*>>, req: HttpServletRequest) {
        logger.entry(factory, paramMap, req)
        val action = factory.get(ParamContainer(dikon, paramMap))
        logger.trace("action $action")
        try {
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
                is Action -> {

                }
                else -> throw InvalidTargetException()
            }
        } finally {
            logger.exit()
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
    companion object {
        val logger = LogManager.getLogger(ParamContainer::class.java)
    }
    override fun get(name: String): Any? {
        logger.entry(name)
        val param = params[name]
        var result :Any? = null
        if (param != null) {
            result = param.get(dikon)
        } else {
            result = dikon[name]
        }
        logger.exit(result)
        return result
    }
}