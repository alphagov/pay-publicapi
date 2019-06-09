package uk.gov.pay.api.model.directdebit.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectDebitSearchResponse {

    @JsonProperty("total")
    public int total;
    @JsonProperty("count")
    public int count;
    @JsonProperty("page")
    public int page;
    @JsonProperty("results")
    public List<DirectDebitTransactionFromResponse> results;
    @JsonProperty("_links")
    public DirectDebitSearchNavigationLinks links = new DirectDebitSearchNavigationLinks();
}
