package io.github.drmashu.buri

import io.github.drmashu.buri.Buri
import io.github.drmashu.buri.HtmlAction
import io.github.drmashu.dikon.Dikon
import io.github.drmashu.dikon.Factory
import io.github.drmashu.dikon.Holder
import io.github.drmashu.dikon.Injection
import org.eclipse.jetty.util.resource.Resource
import java.io.Writer
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.test.assertEquals

import org.junit.Test as test
import org.junit.Before as before
import org.junit.After as after

import org.mockito.Mockito.*
import java.io.PrintWriter
import java.io.StringWriter
import javax.servlet.ServletConfig
import javax.servlet.ServletContext

//import kotlin.reflect.jvm.java

/**
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 */
public class BinderTest {
    @test fun testAction1() {
        val binder = object: Buri() {
            override val config: Map<String, Factory<*>>
                get() = mapOf(
                        Pair("/test", Injection(TestAction::class)),
                        Pair("test", Holder("TEST"))
                )
        }
        val req = mock(HttpServletRequest::class.java)
        `when`(req.pathInfo).thenReturn("/test")
        `when`(req.method).thenReturn("GET")
        val res = mock(HttpServletResponse::class.java)
        val resultWriter = StringWriter()
        `when`(res.writer).thenReturn(PrintWriter(resultWriter))
        binder.service(req, res)
        assertEquals("TEST", resultWriter.buffer.toString())
    }
    @test fun testAction2() {
        val binder = object: Buri() {
            override val config: Map<String, Factory<*>>
                get() = mapOf(
                        Pair("/test/(?<test>[A-Za-z]+)", Injection(TestAction::class))
                )
        }
        val req = mock(HttpServletRequest::class.java)
        `when`(req.pathInfo).thenReturn("/test/TEST")
        `when`(req.method).thenReturn("POST")
        val res = mock(HttpServletResponse::class.java)
        val resultWriter = StringWriter()
        `when`(res.writer).thenReturn(PrintWriter(resultWriter))
        binder.service(req, res)
        assertEquals("TEST", resultWriter.buffer.toString())
    }
    @test fun testAction3() {
        val binder = object: Buri() {
            override val config: Map<String, Factory<*>>
                get() = mapOf(
                        Pair("/test/(?<test>[A-Za-z]+)", Injection(TestAction::class))
                )
            override fun getResource(path: String):Resource? {
                return null
            }
        }
        val req = mock(HttpServletRequest::class.java)
        `when`(req.pathInfo).thenReturn("/test/TEST/Test")
        `when`(req.method).thenReturn("GET")
        val res = mock(HttpServletResponse::class.java)
        val resultWriter = StringWriter()
        `when`(res.writer).thenReturn(PrintWriter(resultWriter))
        binder.service(req, res)
        assertEquals("", resultWriter.buffer.toString())
    }
    @test fun testAction4() {
        val binder = object: Buri() {
            override val config: Map<String, Factory<*>>
                get() = mapOf(
                        Pair("/test/(?<test>[A-Za-z]+):POST", Injection(TestAction::class))
                )

            override fun getResource(path: String):Resource? {
                return null
            }
        }
        val req = mock(HttpServletRequest::class.java)
        `when`(req.pathInfo).thenReturn("/test/TEST")
        `when`(req.method).thenReturn("GET")
        val res = mock(HttpServletResponse::class.java)
        val resultWriter = StringWriter()
        `when`(res.writer).thenReturn(PrintWriter(resultWriter))
        binder.service(req, res)
        assertEquals("", resultWriter.buffer.toString())
    }
    @test fun testAction5() {
        val binder = object: Buri() {
            override val config: Map<String, Factory<*>>
                get() = mapOf(
                        Pair("/test/(?<test>[A-Za-z]+):POST", Injection(TestAction::class))
                )
        }
        val req = mock(HttpServletRequest::class.java)
        `when`(req.pathInfo).thenReturn("/test/TEST")
        `when`(req.method).thenReturn("POST")
        val res = mock(HttpServletResponse::class.java)
        val resultWriter = StringWriter()
        `when`(res.writer).thenReturn(PrintWriter(resultWriter))
        binder.service(req, res)
        assertEquals("TEST", resultWriter.buffer.toString())
    }
    @test fun testAction6() {
        val binder = object: Buri() {
            override val config: Map<String, Factory<*>>
                get() = mapOf(
                        Pair("/test/(?<test>[A-Za-z]+):POST,DELETE", Injection(TestAction::class))
                )
        }
        val req = mock(HttpServletRequest::class.java)
        `when`(req.pathInfo).thenReturn("/test/TEST")
        `when`(req.method).thenReturn("DELETE")
        val res = mock(HttpServletResponse::class.java)
        val resultWriter = StringWriter()
        `when`(res.writer).thenReturn(PrintWriter(resultWriter))
        binder.service(req, res)
        assertEquals("TESTTEST", resultWriter.buffer.toString())
    }
}
class TestAction(request: HttpServletRequest, response: HttpServletResponse, val test: String): HtmlAction(request, response) {
    override fun get() {
       writer.write(test)

    }
    override fun delete() {
        writer.write(test + test)
    }
}
