package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.model.RefundsFromResponse;

public class RefundsForSearchRefundsResult {
    
    @JsonProperty(value = "refund_id")
    private String refundId;

    @JsonProperty(value = "created_date")
    private String createdDate;
    
    @JsonProperty("refund_summary")
    private final RefundSummary refundSummary;

    private String status;
    public RefundsForSearchRefundsResult(String refundId,
                                  String createdDate,
                                  String status,
                                  RefundSummary refundSummary) {
        this.refundId = refundId;
        this.createdDate = createdDate;
        this.status = status;
        this.refundSummary = refundSummary;
    }

    public static RefundsForSearchRefundsResult valueOf(RefundsFromResponse refundResult) {
        return new RefundsForSearchRefundsResult(
                refundResult.getRefundId(), 
                refundResult.getCreatedDate(), 
                refundResult.getStatus(), 
                refundResult.getRefundSummary());
    }

    @Override
    public String toString() {
        return "RefundForSearchRefundsResult{" +
                "refundId='" + refundId + '\'' +
                ", status='" + status + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", refundSummary='" + refundSummary + '\'' +
                '}';
    }
}
