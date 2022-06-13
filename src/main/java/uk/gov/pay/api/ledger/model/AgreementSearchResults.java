package uk.gov.pay.api.ledger.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import uk.gov.pay.api.model.AgreementResponse;
import uk.gov.pay.api.model.links.SearchNavigationLinks;
import uk.gov.pay.api.model.search.SearchPagination;
import uk.gov.pay.api.model.search.card.PaymentForSearchResult;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AgreementSearchResults implements SearchPagination {

    private int total;
    private int count;
    private int page;
    private List<AgreementResponse> results;
    SearchNavigationLinks links;

    public AgreementSearchResults() {
    }

    public AgreementSearchResults(int total, int count, int page, List<AgreementResponse> results,
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
    public List<AgreementResponse> getResults() {
        return results;
    }

    @Override
    @JsonProperty("_links")
    public SearchNavigationLinks getLinks() {
        return links;
    }
}
