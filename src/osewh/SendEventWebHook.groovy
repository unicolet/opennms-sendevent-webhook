package osewh

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;

def startJetty() {
    def port=System.getProperty("PORT")?:"9090"
    def jetty = new Server(port as int)

    def context = new ServletContextHandler(jetty, '/', ServletContextHandler.NO_SESSIONS)
    context.resourceBase = './web/'
    context.addServlet(new ServletHolder(new OSEWebHookServlet()), "/*");

    println "Starting Jetty on 0.0.0.0:${port}, press Ctrl+C to stop."
    jetty.start()
}

startJetty()
