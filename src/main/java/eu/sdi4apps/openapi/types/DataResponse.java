/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.sdi4apps.openapi.types;

/**
 *
 * @author runarbe
 */
public class DataResponse extends Response {

    public Object data;

    public DataResponse() {
        super();
    }

    public void setData(Object data, boolean success) {
        this.data = data;
        if (success) {
            this.setSuccess();
        } else {
            this.setError();
        }
    }

    public DataResponse mergeResponse(DataResponse response) {

        this.time = response.time != 0 ? response.time : this.time;

        this.action = response.action != null ? response.action : this.action;
        
        this.status = response.status;

        for (String s : response.messages) {
            this.messages.add(s);
        }

        this.parameters = (null != response.parameters && "" != response.parameters) ? response.parameters : this.parameters;
        
        this.data = (null != response.data && "" != response.data) ? response.data : this.data;
                
        return this;
    }

}
