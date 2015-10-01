package io.github.drmashu.buri

import io.github.drmashu.buri.HtmlAction
import org.junit.Test
import java.io.*
import org.junit.Test as test
import org.junit.Before as before
import org.junit.After as after
import org.mockito.Mockito.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
//import kotlin.reflect.jvm.java
import kotlin.test.assertEquals

/**
 * Created by tnagasaw on 2015/08/27.
 */
public class HtmlActionTest {
    @Test fun testEncode() {
        val req = mock(HttpServletRequest::class.java)
        val res = mock(HttpServletResponse::class.java)
        val resultWriter = StringWriter()
        `when`(res.writer).thenReturn(PrintWriter(resultWriter))

        var action = object : HtmlAction(req, res) {
            override fun get() {
                writer.write(encode("""<tag path="aaa&bbb" ccc='ccc'/>"""))
            }
        }
        action.get()
        assertEquals("&lt;tag path=&quot;aaa&amp;bbb&quot; ccc=&#39;ccc&#39;/&gt;", resultWriter.toString())
    }
}