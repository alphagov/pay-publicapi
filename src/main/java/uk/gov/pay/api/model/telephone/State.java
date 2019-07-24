package uk.gov.pay.api.model.telephone;

import com.fasterxml.jackson.annotation.JsonProperty;

public class State {

    @JsonProperty("status")
    private String status;

    @JsonProperty("finished")
    private Boolean finished;

    @JsonProperty("message")
    private String message;

    @JsonProperty("code")
    private String code;

    public State() {
        // For Jackson serialization
    }

    public State(String status, Boolean finished, String message, String code) {
        this.status = status;
        this.finished = finished;
        this.message = message;
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public Boolean getFinished() {
        return finished;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }
}
