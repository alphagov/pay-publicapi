package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectDebitPaymentState {

    private String details;

    private String status;

    private boolean finished;

    public DirectDebitPaymentState() {
    }

    public DirectDebitPaymentState(String status, boolean finished, String details) {
        this.status = status;
        this.finished = finished;
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public String getStatus() {
        return status;
    }

    public boolean isFinished() {
        return finished;
    }
}
