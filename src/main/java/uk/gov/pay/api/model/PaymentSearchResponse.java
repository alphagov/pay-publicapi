package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.links.PaymentSearchNavigationLinks;

import java.util.List;

public class PaymentSearchResponse {

    @JsonProperty("total")
    private int total;

    @JsonProperty("count")
    private int count;

    @JsonProperty("page")
    private int page;

    @JsonProperty("results")
    private List<PaymentResult> payments;

    @JsonProperty("_links")
    private PaymentSearchNavigationLinks links = new PaymentSearchNavigationLinks();

    public int getTotal() {
        return total;
    }

    public int getCount() {
        return count;
    }

    public int getPage() {
        return page;
    }

    public List<PaymentResult> getPayments() {
        return payments;
    }

    public PaymentSearchNavigationLinks getLinks() {
        return links;
    }
}
