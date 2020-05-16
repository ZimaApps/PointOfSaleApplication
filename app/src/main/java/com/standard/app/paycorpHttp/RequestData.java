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
public class RequestData {
    public RequestData(boolean validateOnly, String operation, String requestDate, String tlvData) {
        this.validateOnly = validateOnly;
        this.operation = operation;
        this.requestDate = requestDate;
        this.tlvData = tlvData;
    }

    private boolean validateOnly;
    private String operation;
    private String requestDate;
    private String tlvData;
    public void setValidateOnly(boolean validateOnly) {
         this.validateOnly = validateOnly;
     }
     public boolean getValidateOnly() {
         return validateOnly;
     }

    public void setOperation(String operation) {
         this.operation = operation;
     }
     public String getOperation() {
         return operation;
     }

    public void setRequestDate(String requestDate) {
         this.requestDate = requestDate;
     }
     public String getRequestDate() {
         return requestDate;
     }

    public void setTlvData(String tlvData) {
         this.tlvData = tlvData;
     }
     public String getTlvData() {
         return tlvData;
     }

}