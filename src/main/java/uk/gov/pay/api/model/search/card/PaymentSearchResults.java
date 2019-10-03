package uk.gov.pay.api.model.search.card;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.links.SearchNavigationLinks;
import uk.gov.pay.api.model.search.SearchPagination;

import java.util.List;

/**
 * Used to define swagger specs for search results.
 */
public class PaymentSearchResults implements SearchPagination {

    @ApiModelProperty(name = "total", example = "100")
    @Schema(name = "total", example = "100")
    private int total;
    @ApiModelProperty(name = "count", example = "20")
    @Schema(name = "count", example = "20")
    private int count;
    @ApiModelProperty(name = "page", example = "1")
    @Schema(name = "page", example = "1")
    private int page;
    private List<PaymentForSearchResult> results;
    @ApiModelProperty(name = "_links")
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

    @ApiModelProperty(name = "results")
    @Schema(name = "results")
    public List<PaymentForSearchResult> getPayments() {
        return results;
    }

    @Override
    public SearchNavigationLinks getLinks() {
        return links;
    }
}
