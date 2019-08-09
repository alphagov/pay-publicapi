package uk.gov.pay.api.model.ledger;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefundsFromLedger {

    String parentTransactionId;
    List<RefundTransactionFromLedger> transactions;

    public String getParentTransactionId() {
        return parentTransactionId;
    }

    public List<RefundTransactionFromLedger> getTransactions() {
        return transactions;
    }
}
