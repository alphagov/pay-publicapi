package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.SearchRefundsFromResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RefundForSearchRefundsResult {
    
    @JsonProperty(value = "refund_id")
    private String refundId;

    @JsonProperty(value = "created_date")
    private String createdDate;

    @JsonProperty("charge_id")
    private String chargeId;

    @JsonProperty("amount_submitted")
    private Long amountSubmitted;

    @JsonProperty("links")
    private List<Map<String, Object>> dataLinks = new ArrayList<>();

    @JsonProperty("status")
    private String status;
    
    public RefundForSearchRefundsResult(String refundId,
                                        String createdDate,
                                        String status,
                                        String chargeId,
                                        Long amountSubmitted,
                                        List<Map<String, Object>> links) {
        this.refundId = refundId;
        this.createdDate = createdDate;
        this.status = status;
        this.chargeId = chargeId;
        this.amountSubmitted = amountSubmitted;
        this.dataLinks = links;
    }

    public static RefundForSearchRefundsResult valueOf(SearchRefundsFromResponse refundResult) {
        return new RefundForSearchRefundsResult(
                refundResult.getRefundId(), 
                refundResult.getCreatedDate(), 
                refundResult.getStatus(), 
                refundResult.getChargeId(),
                refundResult.getAmountSubmitted(),
                refundResult.getLinks());
    }

    @Override
    public String toString() {
        return "RefundForSearchRefundsResult{" +
                "refundId='" + refundId + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", status='" + status + '\'' +
                ", amountSubmitted=" + amountSubmitted + '\'' +
                ", dataLinks=" + dataLinks + '\'' +
                '}';
    }
}
