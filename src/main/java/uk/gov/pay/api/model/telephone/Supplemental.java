package uk.gov.pay.api.model.telephone;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Supplemental {
    
    @JsonProperty("error_code")
    private String errorCode;
    
    @JsonProperty("error_message")
    private String errorMessage;

    public Supplemental() {
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
