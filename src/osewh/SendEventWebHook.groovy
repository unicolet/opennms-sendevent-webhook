package osewh

@Grapes([
    @Grab(group='org.eclipse.jetty', module='jetty-server', version='9.3.0.M2'),
    @Grab(group='org.eclipse.jetty', module='jetty-servlet', version='9.3.0.M2'),
    @Grab(group='org.eclipse.jetty', module='jetty-servlets', version='9.3.0.M2')
    ]
)

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;

def startJetty() {
    def jetty = new Server(9090)

    def context = new ServletContextHandler(jetty, '/', ServletContextHandler.NO_SESSIONS)
    context.resourceBase = './web/'
    context.addServlet(new ServletHolder(new OSEWebHookServlet()), "/*");

    jetty.start()
}

println "Starting Jetty, press Ctrl+C to stop."
startJetty()