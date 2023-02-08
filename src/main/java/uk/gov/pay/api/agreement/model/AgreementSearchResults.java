package uk.gov.pay.api.agreement.model;

import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.links.SearchNavigationLinks;
import uk.gov.pay.api.model.search.SearchPagination;

import java.util.List;

public class AgreementSearchResults implements SearchPagination {

    @Schema(name = "total", example = "100", description = "Total number of agreements matching your search criteria.")
    private int total;
    @Schema(name = "count", example = "20", description = "Number of agreements on the current page of search results.")
    private int count;
    @Schema(name = "page", example = "1",
            description = "The [page of agreements you’re viewing]" +
                    "(https://docs.payments.service.gov.uk/api_reference/#pagination). " +
                    "To view other pages, make this request again using the `page` parameter.")
    private int page;
    private List<Agreement> results;
    @Schema(name = "_links")
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

    @Schema(name = "results", description = "Contains agreements matching your search criteria.")
    public List<Agreement> getAgreements() {
        return results;
    }

    @Override
    public SearchNavigationLinks getLinks() {
        return links;
    }
}
