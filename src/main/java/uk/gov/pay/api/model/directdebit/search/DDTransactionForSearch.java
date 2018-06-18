package uk.gov.pay.api.model.directdebit.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.directdebit.DDPaymentState;
import uk.gov.pay.api.model.directdebit.search.links.DDTransactionLinksForSearch;

import java.net.URI;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DDTransactionForSearch {
    
    private Long amount;
    @JsonProperty("transaction_id")
    private String transactionId;
    private DDPaymentState state;
    private String description;
    private String reference;
    @JsonProperty(value = "created_date")
    private String createdDate;
    @JsonProperty("links")
    private DDTransactionLinksForSearch links = new DDTransactionLinksForSearch();
    
    
    
    private DDTransactionForSearch(Long amount, String transactionId, DDPaymentState state, String description, String reference, String createdDate, URI selfLink) {
        this.amount = amount;
        this.transactionId = transactionId;
        this.state = state;
        this.description = description;
        this.reference = reference;
        this.createdDate = createdDate;
        links.addSelf(selfLink.toString());
    }

    public Long getAmount() { return amount; }

    public String getTransactionId() { return transactionId; }

    public DDPaymentState getState() { return state; }

    public String getDescription() { return description; }

    public String getReference() { return reference; }

    public String getCreatedDate() { return createdDate; }

    public DDTransactionLinksForSearch getLinks() { return links; }
    
    public static DDTransactionForSearch valueOf(DDTransactionFromResponse forSearch, URI selfLink) {
        return new DDTransactionForSearch(
                forSearch.getAmount(),
                forSearch.getTransactionId(),
                forSearch.getState(),
                forSearch.getDescription(),
                forSearch.getReference(),
                forSearch.getCreatedDate(),
                selfLink
        );
    }
}
