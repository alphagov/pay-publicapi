package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Optional;


@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@ApiModel(value = "Payment", discriminator = "paymentType", subTypes = {
        CardPayment.class, DirectDebitPayment.class })
public abstract class Payment {
    public static final String LINKS_JSON_ATTRIBUTE = "_links";

    @JsonProperty("payment_id")
    protected final String paymentId;

    @JsonProperty("payment_provider")
    protected final String paymentProvider;

    protected final long amount;
    protected final PaymentState state;
    protected final String description;

    @JsonProperty("return_url")
    protected final String returnUrl;
    protected final String reference;
    protected final String email;

    @JsonProperty("created_date")
    protected final String createdDate;

    //Used by Swagger to document the right model in the PaymentsResource
    @JsonIgnore
    protected String paymentType;

    public Payment(String chargeId, long amount, PaymentState state, String returnUrl, String description,
                   String reference, String email, String paymentProvider, String createdDate) {
        this.paymentId = chargeId;
        this.amount = amount;
        this.state = state;
        this.returnUrl = returnUrl;
        this.description = description;
        this.reference = reference;
        this.email = email;
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
    public Optional<String> getReturnUrl() {
        return Optional.ofNullable(returnUrl);
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
    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    @ApiModelProperty(example = "worldpay")
    public String getPaymentProvider() {
        return paymentProvider;
    }
}
