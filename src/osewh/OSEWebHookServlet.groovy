package osewh

import groovy.servlet.GroovyServlet

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.regex.Pattern

class OSEWebHookServlet extends GroovyServlet {
    Logger logger = LoggerFactory.getLogger(this.class)
    private String prefix="/api/v1/"
    
    public OSEWebHookServlet(String p) {
        super()
        if(!p) {
            prefix="/"
        } else {
            prefix=p
            if(!prefix.endsWith("/")) { //must end with a slash
                prefix=prefix+"/"
            }
        }
    }
    
    boolean isJsonOrXml(String txt) {
        boolean result=false
        
        if(txt) {
           result=txt.startsWith("<")
           
           if(!result) {
               result=txt.startsWith("{")
           }
            
        }
        
        return result
    }
    
    String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (
            null != ip && !"".equals(ip.trim())
            && !"unknown".equalsIgnoreCase(ip)) {
            return ip;
        }
        ip = request.getHeader("X-Forwarded-For");
        if (null != ip && !"".equals(ip.trim())
            && !"unknown".equalsIgnoreCase(ip)) {
            // get first ip from proxy ip
            int index = ip.indexOf(',');
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }
   
    @Override
    void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        def uei=parseUEI(request)
        String token=""
        boolean isPost="POST".equalsIgnoreCase(request.getMethod())
        
        if(uei=="" || uei.indexOf("/")==-1) {
            response.setStatus(400) 
            response.getOutputStream().print("{\"ok\":false, \"message\": \"Please supply UEI and params\"}")
            return
        }
        
        if(!uei.startsWith("uei")) {
            uei=uei.split("/",2)
            token=uei[0]
            uei=uei[1]
        }
        
        if(isAuthorized(token)) {
            String id=request.getParameter("id")
            String host=request.getParameter("host")
            String ipInterface=request.getParameter("ip")?: request.getRemoteAddr()
            String source=request.getParameter("source")?:"OSEWebHookServlet"
            String gif=request.getParameter("gif")
            def event=new OnmsEvent(uei, ( id? id as int:0), ipInterface, source)
            event.setHost(host)
            request.getParameterNames().each { p ->
                def name=p.toString()
                if (name!="id" && name!="host" && name!="source" && name!="ip") {
                    if(isPost && isJsonOrXml(name)) {
                        logger.debug("adding event param BODY: "+name)
                        event.addParm('BODY', name)
                    } else {
                        logger.debug("adding event param: "+name)
                        event.addParm(name, request.getParameter(name))
                    }
                }
            }
            event.addParm("sender-ip",request.getRemoteAddr())
    
            def opennmsHost=( System.getProperty("opennms.host")?:"127.0.0.1" )
    
            logger.info("Sending event with uei=${uei} to ${opennmsHost}")
    
            event.setOpennmsHost( opennmsHost ) 
            def xml=event.toXml()
            logger.trace(xml)
            def result=event.sendEvent(xml)
            if(logger.isDebugEnabled()) {
                logger.debug(xml)
            }
            
            if(result.ok) {
                logger.info("event posted successfully")
            } else {
                logger.error("event post failed:"+result.message)
            }
   
            if("1".equals(gif)) {
                response.setHeader("Content-type","image/gif")
                response.getOutputStream().write(Gif.getImageBytes())
            } else {
                response.setHeader("Content-type","application/json")
                response.getOutputStream().print("{\"ok\":${result.ok}, \"message\": \"${result.message}\"}")
            }
        } else {
            logger.warn("unauthorized request from ip="+request.getRemoteAddr()+" request="+request.getRequestURL())
            response.setStatus(403) //unauthorized
            response.getOutputStream().print("{\"ok\":false, \"message\": \"Unauthorized\"}")
        }
    }
    
    private String parseUEI(HttpServletRequest request) {
        def uei=request.getPathInfo()?.replaceFirst(prefix,"") ?: "" //strip the prefix and be done with it
        if(uei.startsWith("/")) {
            // replace first slash
            uei=uei.replaceFirst("/","")
        }
        return uei
    }
    
    private boolean isAuthorized(String token) {
        def authorizedTokens=new File("auth_tokens")
        if(authorizedTokens.exists()) {
            token=token.replaceAll("([\\.\\*\\]\\[\\(\\)])") { "" } // escape regex
            return token && Pattern.compile("(?m)^${token}\$").matcher(authorizedTokens.text).find()
        }
        return true
    }
    
    public String getPrefix() {
        return prefix;
    }
}
