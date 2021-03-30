package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.service.payments.commons.api.json.ExternalMetadataSerialiser;
import uk.gov.service.payments.commons.model.SupportedLanguage;
import uk.gov.service.payments.commons.model.charge.ExternalMetadata;

import java.util.Optional;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@Schema(name = "CardPayment")
public class CardPayment extends Payment {

    @JsonProperty("refund_summary")
    private final RefundSummary refundSummary;

    @JsonProperty("settlement_summary")
    private final PaymentSettlementSummary settlementSummary;

    @JsonProperty("card_details")
    private final CardDetails cardDetails;

    @JsonSerialize(using = ToStringSerializer.class)
    private final SupportedLanguage language;

    @JsonProperty("delayed_capture")
    private final boolean delayedCapture;
    
    @JsonProperty("moto")
    private final boolean moto;

    @JsonProperty("corporate_card_surcharge")
    private final Long corporateCardSurcharge;

    @JsonProperty("total_amount")
    private final Long totalAmount;

    @JsonProperty("fee")
    private final Long fee;

    @JsonProperty("net_amount")
    private final Long netAmount;

    @JsonProperty("provider_id")
    @Schema(example = "reference-from-payment-gateway", accessMode = READ_ONLY)
    private final String providerId;

    @JsonSerialize(using = ExternalMetadataSerialiser.class)
    @Schema(name = "metadata", example = "{\"property1\": \"value1\", \"property2\": \"value2\"}\"")
    private final ExternalMetadata metadata;

    @JsonProperty("return_url")
    protected String returnUrl;
    
    protected String email;

    protected PaymentState state;

    //Used by Swagger to document the right model in the PaymentsResource
    @JsonIgnore
    protected String paymentType;


    public CardPayment(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                       String reference, String email, String paymentProvider, String createdDate,
                       RefundSummary refundSummary, PaymentSettlementSummary settlementSummary, CardDetails cardDetails,
                       SupportedLanguage language, boolean delayedCapture, boolean moto, Long corporateCardSurcharge, Long totalAmount,
                       String providerId, ExternalMetadata metadata, Long fee, Long netAmount) {
        super(chargeId, amount, description, reference, paymentProvider, createdDate);
        this.state = state;
        this.refundSummary = refundSummary;
        this.settlementSummary = settlementSummary;
        this.cardDetails = cardDetails;
        this.providerId = providerId;
        this.metadata = metadata;
        this.paymentType = CARD.getFriendlyName();
        this.language = language;
        this.delayedCapture = delayedCapture;
        this.moto = moto;
        this.corporateCardSurcharge = corporateCardSurcharge;
        this.totalAmount = totalAmount;
        this.fee = fee;
        this.netAmount = netAmount;
        this.email = email;
        this.returnUrl = returnUrl;
    }

    /**
     * card brand is no longer a top level charge property. It is now at `card_details.card_brand` attribute
     * We still need to support `v1` clients with a top level card brand attribute to keep support their integrations.
     *
     * @return
     */
    @Schema(description = "Card Brand. Deprecated, please use card_details.card_brand instead", example = "Visa",
            accessMode = READ_ONLY, deprecated = true)
    @JsonProperty("card_brand")
    @Deprecated
    public String getCardBrand() {
        return cardDetails != null ? cardDetails.getCardBrand() : null;
    }

    public ExternalMetadata getMetadata() {
        return metadata;
    }

    public Optional<RefundSummary> getRefundSummary() {
        return Optional.ofNullable(refundSummary);
    }

    public Optional<PaymentSettlementSummary> getSettlementSummary() {
        return Optional.ofNullable(settlementSummary);
    }

    public Optional<CardDetails> getCardDetails() {
        return Optional.ofNullable(cardDetails);
    }

    @Schema(name = "language", example = "en")
    public SupportedLanguage getLanguage() {
        return language;
    }

    @Schema(description = "delayed capture flag", example = "false", accessMode = READ_ONLY)
    public boolean getDelayedCapture() {
        return delayedCapture;
    }

    @Schema(description = "Mail Order / Telephone Order (MOTO) payment flag", example = "false")
    public boolean getMoto() { return moto; }

    @Schema(example = "250", accessMode = READ_ONLY)
    public Optional<Long> getCorporateCardSurcharge() {
        return Optional.ofNullable(corporateCardSurcharge);
    }

    @Schema(example = "5", description = "processing fee taken by the GOV.UK Pay platform, in pence. Only available depending on payment service provider",
            accessMode = READ_ONLY)
    public Optional<Long> getFee() {
        return Optional.ofNullable(fee);
    }

    @Schema(example = "1195", 
            description = "amount including all surcharges and less all fees, in pence. Only available depending on payment service provider",
            accessMode = READ_ONLY)
    public Optional<Long> getNetAmount() {
        return Optional.ofNullable(netAmount);
    }

    @Schema(example = "1450", accessMode = READ_ONLY)
    public Optional<Long> getTotalAmount() {
        return Optional.ofNullable(totalAmount);
    }

    public String getProviderId() {
        return providerId;
    }

    @Schema(example = "http://your.service.domain/your-reference", accessMode = READ_ONLY)
    public Optional<String> getReturnUrl() {
        return Optional.ofNullable(returnUrl);
    }

    @Schema(example = "your email")
    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public PaymentState getState() {
        return state;
    }

    @Override
    public String toString() {
        // Some services put PII in the description, so don’t include it in the stringification
        return "Card Payment{" +
                "paymentId='" + super.paymentId + '\'' +
                ", paymentProvider='" + paymentProvider + '\'' +
                ", cardBrandLabel='" + getCardBrand() + '\'' +
                ", amount=" + amount +
                ", fee=" + fee +
                ", netAmount=" + netAmount +
                ", corporateCardSurcharge='" + corporateCardSurcharge + '\'' +
                ", state='" + state + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", reference='" + reference + '\'' +
                ", language='" + language.toString() + '\'' +
                ", delayedCapture=" + delayedCapture +
                ", moto=" + moto +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }
}
