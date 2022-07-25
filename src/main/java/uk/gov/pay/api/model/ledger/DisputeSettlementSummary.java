package uk.gov.pay.api.model.ledger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DisputeSettlementSummary {

    @JsonProperty("settled_date")
    private String settledDate;

    public DisputeSettlementSummary() {}

    public DisputeSettlementSummary(String settledDate) {
        this.settledDate = settledDate;
    }

    public String getSettledDate() {
        return settledDate;
    }
}
