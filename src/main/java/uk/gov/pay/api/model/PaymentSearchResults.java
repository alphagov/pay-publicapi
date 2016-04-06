package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaymentSearchResults {

    @JsonProperty(value = "results")
    private List<PaymentWithSelfLinks> payments;

    public PaymentSearchResults(List<PaymentWithSelfLinks> payments) {
        this.payments = payments;
    }
}
