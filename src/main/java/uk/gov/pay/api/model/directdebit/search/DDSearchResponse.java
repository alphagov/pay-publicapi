package uk.gov.pay.api.model.directdebit.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.IPaymentSearchPagination;
import uk.gov.pay.api.model.links.PaymentSearchNavigationLinks;

import java.util.List;


public class DDSearchResponse implements IPaymentSearchPagination {

    @JsonProperty("total")
    private int total;

    @JsonProperty("count")
    private int count;

    @JsonProperty("page")
    private int page;
    
    @JsonProperty("payer")
    private DDPayer payer;

    @JsonProperty("results")
    private List<DDTransactionFromResponse> payments;

    @JsonProperty("_links")
    private PaymentSearchNavigationLinks links = new PaymentSearchNavigationLinks();
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

    public DDPayer getPayer() { return payer; }

    public List<DDTransactionFromResponse> getPayments() {
        return payments;
    }
    @Override
    public PaymentSearchNavigationLinks getLinks() {
        return links;
    }
}
