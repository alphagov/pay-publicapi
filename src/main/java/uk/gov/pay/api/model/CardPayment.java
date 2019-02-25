package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.commons.model.SupportedLanguage;

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

    @JsonProperty("provider_id")
    @ApiModelProperty(example = "reference-from-payment-gateway")
    private final String providerId;

    public CardPayment(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                       String reference, String email, String paymentProvider, String createdDate,
                       RefundSummary refundSummary, SettlementSummary settlementSummary, CardDetails cardDetails,
                       SupportedLanguage language, boolean delayedCapture, Long corporateCardSurcharge, Long totalAmount, 
                       String providerId) {
        super(chargeId, amount, state, returnUrl, description, reference, email, paymentProvider, createdDate);
        this.refundSummary = refundSummary;
        this.settlementSummary = settlementSummary;
        this.cardDetails = cardDetails;
        this.providerId = providerId;
        this.paymentType = CARD.getFriendlyName();
        this.language = language;
        this.delayedCapture = delayedCapture;
        this.corporateCardSurcharge = corporateCardSurcharge;
        this.totalAmount = totalAmount;
    }

    /**
     * card brand is no longer a top level charge property. It is now at `card_details.card_brand` attribute
     * We still need to support `v1` clients with a top level card brand attribute to keep support their integrations.
     *
     * @return
     */
    @ApiModelProperty(value = "Card Brand", example = "Visa", notes = "Deprecated. Please use card_details.card_brand instead")
    @JsonProperty("card_brand")
    @Deprecated
    public String getCardBrand() {
        return cardDetails != null ? cardDetails.getCardBrand() : null;
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

    @ApiModelProperty(value = "delayed capture flag", example = "false")
    public boolean getDelayedCapture() {
        return delayedCapture;
    }

    @ApiModelProperty(example = "250")
    public Optional<Long> getCorporateCardSurcharge() {
        return Optional.ofNullable(corporateCardSurcharge);
    }

    @ApiModelProperty(example = "1450")
    public Optional<Long> getTotalAmount() {
        return Optional.ofNullable(totalAmount);
    }
    
    public String getProviderId() {
        return providerId;
    }

    @Override
    public String toString() {
        // Some services put PII in the description, so don’t include it in the stringification
        return "Card Payment{" +
                "paymentId='" + super.paymentId + '\'' +
                ", paymentProvider='" + paymentProvider + '\'' +
                ", cardBrandLabel='" + getCardBrand() + '\'' +
                ", amount=" + amount +
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
