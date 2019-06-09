package uk.gov.pay.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.directdebit.search.DirectDebitPaymentState;
import uk.gov.pay.api.model.directdebit.search.DirectDebitTransactionFromResponse;
import uk.gov.pay.api.model.directdebit.DDTransactionLinksForSearch;

import java.net.URI;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectDebitTransactionForSearch {

    private Long amount;
    @JsonProperty("transaction_id")
    private String transactionId;
    private DirectDebitPaymentState state;
    private String description;
    private String reference;
    private final String email;
    private final String name;
    @JsonProperty(value = "created_date")
    private String createdDate;
    @JsonProperty("agreement_id")
    private String agreementId;
    @JsonProperty("links")
    private DDTransactionLinksForSearch links = new DDTransactionLinksForSearch();
    
    
    
    private DirectDebitTransactionForSearch(Long amount, String transactionId, DirectDebitPaymentState state, String description,
                                            String reference, String email, String name, String createdDate, URI selfLink,
                                            String agreementId) {
        this.amount = amount;
        this.transactionId = transactionId;
        this.state = state;
        this.description = description;
        this.reference = reference;
        this.createdDate = createdDate;
        this.email = email;
        this.name = name;
        this.agreementId = agreementId;
        links.addSelf(selfLink.toString());
    }

    public Long getAmount() { return amount; }

    public String getTransactionId() { return transactionId; }

    public DirectDebitPaymentState getState() { return state; }

    public String getDescription() { return description; }

    public String getReference() { return reference; }

    public String getEmail() { return email; }

    public String getName() { return name; }

    public String getCreatedDate() { return createdDate; }

    public String getAgreementId() { return agreementId; }

    public DDTransactionLinksForSearch getLinks() { return links; }
    
    public static DirectDebitTransactionForSearch valueOf(DirectDebitTransactionFromResponse forSearch, URI selfLink) {
        return new DirectDebitTransactionForSearch(
                forSearch.getAmount(),
                forSearch.getTransactionId(),
                forSearch.getState(),
                forSearch.getDescription(),
                forSearch.getReference(),
                forSearch.getEmail(),
                forSearch.getName(),
                forSearch.getCreatedDate(),
                selfLink,
                forSearch.getAgreementId()
        );
    }
}
