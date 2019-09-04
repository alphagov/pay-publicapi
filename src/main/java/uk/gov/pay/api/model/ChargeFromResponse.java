package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import uk.gov.pay.api.model.telephone.PaymentOutcome;
import uk.gov.pay.api.utils.CustomSupportedLanguageDeserializer;
import uk.gov.pay.commons.api.json.ExternalMetadataDeserialiser;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.model.charge.ExternalMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @JsonProperty(value = "settlement_summary")
    private SettlementSummary settlementSummary;

    @JsonProperty(value = "card_details")
    private CardDetails cardDetails;

    private Long amount;

    private PaymentState state;

    private String description;

    private String reference;

    private String email;
    
    @JsonProperty(value = "telephone_number")
    private String telephoneNumber;

    @JsonDeserialize(using = CustomSupportedLanguageDeserializer.class)
    private SupportedLanguage language;

    @JsonProperty(value = "delayed_capture")
    private boolean delayedCapture;

    @JsonProperty("corporate_card_surcharge")
    private Long corporateCardSurcharge;

    @JsonProperty("total_amount")
    private Long totalAmount;

    @JsonProperty("fee")
    private Long fee;

    @JsonProperty("net_amount")
    private Long netAmount;

    @JsonProperty(value = "created_date")
    private String createdDate;

    @JsonProperty(value = "authorised_date")
    private String authorisedDate;

    @JsonProperty(value = "processor_id")
    private String processorId;
    
    @JsonProperty(value = "provider_id")
    private String providerId;
    
    @JsonProperty(value = "auth_code")
    private String authCode;
    
    @JsonProperty(value = "payment_outcome")
    private PaymentOutcome paymentOutcome;

    @JsonProperty(value = "gateway_transaction_id")
    private String gatewayTransactionId;

    @JsonDeserialize(using = ExternalMetadataDeserialiser.class)
    private ExternalMetadata metadata;

    public Optional<ExternalMetadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }

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

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public SupportedLanguage getLanguage() {
        return language;
    }

    public boolean getDelayedCapture() {
        return delayedCapture;
    }

    public Long getCorporateCardSurcharge() {
        return corporateCardSurcharge;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }
    
    public Long getFee() {
        return fee;
    }

    public Long getNetAmount() {
        return netAmount;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    /**
     * card brand is no longer a top level charge property. It is now at `card_details.card_brand` attribute
     * We still need to support `v1` clients with a top level card brand attribute to keep support their integrations.
     *
     * @return
     */
    @JsonProperty("card_brand")
    public String getCardBrand() {
        return cardDetails != null ? cardDetails.getCardBrand() : "";
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getAuthorisedDate() {
        return authorisedDate;
    }

    public String getProcessorId() {
        return processorId;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getAuthCode() {
        return authCode;
    }

    public PaymentOutcome getPaymentOutcome() {
        return paymentOutcome;
    }

    public List<PaymentConnectorResponseLink> getLinks() {
        return links;
    }

    public RefundSummary getRefundSummary() {
        return refundSummary;
    }

    public SettlementSummary getSettlementSummary() {
        return settlementSummary;
    }

    public CardDetails getCardDetails() {
        return cardDetails;
    }

    public String getGatewayTransactionId() {
        return gatewayTransactionId;
    }
}
