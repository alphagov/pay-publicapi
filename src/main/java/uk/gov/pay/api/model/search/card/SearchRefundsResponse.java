package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.links.SearchRefundsNavigationLinks;
import uk.gov.pay.api.model.search.ISearchPagination;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchRefundsResponse implements ISearchPagination<SearchRefundsNavigationLinks> {

    @JsonProperty("total")
    private int total;

    @JsonProperty("count")
    private int count;

    @JsonProperty("status")
    private String status;

    @JsonProperty("page")
    private int page;

    @JsonProperty("results")
    private List<RefundForSearchRefundsResult> refunds;

    @JsonProperty("_links")
    private SearchRefundsNavigationLinks links = new SearchRefundsNavigationLinks();

    public List<RefundForSearchRefundsResult> getRefunds() {
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
