package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

import static java.util.Arrays.asList;

public abstract class Payment {
    public static final String LINKS_JSON_ATTRIBUTE = "_links";

    @JsonProperty("payment_id")
    private final String paymentId;

    @JsonProperty("payment_provider")
    private final String paymentProvider;

    @JsonProperty("amount")
    private final long amount;

    @JsonProperty("state")
    private final PaymentState state;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("return_url")
    private final String returnUrl;

    @JsonProperty("reference")
    private final String reference;

    @JsonProperty("created_date")
    private final String createdDate;

    public Payment(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                    String reference, String paymentProvider, String createdDate) {
        this.paymentId = chargeId;
        this.amount = amount;
        this.state = state;
        this.returnUrl = returnUrl;
        this.description = description;
        this.reference = reference;
        this.paymentProvider = paymentProvider;
        this.createdDate = createdDate;
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

    @ApiModelProperty(example = "worldpay")
    public String getPaymentProvider() {
        return paymentProvider;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId='" + paymentId + '\'' +
                ", paymentProvider='" + paymentProvider + '\'' +
                ", amount=" + amount +
                ", state='" + state + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", description='" + description + '\'' +
                ", reference='" + reference + '\'' +
                ", createdDate='" + createdDate + '\'' +
                '}';
    }
}
