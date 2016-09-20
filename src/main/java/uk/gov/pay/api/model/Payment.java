package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(value= JsonInclude.Include.NON_NULL)
public abstract class           Payment {
    public static final String LINKS_JSON_ATTRIBUTE = "_links";

    @JsonProperty("payment_id")
    private final String paymentId;

    @JsonProperty("payment_provider")
    private final String paymentProvider;

    @JsonProperty("card_brand")
    private final String cardBrand;

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

    public Payment(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                   String reference, String email, String paymentProvider, String cardBrand,
                   String createdDate, RefundSummary refundSummary) {
        this.paymentId = chargeId;
        this.amount = amount;
        this.state = state;
        this.returnUrl = returnUrl;
        this.description = description;
        this.reference = reference;
        this.email = email;
        this.paymentProvider = paymentProvider;
        this.cardBrand = cardBrand;
        this.createdDate = createdDate;
        this.refundSummary = refundSummary;
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

    @ApiModelProperty(value = "Card Brand", example = "Visa")
    public String getCardBrand(){
        return cardBrand;
    }

    @ApiModelProperty(dataType = "uk.gov.pay.api.model.RefundSummary")
    public RefundSummary getRefundSummary() {
        return refundSummary;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId='" + paymentId + '\'' +
                ", paymentProvider='" + paymentProvider + '\'' +
                ", cardBrandLabel='" + cardBrand + '\'' +
                ", amount=" + amount +
                ", state='" + state + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", description='" + description + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }
}
