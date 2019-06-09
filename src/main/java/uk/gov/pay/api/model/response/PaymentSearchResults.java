package uk.gov.pay.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.search.SearchPagination;

import java.util.List;

/**
 * Used to define swagger specs for search results.
 */
public class PaymentSearchResults implements SearchPagination {

    public PaymentSearchResults() {}
    
    public PaymentSearchResults(int total, int count, int page, List<PaymentForSearchResult> results, SearchNavigationLinks links) {
        this.total = total;
        this.count = count;
        this.page = page;
        this.results = results;
        this.links = links;
    }

    @ApiModelProperty(name = "total", example = "100")
    private int total;
    @ApiModelProperty(name = "count", example = "20")
    private int count;
    @ApiModelProperty(name = "page", example = "1")
    private int page;
    private List<PaymentForSearchResult> results;
    @ApiModelProperty(name = "_links")
    SearchNavigationLinks links;

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

    @ApiModelProperty(name = "results")
    public List<PaymentForSearchResult> getResults() {
        return results;
    }

    @Override
    @JsonProperty("_links")
    public SearchNavigationLinks getLinks() {
        return links;
    }
}
