package uk.gov.pay.api.ledger.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.api.model.links.SearchNavigationLinks;
import uk.gov.pay.api.model.search.SearchPagination;
import uk.gov.pay.api.model.search.card.PaymentForSearchResult;

import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TransactionSearchResults implements SearchPagination {

    private int total;
    private int count;
    private int page;
    private List<PaymentForSearchResult> results;
    SearchNavigationLinks links;

    public TransactionSearchResults() {
    }

    public TransactionSearchResults(int total, int count, int page, List<PaymentForSearchResult> results,
                                    SearchNavigationLinks links) {
        this.total = total;
        this.count = count;
        this.page = page;
        this.results = results;
        this.links = links;
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

    @JsonProperty("results")
    public List<PaymentForSearchResult> getResults() {
        return results;
    }

    @Override
    @JsonProperty("_links")
    public SearchNavigationLinks getLinks() {
        return links;
    }
}
