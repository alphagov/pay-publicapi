package uk.gov.pay.api.model.directdebit.mandates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MandateState {
    @JsonProperty("status")
    private String status;

    @JsonProperty("finished")
    private boolean finished;

    @JsonProperty("message")
    private String message;

    @JsonProperty("code")
    private String code;

    @JsonProperty("details")
    private String details;

    public MandateState() {}
    
    public MandateState(String status, boolean finished, String details) {
        this(status, finished, null, null, details);
    }

    public MandateState(String status, boolean finished, String message, String code, String details) {
        this.status = status;
        this.finished = finished;
        this.message = message;
        this.code = code;
        this.details = details;
    }

    public MandateState(String status, boolean finished) {
        this(status, finished, null, null, null);
    }

    public String getStatus() {
        return status;
    }

    public boolean isFinished() {
        return finished;
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public String toString() {
        return "MandateState{" +
                "status='" + status + '\'' +
                ", finished='" + finished + '\'' +
                ", message=" + message +
                ", code=" + code +
                ", details=" + details+
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MandateState that = (MandateState) o;
        return finished == that.finished &&
                Objects.equals(status, that.status) &&
                Objects.equals(details, that.details) &&
                Objects.equals(message, that.message) &&
                Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, finished, message, code, details);
    }
}
