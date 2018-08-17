package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.RefundsFromResponse;
import uk.gov.pay.api.model.links.SearchRefundsNavigationLinks;
import uk.gov.pay.api.model.search.ISearchRefundsPagination;

import java.util.List;

public class SearchRefundsResponse implements ISearchRefundsPagination {

    @JsonProperty("total")
    private int total;

    @JsonProperty("count")
    private int count;

    @JsonProperty("page")
    private int page;

    @JsonProperty("results")
    private List<RefundsFromResponse> refunds;

    @JsonProperty("_links")
    private SearchRefundsNavigationLinks links = new SearchRefundsNavigationLinks();

    public List<RefundsFromResponse> getRefunds() {
        return refunds;
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
    public SearchRefundsNavigationLinks getLinks() {
        return links;
    }
}
