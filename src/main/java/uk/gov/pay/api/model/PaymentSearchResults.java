package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaymentSearchResults {

    @JsonProperty(value = "results")
    private List<PaymentWithLinks> payments;

    public PaymentSearchResults(List<PaymentWithLinks> payments) {
        this.payments = payments;
    }
}
