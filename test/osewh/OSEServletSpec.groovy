package osewh

import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class OSEServletSpec extends spock.lang.Specification {
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
  
  def "pathInfo must begin with 'uei' and contain at least a /"() {
    when:
    def response=new MockHttpServletResponse()
    def servlet=new OSEWebHookServlet(null).service(new MockHttpServletRequest(), response)
    
    then:
    response.status == 400
  }
}