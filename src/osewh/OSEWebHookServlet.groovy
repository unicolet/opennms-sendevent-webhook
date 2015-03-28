package osewh

import groovy.servlet.GroovyServlet

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OSEWebHookServlet extends GroovyServlet {
    Logger logger = LoggerFactory.getLogger(this.class);
    
    @Override
    void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uei=request.getPathInfo().replaceFirst("/","");

        String id=request.getParameter("id");
        String host=request.getParameter("host");
        String ipInterface=request.getParameter("ip")?: request.getRemoteHost();
        String source=request.getParameter("source")?:"OSEWebHookServlet";

        def event=new OnmsEvent(uei, ( id? id as int:0), ipInterface, source)
        event.setHost(host)
        request.getParameterNames().each { p ->
            def name=p.toString()
            if (name!="id" && name!="host" && name!="source" && name!="ip") {
                println("adding event param: "+name)
                event.addParm(name, request.getParameter(name))
            }
        }
        event.addParm("sender-ip",request.getRemoteHost())

        def opennmsHost=( System.getProperty("opennms.host")?:"127.0.0.1" );

        println "Sending event with uei=${uei} to ${opennmsHost}"

        event.setOpennmsHost( opennmsHost ) ;
        def xml=event.toXml()
        println xml
        def result=event.sendEvent(xml);

        response.setHeader("Content-type","application/json")
        response.getOutputStream().print("{ok:${result.ok}, message: '${result.message}'}\n");
    }
}
