package uk.gov.pay.api.model.ledger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundTransactionFromLedger {

    Long amount;
    String description;
    String reference;
    String createdDate;
    String refundedBy;
    String transactionId;
    TransactionState state;

    public Long getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getRefundedBy() {
        return refundedBy;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public TransactionState getState() {
        return state;
    }
}
