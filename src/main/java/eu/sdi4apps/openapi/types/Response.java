package eu.sdi4apps.openapi.types;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;

public class Response {

    public String status;

    public int count = 0;

    public List<String> messages;

    public long time;

    public String action;

    public String parameters;

    public Response() {
        this.time = System.nanoTime();
        this.messages = new ArrayList<>();
        this.status = Status.success;
        this.parameters = "";
    }

    public String getMessages() {
        return this.messages.toString();
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public void missingParam(String parameterName) {
        this.addMessage(String.format("Missing mandatory parameter: '%s'", parameterName));
        this.setError();
    }

    public void unsupportedAction(String actionValue) {
        this.addMessage(String.format("Unsupported value for action parameter: '%s'", actionValue));
        this.setError();
    }

    public String asJson() {
        GsonBuilder gson = new GsonBuilder().disableHtmlEscaping();
        return gson.create().toJson(this.calculateTime());
    }

    public void setError(String message) {
        this.addMessage(message);
        this.setError();
    }

    public void setError() {
        this.status = Status.error;
    }
    
    public void setSuccess() {
        this.status = Status.success;
    }

    public static Response Error(String message) {
        Response mResponse = new Response();
        mResponse.setError(message);
        return mResponse;
    }

    public Response calculateTime() {
        this.time = Math.round((System.nanoTime() - this.time) * 0.000001);
        return this;
    }

    public Response mergeResponse(Response response) {

        this.time = response.time != 0 ? response.time : this.time;

        this.action = response.action != null ? response.action : this.action;
        
        this.status = response.status;

        for (String s : response.messages) {
            this.messages.add(s);
        }

        this.parameters = null != response.parameters && "" != response.parameters ? response.parameters : this.parameters;
                
        return this;
    }

}
