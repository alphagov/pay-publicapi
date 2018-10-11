package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.links.PaymentSearchNavigationLinks;
import uk.gov.pay.api.model.search.ISearchPagination;

import java.util.List;

public class PaymentSearchResponse implements ISearchPagination<PaymentSearchNavigationLinks> {

    @JsonProperty("total")
    private int total;

    @JsonProperty("count")
    private int count;

    @JsonProperty("page")
    private int page;

    @JsonProperty("results")
    private List<ChargeFromResponse> payments;

    @JsonProperty("_links")
    private PaymentSearchNavigationLinks links = new PaymentSearchNavigationLinks();

    public List<ChargeFromResponse> getPayments() {
        return payments;
    }
    @Override
    public int getTotal() {
        return total;
    }
    @Override
    public int getCount() {
        return count;
    }
    @Override
    public int getPage() {
        return page;
    }

    public PaymentSearchNavigationLinks getLinks() {
        return links;
    }
}
