package uk.gov.pay.api.model.search.directdebit;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.directdebit.mandates.MandateConnectorResponse;
import uk.gov.pay.api.model.links.SearchNavigationLinks;

import java.util.List;

public class DirectDebitSearchMandateResponse {
    @JsonProperty("total")
    private int total;
    @JsonProperty("count")
    private int count;
    @JsonProperty("page")
    private int page;
    @JsonProperty("results")
    private List<MandateConnectorResponse> mandates;
    @JsonProperty("_links")
    private SearchNavigationLinks links = new SearchNavigationLinks();

    DirectDebitSearchMandateResponse() {
        
    }
}
