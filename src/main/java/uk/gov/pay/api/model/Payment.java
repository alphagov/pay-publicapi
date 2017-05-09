package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
public abstract class Payment {
    public static final String LINKS_JSON_ATTRIBUTE = "_links";

    @JsonProperty("payment_id")
    private final String paymentId;

    @JsonProperty("payment_provider")
    private final String paymentProvider;

    private final long amount;
    private final PaymentState state;
    private final String description;

    @JsonProperty("return_url")
    private final String returnUrl;
    private final String reference;
    private final String email;

    @JsonProperty("created_date")
    private final String createdDate;

    @JsonProperty("refund_summary")
    private final RefundSummary refundSummary;

    @JsonProperty("settlement_summary")
    private final SettlementSummary settlementSummary;

    @JsonProperty("card_details")
    private final CardDetails cardDetails;

    public Payment(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                   String reference, String email, String paymentProvider, String createdDate,
                   RefundSummary refundSummary, SettlementSummary settlementSummary, CardDetails cardDetails) {
        this.paymentId = chargeId;
        this.amount = amount;
        this.state = state;
        this.returnUrl = returnUrl;
        this.description = description;
        this.reference = reference;
        this.email = email;
        this.paymentProvider = paymentProvider;
        this.createdDate = createdDate;
        this.refundSummary = refundSummary;
        this.settlementSummary = settlementSummary;
        this.cardDetails = cardDetails;
    }

    @ApiModelProperty(example = "2016-01-21T17:15:00Z")
    public String getCreatedDate() {
        return createdDate;
    }

    @ApiModelProperty(example = "hu20sqlact5260q2nanm0q8u93")
    public String getPaymentId() {
        return paymentId;
    }

    @ApiModelProperty(example = "1200")
    public long getAmount() {
        return amount;
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.PaymentState")
    public PaymentState getState() {
        return state;
    }

    @ApiModelProperty(example = "http://your.service.domain/your-reference")
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(example = "Your Service Description")
    public String getDescription() {
        return description;
    }

    @ApiModelProperty(example = "your-reference")
    public String getReference() {
        return reference;
    }

    @ApiModelProperty(example = "your email")
    public String getEmail() {
        return email;
    }

    @ApiModelProperty(example = "worldpay")
    public String getPaymentProvider() {
        return paymentProvider;
    }

    /**
     * card brand is no longer a top level charge property. It is now at `card_details.card_brand` attribute
     * We still need to support `v1` clients with a top level card brand attribute to keep support their integrations.
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

    @Override
    public String toString() {
        // Some services put PII in the description, so don’t include it in the stringification
        return "Payment{" +
                "paymentId='" + paymentId + '\'' +
                ", paymentProvider='" + paymentProvider + '\'' +
                ", cardBrandLabel='" + getCardBrand() + '\'' +
                ", amount=" + amount +
                ", state='" + state + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }
}
