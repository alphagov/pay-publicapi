package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.commons.api.json.ExternalMetadataSerialiser;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.model.charge.ExternalMetadata;

import java.util.Optional;

import static uk.gov.pay.api.model.TokenPaymentType.CARD;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@ApiModel(value = "CardPayment")
public class CardPayment extends Payment {

    @JsonProperty("refund_summary")
    private final RefundSummary refundSummary;

    @JsonProperty("settlement_summary")
    private final SettlementSummary settlementSummary;

    @JsonProperty("card_details")
    private final CardDetails cardDetails;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(name = "language", access = "language")
    private final SupportedLanguage language;

    @JsonProperty("delayed_capture")
    @ApiModelProperty(name = "delayed_capture", access = "delayed_capture")
    private final boolean delayedCapture;

    @JsonProperty("corporate_card_surcharge")
    private final Long corporateCardSurcharge;

    @JsonProperty("total_amount")
    private final Long totalAmount;

    @JsonProperty("fee")
    private final Long fee;

    @JsonProperty("net_amount")
    private final Long netAmount;

    @JsonProperty("provider_id")
    @ApiModelProperty(example = "reference-from-payment-gateway")
    private final String providerId;

    @JsonSerialize(using = ExternalMetadataSerialiser.class)
    @ApiModelProperty(name = "metadata", dataType = "Map[String,String]")
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
                       RefundSummary refundSummary, SettlementSummary settlementSummary, CardDetails cardDetails,
                       SupportedLanguage language, boolean delayedCapture, Long corporateCardSurcharge, Long totalAmount,
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
    @ApiModelProperty(value = "The brand of the payment card, for example `master-card`.", example = "Visa", notes = "Deprecated. Please use card_details.card_brand instead")
    @JsonProperty("card_brand")
    @Deprecated
    public String getCardBrand() {
        return cardDetails != null ? cardDetails.getCardBrand() : null;
    }

    public ExternalMetadata getMetadata() {
        return metadata;
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.RefundSummary")
    public Optional<RefundSummary> getRefundSummary() {
        return Optional.ofNullable(refundSummary);
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.SettlementSummary")
    public Optional<SettlementSummary> getSettlementSummary() {
        return Optional.ofNullable(settlementSummary);
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.CardDetails")
    public Optional<CardDetails> getCardDetails() {
        return Optional.ofNullable(cardDetails);
    }

    @ApiModelProperty(example = "en", allowableValues = "en,cy")
    public SupportedLanguage getLanguage() {
        return language;
    }

    @ApiModelProperty(value = "Whether to [delay capturing](https://docs.payments.service.gov.uk/optional_features/delayed_capture/) this payment.", example = "false")
    public boolean getDelayedCapture() {
        return delayedCapture;
    }

    @ApiModelProperty(example = "250")
    public Optional<Long> getCorporateCardSurcharge() {
        return Optional.ofNullable(corporateCardSurcharge);
    }

    @ApiModelProperty(example = "5", value = "The Payment Service Provider (PSP) transaction fee that we took from the amount your user paid, in pence. This is only included in the API response if you use GOV.UK’s PSP.")
    public Optional<Long> getFee() {
        return Optional.ofNullable(fee);
    }

    @ApiModelProperty(example = "1195", value = "The amount that will be paid into your account, in pence. This is only included in the API response if you use GOV.UK’s PSP.")
    public Optional<Long> getNetAmount() {
        return Optional.ofNullable(netAmount);
    }

    @ApiModelProperty(example = "1450")
    public Optional<Long> getTotalAmount() {
        return Optional.ofNullable(totalAmount);
    }

    public String getProviderId() {
        return providerId;
    }

    @ApiModelProperty(example = "http://your.service.domain/your-reference")
    public Optional<String> getReturnUrl() {
        return Optional.ofNullable(returnUrl);
    }

    @ApiModelProperty(example = "your email")
    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.PaymentState")
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
                ", createdDate='" + createdDate + '\'' +
                '}';
    }
}
