package io.github.drmashu.buri

import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.drmashu.dikon.Factory
import io.github.drmashu.dikon.Holder
import org.apache.logging.log4j.LogManager
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Http Action
 */
public open class HttpAction(request: HttpServletRequest, response: HttpServletResponse) : Action(request, response) {

    companion object{
        val logger = LogManager.getLogger(HttpAction::class.java)
        val objectMapper = ObjectMapper().registerModule(KotlinModule())
    }
    override fun encode(str: String): String = str

    /**
     * GET Methodに該当する処理.
     */
    public open fun get() {}
    /**
     * POST Methodに該当する処理.
     */
    public open fun post() { get() }
    /**
     * PUT Methodに該当する処理.
     */
    public open fun put() { get() }
    /**
     * DELETE Methodに該当する処理.
     */
    public open fun delete() { get() }
    /**
     * リダイレクト
     */
    protected fun redirect(name: String, vararg args: Pair<String, Any>) {
        logger.entry(name, args)
        val factory = __dikon.objectMap.get(name)
        val paramMap: MutableMap<String, Factory<*>> = hashMapOf(
                "request" to Holder(request),
                "response" to Holder(response)
        )
        for (arg in args) {
            var value =
                    if (arg.second is Factory<*>) {
                        arg.second as Factory<*>
                    } else {
                        Holder(arg.second)
                    }
            paramMap.put(arg.first, value)
        }
        ___buri!!.callAction(factory!!, paramMap, request)
        logger.exit()
    }
    protected fun responseByJson(result: Any) {
        val resultString = objectMapper.writeValueAsString(result)
        response.contentType = "application/json"
        response.writer.print(resultString)
    }
}