package uk.gov.pay.api.model.telephone;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.Optional;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Supplemental {
    
    @JsonProperty("error_code")
    private String errorCode;
    
    @JsonProperty("error_message")
    private String errorMessage;

    public Supplemental() {
    }

    public Supplemental(String errorCode, String errorMessage) {
        // For testing deserialization
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public Optional<String> getErrorCode() {
        return Optional.ofNullable(errorCode);
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }
}
