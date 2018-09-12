package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundsFromResponse {

    @JsonProperty(value = "refund_id")
    private String refundId;

    @JsonProperty(value = "created_date")
    private String createdDate;

    @JsonProperty(value = "refund_summary")
    private RefundSummary refundSummary;
    
    private String status;
    
    public String getRefundId() {
        return refundId;
    }
    
    public String getCreatedDate() {
        return createdDate;
    }

    public RefundSummary getRefundSummary() {
        return refundSummary;
    }
    public String getStatus() {
        return status;
    }
}
