package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.commons.model.SupportedLanguage;

import static uk.gov.pay.api.model.TokenPaymentType.CARD;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@ApiModel(value = "CardPayment")
public class CardPayment extends Payment {

    @JsonProperty("refund_summary")
    private final RefundSummary refundSummary;

    @JsonProperty("settlement_summary")
    private final SettlementSummary settlementSummary;

    @JsonProperty("card_details")
    private final CardDetails cardDetails;

    @JsonSerialize(using = ToStringSerializer.class)
    private final SupportedLanguage language;

    public CardPayment(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                       String reference, String email, String paymentProvider, String createdDate,
                       RefundSummary refundSummary, SettlementSummary settlementSummary, CardDetails cardDetails,
                       SupportedLanguage language) {
        super(chargeId, amount, state, returnUrl, description, reference, email, paymentProvider, createdDate);
        this.refundSummary = refundSummary;
        this.settlementSummary = settlementSummary;
        this.cardDetails = cardDetails;
        this.paymentType = CARD.getFriendlyName();
        this.language = language;
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
    public RefundSummary getRefundSummary() {
        return refundSummary;
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.SettlementSummary")
    public SettlementSummary getSettlementSummary() {
        return settlementSummary;
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.CardDetails")
    public CardDetails getCardDetails() {
        return cardDetails;
    }

    @ApiModelProperty(example = "en")
    public SupportedLanguage getLanguage() {
        return language;
    }

    @Override
    public String toString() {
        // Some services put PII in the description, so don’t include it in the stringification
        return "Card Payment{" +
                "paymentId='" + super.paymentId + '\'' +
                ", paymentProvider='" + paymentProvider + '\'' +
                ", cardBrandLabel='" + getCardBrand() + '\'' +
                ", amount=" + amount +
                ", state='" + state + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", reference='" + reference + '\'' +
                ", language='" + language.toString() + '\'' +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }

}
