package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectDebitPaymentState {

    private String details;

    private String status;

    public DirectDebitPaymentState() {
    }

    public DirectDebitPaymentState(String status, String details) {
        this.status = status;
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectDebitPaymentState that = (DirectDebitPaymentState) o;
        return Objects.equals(details, that.details) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(details, status);
    }
}
