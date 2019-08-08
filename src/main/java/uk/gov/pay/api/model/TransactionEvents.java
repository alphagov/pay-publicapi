package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TransactionEvents {

    private String transactionId;
    private List<TransactionEvent> events;

    public TransactionEvents() {}

    public String getTransactionId() {
        return transactionId;
    }

    public List<TransactionEvent> getEvents() {
        return events;
    }
}
