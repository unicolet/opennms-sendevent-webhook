package osewh

import java.text.DateFormat
/**
 * Modified from: http://www.opennms.org/wiki/Send_event_using_java
 * 
 * @author apaxson
 */
public class OnmsEvent {
    private String opennmsHost;
    private String ipInterface;
    private String uei;
    private String source;
    private int nodeid=0;
    private String time;
    private String host;
    private Map parms = new HashMap();
    public OnmsEvent(String uei, int nodeid, String ipInterface, String source) {
        setUei(uei);
        setNodeid(nodeid);
        setIpInterface(ipInterface);
        setSource(source);
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat dformat = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
        dformat.setTimeZone(TimeZone.getTimeZone("GMT"));
        setTime(dformat.format(currentTime));
        opennmsHost="127.0.0.1"; // sane default
    }
    protected String getUei() {
        return uei;
    }
    protected void setUei(String uei) {
        this.uei = uei;
    }
    protected String getSource() {
        return source;
    }
    protected void setSource(String source) {
        this.source = source;
    }
    protected int getNodeid() {
        return nodeid;
    }
    protected void setNodeid(int nodeid) {
        this.nodeid = nodeid;
    }
    protected String getTime() {
        return time;
    }
    protected void setTime(String time) {
        this.time = time;
    }
    protected String getHost() {
        return host;
    }
    protected void setHost(String host) {
        this.host = host;
    }
    public void addParm(String key, String value) {
        parms.put(key, value);
    }

    String getIpInterface() {
//        if(!ipInterface) {
//
//        }
        return ipInterface
    }

    void setIpInterface(String ipInterface) {
        this.ipInterface = ipInterface
    }

    String getOpennmsHost() {
        return opennmsHost
    }

    void setOpennmsHost(String opennmsHost) {
        this.opennmsHost = opennmsHost
    }

    @SuppressWarnings(value = "unchecked")
    protected Map<String, String> getParms() {
        return parms;
    }
    protected String toXml() {
        StringBuffer data = new StringBuffer();
        data.append("<log>");
        data.append("<events>");
        data.append("<event>");
        data.append("<uei>" + getUei() + "</uei>");
        if(getSource())
            data.append("<source>" + getSource() + "</source>");
        if(getNodeid()>0)
            data.append("<nodeid>" + getNodeid() + "</nodeid>");
        data.append("<time>" + getTime() + "</time>");
        if(getHost())
            data.append("<host>" + getHost() + "</host>");
        data.append("<interface>"+getIpInterface()+"</interface>");
        data.append("<parms>");
        // Cycle through each parameter
        for (Map.Entry<String, String> e : getParms().entrySet()) {
            data.append("<parm>");
            data.append("<parmName>");
            data.append("<![CDATA[" + e.getKey() + "]]></parmName>");
            data.append("<value type=\"string\" encoding=\"text\"><![CDATA[" + e.getValue() + "]]></value>");
            data.append("</parm>");
        }
        data.append("</parms>");
        data.append("</event>");
        data.append("</events>");
        data.append("</log>");
        return data.toString();
    }

    public Map sendEvent(String xmlData) {
        def result=[ok:false,message:""];

        if(!xmlData) {
            System.out.println("null data, not sending anything");
        } else {
            Socket socket = null;
            try {
                socket = new Socket(opennmsHost, 5817);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(xmlData);
                result.ok=true
                result.message="Event sent successfully"
            } catch (UnknownHostException ex) {
                result.message="Unknown host"
            } catch (IOException ex) {
                result.message="I/O Exception while creating socket"
            } finally {
                try {
                    if(socket!=null) {
                        socket.close();
                    }
                } catch (IOException ex) {
                    result.message="I/O Exception when closing socket"
                }
            }
        }

        return result
    }
}
