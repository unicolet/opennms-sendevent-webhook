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
        def uei=request.getPathInfo().replaceFirst("/","");
        String token=""
        
        if(uei=="" || uei.indexOf("/")==-1) {
            response.setStatus(400) 
            response.getOutputStream().print("Please supply a uei and params");
            return;
        }
        
        if(!uei.startsWith("uei")) {
            uei=uei.split("/",2)
            token=uei[0]
            uei=uei[1]
        }
        
        if(isAuthorized(token)) {
            String id=request.getParameter("id");
            String host=request.getParameter("host");
            String ipInterface=request.getParameter("ip")?: request.getRemoteHost();
            String source=request.getParameter("source")?:"OSEWebHookServlet";
                def event=new OnmsEvent(uei, ( id? id as int:0), ipInterface, source)
            event.setHost(host)
            request.getParameterNames().each { p ->
                def name=p.toString()
                if (name!="id" && name!="host" && name!="source" && name!="ip") {
                    logger.debug("adding event param: "+name)
                    event.addParm(name, request.getParameter(name))
                }
            }
            event.addParm("sender-ip",request.getRemoteHost())
    
            def opennmsHost=( System.getProperty("opennms.host")?:"127.0.0.1" );
    
            logger.info("Sending event with uei=${uei} to ${opennmsHost}")
    
            event.setOpennmsHost( opennmsHost ) ;
            def xml=event.toXml()
            logger.trace(xml)
            def result=event.sendEvent(xml);
            
            if(result.ok) {
                logger.info("event posted successfully")
            } else {
                logger.error("event post failed:"+result.message)
            }
    
            response.setHeader("Content-type","application/json")
            response.getOutputStream().print("{ok:${result.ok}, message: '${result.message}'}\n");
        } else {
            response.setStatus(403) //unauthorized
        }
    }
    
    private boolean isAuthorized(String token) {
        def authorizedTokens=new File("auth_tokens")
        if(authorizedTokens.exists()) {
            return token && authorizedTokens.text.indexOf(token)!=-1
        }
        return true
    }
}
