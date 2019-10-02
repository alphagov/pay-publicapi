package uk.gov.pay.api.model.search.directdebit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.directdebit.DirectDebitConnectorPaymentResponse;
import uk.gov.pay.api.model.links.SearchNavigationLinks;
import uk.gov.pay.api.model.search.SearchPagination;

import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectDebitSearchResponse implements SearchPagination {

    @JsonProperty("total")
    @Schema(example = "100", accessMode = READ_ONLY)
    private int total;
    @JsonProperty("count")
    @Schema(example = "20", accessMode = READ_ONLY)
    private int count;
    @JsonProperty("page")
    @Schema(example = "1", accessMode = READ_ONLY)
    private int page;
    @JsonProperty("results")
    @Schema(name = "results", accessMode = READ_ONLY)
    private List<DirectDebitConnectorPaymentResponse> payments;
    @JsonProperty("_links")
    private SearchNavigationLinks links = new SearchNavigationLinks();

    public List<DirectDebitConnectorPaymentResponse> getPayments() {
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
    public SearchNavigationLinks getLinks() {
        return links;
    }
}
