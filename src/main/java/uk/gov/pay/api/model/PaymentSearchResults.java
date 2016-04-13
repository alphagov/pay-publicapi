package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaymentSearchResults {

    @JsonProperty(value = "results")
    private List<PaymentForSearchResult> payments;

    public PaymentSearchResults(List<PaymentForSearchResult> payments) {
        this.payments = payments;
    }
}
