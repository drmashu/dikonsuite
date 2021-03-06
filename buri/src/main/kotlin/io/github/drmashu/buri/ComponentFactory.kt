package io.github.drmashu.buri

import io.github.drmashu.dikon.Container
import io.github.drmashu.dikon.Factory
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *
 */
public class StaticFileHolder(val fileName:String):Factory<HtmlAction> {
    override fun get(dikon: Container): HtmlAction? {
        return object:HtmlAction(dikon.get("context") as ServletContext, dikon.get("request") as HttpServletRequest, dikon.get("response") as HttpServletResponse) {
            override fun get() {
                responseFromFile(fileName)
            }
        }
    }
}

/**
 *
 */
public class TemplateHolder(val fileName:String, val objNames: Array<String>):Factory<HtmlAction> {
    override fun get(dikon: Container): HtmlAction? {
        return object:HtmlAction(dikon.get("context") as ServletContext, dikon.get("request") as HttpServletRequest, dikon.get("response") as HttpServletResponse) {
            override fun get() {
                responseFromTemplate(fileName, mapOf(*Array<Pair<String,Any>>(objNames.size, { idx -> (objNames[idx] to dikon.get(objNames[idx])!!) })))
            }
        }
    }
}
