package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.RefundsFromResponse;
import uk.gov.pay.api.model.links.SearchRefundsNavigationLinks;
import java.util.List;

public class SearchRefundsResponse {

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
    public int getTotal() {
        return total;
    }

    public int getCount() {
        return count;
    }

    public int getPage() {
        return page;
    }
    public SearchRefundsNavigationLinks getLinks() {
        return links;
    }
}
