package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PaymentSearchResults {

    @JsonProperty(value = "results")
    private List<PaymentForSearchResult> payments;

    @JsonProperty(value= "total")
    private int total;

    @JsonProperty(value="count")
    private int count;

    @JsonProperty(value="_links")
    private List<Object> links;

    public PaymentSearchResults(List<PaymentForSearchResult> payments) {
        this.payments = payments;
    }

    public List<PaymentForSearchResult> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentForSearchResult> payments) {
        this.payments = payments;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Object> getLinks() {
        return links;
    }

    public void setLinks(List<Object> links) {
        this.links = links;
    }
}
