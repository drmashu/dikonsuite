package io.github.drmashu.buri

import io.github.drmashu.dikon.Dikon
import io.github.drmashu.dikon.Factory
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory
import org.eclipse.jetty.server.HttpConfiguration
import org.eclipse.jetty.server.HttpConnectionFactory
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.server.handler.ResourceHandler
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.resource.Resource

/**
 * Buri ランナークラス.
 * @author NAGASAWA Takahiro<drmashu@gmail.com>
 * @param buri buri本体(を設定までしたサブクラス)
 */
public class BuriRunner(val buri: Buri) {

    /**
     * サーバ起動
     * @param portNo ポート番号
     */
    public fun start(portNo: Int) {
        val server = Server()

        var httpConf = HttpConfiguration();

        var http1 = HttpConnectionFactory(httpConf);
        var http2c = HTTP2CServerConnectionFactory(httpConf);

        val connector = ServerConnector(server)
        connector.port = portNo
        connector.addConnectionFactory(http1)
        connector.addConnectionFactory(http2c)
        server.connectors = arrayOf(connector)

        val servletContextHandler = ServletContextHandler()
        servletContextHandler.contextPath = "/"
        servletContextHandler.resourceBase = "./"

        val holder = ServletHolder(buri)
        servletContextHandler.addServlet(holder, "/*")

        server.handler = servletContextHandler

        server.isDumpBeforeStop = true
        server.start()
        server.join()
    }
}
