package uk.gov.pay.api.model.search.dispute;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.api.model.links.SearchNavigationLinks;
import uk.gov.pay.api.model.search.SearchPagination;

import java.util.List;
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DisputesSearchResults implements SearchPagination {
    private int total;
    private int count;
    private int page;
    private List<DisputeForSearchResult> results;
    SearchNavigationLinks links;

    public DisputesSearchResults(int total, int count, int page, List<DisputeForSearchResult> results,
                                 SearchNavigationLinks links) {
        this.total = total;
        this.count = count;
        this.page = page;
        this.results = results;
        this.links = links;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public int getTotal() {
        return total;
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public SearchNavigationLinks getLinks() {
        return links;
    }

    public List<DisputeForSearchResult> getResults() {
        return results;
    }
}
