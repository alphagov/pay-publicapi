package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.model.links.RefundLinksForSearch;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RefundForSearchRefundsResult {

    private RefundLinksForSearch links = new RefundLinksForSearch();

    @JsonProperty("refund_id")
    private String refundId;

    @JsonProperty("created_date")
    private String createdDate;

    @JsonProperty("charge_id")
    private String chargeId;

    @JsonProperty("amount_submitted")
    private Long amountSubmitted;

    @JsonProperty("links")
    private List<Map<String, Object>> dataLinks = new ArrayList<>();

    @JsonProperty("status")
    private String status;

    public RefundForSearchRefundsResult() {
    }

    public RefundForSearchRefundsResult(String refundId, String createdDate, String status, String chargeId, Long amountSubmitted, URI paymentURI, URI refundsURI) {
        this.refundId = refundId;
        this.createdDate = createdDate;
        this.status = status;
        this.chargeId = chargeId;
        this.amountSubmitted = amountSubmitted;
        this.links.addSelf(refundsURI.toString()); 
        this.links.addPayment(paymentURI.toString()); 
    }

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
    @ApiModelProperty(dataType = "uk.gov.pay.api.model.links.PaymentLinksForSearch")
    public RefundLinksForSearch getLinks() {
        return links;
    }

    public static RefundForSearchRefundsResult valueOf(RefundForSearchRefundsResult refundResult, URI paymentURI, URI refundsURI) {
        return new RefundForSearchRefundsResult(
                refundResult.getRefundId(),
                refundResult.getCreatedDate(),
                refundResult.getStatus(),
                refundResult.getChargeId(),
                refundResult.getAmountSubmitted(),
                paymentURI,
                refundsURI);
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
