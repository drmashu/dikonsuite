package io.github.drmashu.buri

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.MustacheFactory
//import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.drmashu.dikon.Factory
import io.github.drmashu.dikon.Holder
import org.apache.logging.log4j.LogManager
import java.io.*
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Http Action
 */
public open class HttpAction(context: ServletContext, request: HttpServletRequest, response: HttpServletResponse) : Action(context, request, response) {

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
    protected fun responseFromFile(fileName: String) {
        logger.entry(fileName)
        val path = context.getRealPath(fileName)
        logger.trace(path)
        val inStr = BufferedInputStream(FileInputStream(path))
        val outStr = response.outputStream
        val buffer = ByteArray(4096)
        var bytesRead = inStr.read(buffer)
        while (bytesRead != -1)
        {
            outStr.write(buffer, 0, bytesRead)
            bytesRead = inStr.read(buffer)
        }
        outStr.flush()
        inStr.close()
        outStr.close()
        logger.exit()
    }
    protected fun responseFromTemplate(fileName: String, obj: Any) {
        logger.entry(fileName)
        val path = context.getRealPath(fileName)
        logger.trace(path)
        val mf = DefaultMustacheFactory()
        val reader = InputStreamReader(FileInputStream(path), "UTF-8")
        val mustache = mf.compile(reader, path)
        mustache.execute(PrintWriter(OutputStreamWriter(response.outputStream, "UTF-8")), obj).flush()
        logger.exit()
    }
}