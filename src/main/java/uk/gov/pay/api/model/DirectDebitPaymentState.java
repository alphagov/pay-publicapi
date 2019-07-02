package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectDebitPaymentState that = (DirectDebitPaymentState) o;
        return finished == that.finished &&
                Objects.equals(details, that.details) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(details, status, finished);
    }
}
