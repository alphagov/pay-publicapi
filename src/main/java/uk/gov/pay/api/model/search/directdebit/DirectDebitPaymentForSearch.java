package uk.gov.pay.api.model.search.directdebit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.pay.api.model.search.links.DDPaymentLinksForSearch;

import java.net.URI;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectDebitPaymentForSearch {

    private Long amount;
    @JsonProperty("transaction_id")
    private String paymentId;
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
    private DDPaymentLinksForSearch links = new DDPaymentLinksForSearch();
    
    
    
    private DirectDebitPaymentForSearch(Long amount, String paymentId, DirectDebitPaymentState state, String description,
                                        String reference, String email, String name, String createdDate, URI selfLink,
                                        String agreementId) {
        this.amount = amount;
        this.paymentId = paymentId;
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

    public String getPaymentId() { return paymentId; }

    public DirectDebitPaymentState getState() { return state; }

    public String getDescription() { return description; }

    public String getReference() { return reference; }

    public String getEmail() { return email; }

    public String getName() { return name; }

    public String getCreatedDate() { return createdDate; }

    public String getAgreementId() { return agreementId; }

    public DDPaymentLinksForSearch getLinks() { return links; }
    
    public static DirectDebitPaymentForSearch valueOf(DirectDebitPaymentFromResponse forSearch, URI selfLink) {
        return new DirectDebitPaymentForSearch(
                forSearch.getAmount(),
                forSearch.getPaymentId(),
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
