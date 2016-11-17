package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChargeFromResponse {

    @JsonProperty("charge_id")
    private String chargeId;

    @JsonProperty("return_url")
    private String returnUrl;

    @JsonProperty("payment_provider")
    private String paymentProvider;

    @JsonProperty("links")
    private List<PaymentConnectorResponseLink> links = new ArrayList<>();

    @JsonProperty(value = "refund_summary")
    private RefundSummary refundSummary;

    @JsonProperty(value = "card_details")
    private CardDetails cardDetails;

    private Long amount;
    private PaymentState state;
    private String description;
    private String reference;
    private String email;

    @JsonProperty(value = "created_date")
    private String createdDate;

    public String getChargeId() {
        return chargeId;
    }

    public Long getAmount() {
        return amount;
    }

    public PaymentState getState() {
        return state;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public String getEmail() {
        return email;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    /**
     * card brand is no longer a top level charge property. It is now at `card_details.card_brand` attribute
     * We still need to support `v1` clients with a top level card brand attribute to keep support their integrations.
     * @return
     */
    @JsonProperty("card_brand")
    public String getCardBrand() {
        return cardDetails != null ? cardDetails.getCardBrand() : "";
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public List<PaymentConnectorResponseLink> getLinks() {
        return links;
    }

    public RefundSummary getRefundSummary() {
        return refundSummary;
    }

    public CardDetails getCardDetails() {
        return cardDetails;
    }
}
