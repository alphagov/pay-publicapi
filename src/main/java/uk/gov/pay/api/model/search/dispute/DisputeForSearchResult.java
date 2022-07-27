package uk.gov.pay.api.model.search.dispute;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.ledger.DisputeSettlementSummary;
import uk.gov.pay.api.model.ledger.DisputeTransactionFromLedger;
import uk.gov.pay.api.model.links.DisputeLinksForSearch;

import java.net.URI;

import static uk.gov.pay.api.model.Payment.LINKS_JSON_ATTRIBUTE;

public class DisputeForSearchResult {
    private Long amount;
    @JsonProperty("created_date")
    private String createdDate;
    @JsonProperty("dispute_id")
    private String disputeId;
    @JsonProperty("evidence_due_date")
    private String evidenceDueDate;
    private Long fee;
    @JsonProperty("net_amount")
    private Long netAmount;
    @JsonProperty("payment_id")
    private String paymentId;
    private String reason;
    @JsonProperty("settlement_summary")
    private DisputeSettlementSummary settlementSummary;
    private String status;
    @JsonProperty(LINKS_JSON_ATTRIBUTE)
    private DisputeLinksForSearch links = new DisputeLinksForSearch();

    public DisputeForSearchResult(Long amount, String createdDate, String disputeId, String evidenceDueDate,
                                  Long fee, Long netAmount, String paymentId, String reason,
                                  DisputeSettlementSummary settlementSummary, String status, URI paymentURI) {
        this.amount = amount;
        this.createdDate = createdDate;
        this.disputeId = disputeId;
        this.evidenceDueDate = evidenceDueDate;
        this.fee = fee;
        this.netAmount = netAmount;
        this.paymentId = paymentId;
        this.reason = reason;
        this.settlementSummary = settlementSummary;
        this.status = status;
        this.links.addPayment(paymentURI.toString());
    }

    public static DisputeForSearchResult valueOf(DisputeTransactionFromLedger fromLedger, URI paymentUri) {
        return new DisputeForSearchResult(fromLedger.getAmount(), fromLedger.getCreatedDate(), fromLedger.getTransactionId(),
                fromLedger.getEvidenceDueDate(), fromLedger.getFee(), fromLedger.getNetAmount(), fromLedger.getParentTransactionId(),
                fromLedger.getReason(), fromLedger.getSettlementSummary(), fromLedger.getState().getStatus(),
                paymentUri);
    }

    public Long getAmount() {
        return amount;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getDisputeId() {
        return disputeId;
    }

    public String getEvidenceDueDate() {
        return evidenceDueDate;
    }

    public Long getFee() {
        return fee;
    }

    public Long getNetAmount() {
        return netAmount;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getReason() {
        return reason;
    }

    public DisputeSettlementSummary getSettlementSummary() {
        return settlementSummary;
    }

    public String getStatus() {
        return status;
    }

    public DisputeLinksForSearch getLinks() {
        return links;
    }
}
