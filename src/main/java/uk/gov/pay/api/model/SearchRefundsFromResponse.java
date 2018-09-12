package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchRefundsFromResponse {

    @JsonProperty(value = "refund_id")
    private String refundId;

    @JsonProperty(value = "created_date")
    private String createdDate;

    @JsonProperty(value = "status")
    private String status;

    @JsonProperty(value = "charge_id")
    private String chargeId;

    @JsonProperty("amount_submitted")
    private Long amountSubmitted;

    @JsonProperty("links")
    private List<Map<String, Object>> dataLinks = new ArrayList<>();
    
    public String getRefundId() {
        return refundId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getStatus() {
        return status;
    }

    public String getChargeId() {
        return chargeId;
    }

    public Long getAmountSubmitted() {
        return amountSubmitted;
    }

    public List<Map<String, Object>> getLinks() {
        return dataLinks;
    }
}
