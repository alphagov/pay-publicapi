package uk.gov.pay.api.model.search.directdebit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.links.PaymentSearchNavigationLinks;
import uk.gov.pay.api.model.search.ISearchPagination;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DDSearchResponse implements ISearchPagination<PaymentSearchNavigationLinks> {

    @JsonProperty("total")
    private int total;
    @JsonProperty("count")
    private int count;
    @JsonProperty("page")
    private int page;
    @JsonProperty("results")
    private List<DDTransactionFromResponse> payments;
    @JsonProperty("_links")
    private PaymentSearchNavigationLinks links = new PaymentSearchNavigationLinks();

    public List<DDTransactionFromResponse> getPayments() {
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

    @Override
    public PaymentSearchNavigationLinks getLinks() {
        return links;
    }
}
