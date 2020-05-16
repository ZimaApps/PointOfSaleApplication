/**
  * Copyright 2018 bejson.com 
  */
package com.standard.app.paycorpHttp;

/**
 * Auto-generated: 2018-06-15 16:48:9
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class JsonRootBean {

    private String version;

    public JsonRootBean(String version, String msgId, String requestDate, boolean validateOnly, String serviceId, String resource, String action, RequestData requestData) {
        this.version = version;
        this.msgId = msgId;
        this.requestDate = requestDate;
        this.validateOnly = validateOnly;
        this.serviceId = serviceId;
        this.resource = resource;
        this.action = action;
        this.requestData = requestData;
    }

    private String msgId;
    private String requestDate;
    private boolean validateOnly;
    private String serviceId;
    private String resource;
    private String action;
    private RequestData requestData;
    public void setVersion(String version) {
         this.version = version;
     }
     public String getVersion() {
         return version;
     }

    public void setMsgId(String msgId) {
         this.msgId = msgId;
     }
     public String getMsgId() {
         return msgId;
     }

    public void setRequestDate(String requestDate) {
         this.requestDate = requestDate;
     }
     public String getRequestDate() {
         return requestDate;
     }

    public void setValidateOnly(boolean validateOnly) {
         this.validateOnly = validateOnly;
     }
     public boolean getValidateOnly() {
         return validateOnly;
     }

    public void setServiceId(String serviceId) {
         this.serviceId = serviceId;
     }
     public String getServiceId() {
         return serviceId;
     }

    public void setResource(String resource) {
         this.resource = resource;
     }
     public String getResource() {
         return resource;
     }

    public void setAction(String action) {
         this.action = action;
     }
     public String getAction() {
         return action;
     }

    public void setRequestData(RequestData requestData) {
         this.requestData = requestData;
     }
     public RequestData getRequestData() {
         return requestData;
     }

}