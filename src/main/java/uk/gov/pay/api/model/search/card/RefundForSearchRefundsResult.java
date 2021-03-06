package uk.gov.pay.api.model.search.card;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.RefundSettlementSummary;
import uk.gov.pay.api.model.ledger.RefundTransactionFromLedger;
import uk.gov.pay.api.model.links.RefundLinksForSearch;

import java.net.URI;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "RefundDetailForSearch")
@Schema(name = "RefundDetailForSearch")
public class RefundForSearchRefundsResult {

    @JsonProperty("refund_id")
    @ApiModelProperty(example = "act4c33g40j3edfmi8jknab84x")
    @Schema(example = "act4c33g40j3edfmi8jknab84x", accessMode = READ_ONLY)
    private String refundId;

    @JsonProperty("created_date")
    @ApiModelProperty(example = "2017-01-10T16:52:07.855Z")
    @Schema(example = "2017-01-10T16:52:07.855Z", accessMode = READ_ONLY)
    private String createdDate;

    private String chargeId;

    private Long amount;

    private RefundLinksForSearch links = new RefundLinksForSearch();

    @JsonProperty("status")
    @ApiModelProperty(example = "success", allowableValues = "submitted,success,error")
    @Schema(example = "success", allowableValues = {"submitted", "success", "error"}, accessMode = READ_ONLY)
    private String status;

    @Schema(accessMode = READ_ONLY)
    private RefundSettlementSummary settlementSummary;

    public RefundForSearchRefundsResult() {
    }

    public RefundForSearchRefundsResult(String refundId, String createdDate, String status,
                                        String chargeId, Long amount, URI paymentURI, URI refundsURI,
                                        RefundSettlementSummary settlementSummary) {
        this.refundId = refundId;
        this.createdDate = createdDate;
        this.status = status;
        this.chargeId = chargeId;
        this.amount = amount;
        this.links.addSelf(refundsURI.toString());
        this.links.addPayment(paymentURI.toString());
        this.settlementSummary = settlementSummary;
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

    @JsonProperty("payment_id")
    @ApiModelProperty(example = "2q1r18djndhsrm3closjqr81fx", hidden = true)
    @Schema(hidden = true)
    public String getChargeId() {
        return chargeId;
    }

    @JsonProperty("charge_id")
    @Schema(hidden = true)
    public void setChargeId(String chargeId) {
        this.chargeId = chargeId;
    }

    @JsonProperty("amount_submitted")
    @Schema(hidden = true)
    public void setAmount(Long amount) {
        this.amount = amount;
    }

    @JsonProperty("amount")
    @ApiModelProperty(example = "120")
    @Schema(example = "120", accessMode = READ_ONLY)
    public Long getAmount() {
        return amount;
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.links.RefundLinksForSearch")
    @JsonProperty("_links")
    public RefundLinksForSearch getLinks() {
        return links;
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.RefundSettlementSummary")
    @JsonProperty("settlement_summary")
    @Schema(accessMode = READ_ONLY)
    public RefundSettlementSummary getSettlementSummary() {
        return settlementSummary;
    }

    public static RefundForSearchRefundsResult valueOf(RefundTransactionFromLedger refundResult, URI paymentURI, URI refundsURI) {
        return new RefundForSearchRefundsResult(
                refundResult.getTransactionId(),
                refundResult.getCreatedDate(),
                refundResult.getState().getStatus(),
                refundResult.getParentTransactionId(),
                refundResult.getAmount(),
                paymentURI,
                refundsURI,
                refundResult.getSettlementSummary());
    }

    @Override
    public String toString() {
        return "RefundForSearchRefundsResult{" +
                "refundId='" + refundId + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", status='" + status + '\'' +
                ", amount=" + amount + '\'' +
                ", links=" + links + '\'' +
                '}';
    }
}
