package osewh

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import groovy.json.JsonSlurper

class OSEServletSpec extends spock.lang.Specification {
  def json = new JsonSlurper()
  
  def cleanup() {
    def tokens=new File("auth_tokens")
    if (tokens.exists()) {
      tokens.delete()
    }
  }
  
  def "prefix should always end with a /"(servlet) {
    expect:
    servlet.prefix.charAt(servlet.prefix.length()-1) == "/"

    where:
    servlet                            | _ 
    new OSEWebHookServlet(null)        | _
    new OSEWebHookServlet("")          | _
    new OSEWebHookServlet("/api/v2/")  | _
    new OSEWebHookServlet("/api/v2")   | _
  }
  
  def "check MockHttpServletRequest behavior"(uri,method) {
    expect:
    def req = new MockHttpServletRequest(method,uri)
    req.setPathInfo(uri)
    req.getPathInfo() == uri
    
    where:
    uri                                  | method
    "/"                                  | "GET"
    "/uei?ip=1.2.3.4"                    | "GET"
    "/uei.test/abc?ip=1.2.3.4"           | "GET"
  }
  
  def "pathInfo must begin with 'uei' and contain at least a /"(uri, method, status, ok) {
    given:
    def response=new MockHttpServletResponse()
    
    expect:
    def req=new MockHttpServletRequest(method,uri)
    req.setPathInfo(uri)
    def servlet=new OSEWebHookServlet(null).service(req, response)
    response.status == status
    json.parseText(response.getContentAsString()).ok == ok // ok is always false because there is no one to receive our event
    
    where:
    uri                                  | method | status | ok
    "/"                                  | "GET"  | 400    | false
    "/uei?ip=1.2.3.4"                    | "GET"  | 400    | false
    "/uei.test/abc?ip=1.2.3.4"           | "GET"  | 200    | false
    "/abcbefg/uei.test/abc?ip=1.2.3.4"   | "GET"  | 200    | false
  }
  
  def "when auth_tokens exists a valid token is required"(uri, method, status, ok) {
    given:
    def response=new MockHttpServletResponse()
    new File("auth_tokens") << "1234567"
    
    expect:
    def req=new MockHttpServletRequest(method,uri)
    req.setPathInfo(uri)
    def servlet=new OSEWebHookServlet(null).service(req, response)
    response.status == status
    json.parseText(response.getContentAsString()).ok == ok // ok is always false because there is no one to receive our event
    
    where:
    uri                                  | method | status | ok
    "/"                                  | "GET"  | 400    | false
    "/uei?ip=1.2.3.4"                    | "GET"  | 400    | false
    "/uei.test/abc?ip=1.2.3.4"           | "GET"  | 403    | false
    "/abcbefg/uei.test/abc?ip=1.2.3.4"   | "GET"  | 403    | false
    "/1234567/uei.test/abc?ip=1.2.3.4"   | "GET"  | 200    | false
  }
  
  def "when a string contains xml it should be detected"(text, xml) {
    expect:
    def servlet=new OSEWebHookServlet(null)
    servlet.isJsonOrXml(text) == xml
    
    where:
    text                                 | xml
    "some text"                          | false
    "<tag></tag>"                        | true
    "<?xml version='1.0'><tag></tag>"    | true
    "some text <tag></tag>"              | false
  }
  
  def "when a string contains json it should be detected"(text, json) {
    expect:
    def servlet=new OSEWebHookServlet(null)
    servlet.isJsonOrXml(text) == json
    
    where:
    text                                             | json
    "some text"                                      | false
    "some text {json: 1, string:'str'}"              | false
    "{a:1, t:'abc'}"                                 | true
  }
  
  def "if the gif param is present and set to 1 return a 1x1 transparent gif"(uri, gif, method, status, ctype) {
    given:
    def response=new MockHttpServletResponse()
    new File("auth_tokens") << "1234567"
    
    expect:
    def req=new MockHttpServletRequest(method,uri)
    req.setPathInfo(uri)
    req.addParameter("gif", gif)
    def servlet=new OSEWebHookServlet(null).service(req, response)
    response.status == status
    response.contentType == ctype
    response.getContentAsByteArray().length > 0
    
    where:
    uri                                        | gif | method | status | ctype 
    "/1234567/uei.test/abc?ip=1.2.3.4"         | "0" | "GET"  | 200    | "application/json"
    "/1234567/uei.test/abc?ip=1.2.3.4&gif=1"   | "1" | "GET"  | 200    | "image/gif"
    "/1234567/uei.test/abc?ip=1.2.3.4&gif=0"   | "0" | "GET"  | 200    | "application/json"
  }
}