package uk.gov.pay.api.model.card;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ConnectorSearchResponse {

    @JsonProperty("total")
    private int total;

    @JsonProperty("count")
    private int count;

    @JsonProperty("page")
    private int page;

    @JsonProperty("results")
    private List<ChargeFromResponse> payments;

    @JsonProperty("_links")
    public ConnectorSearchNavigationLinks links;

    public List<ChargeFromResponse> getPayments() {
        return payments;
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
}
