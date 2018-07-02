package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaymentSearchResults {

    @JsonProperty(value = "results")
    private List<PaymentForSearchResult> payments;

    public PaymentSearchResults(List<PaymentForSearchResult> payments) {
        this.payments = payments;
    }

    public List<PaymentForSearchResult> getPayments() {
        return payments;
    }

}
