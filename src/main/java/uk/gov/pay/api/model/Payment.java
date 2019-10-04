package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import uk.gov.pay.api.model.directdebit.mandates.DirectDebitPayment;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;


@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@ApiModel(value = "Payment", discriminator = "paymentType", subTypes = {
        CardPayment.class, DirectDebitPayment.class})
@Schema(name = "Payment", subTypes = {CardPayment.class, DirectDebitPayment.class})
public abstract class Payment {
    public static final String LINKS_JSON_ATTRIBUTE = "_links";

    @JsonProperty("payment_id")
    protected String paymentId;

    @JsonProperty("payment_provider")
    protected String paymentProvider;

    protected long amount;
    protected String description;
    protected String reference;


    @JsonProperty("created_date")
    protected String createdDate;
    
    protected Payment() {
        //To enable Jackson serialisation we need a default constructor
    }

    public Payment(String chargeId, 
                   long amount, 
                   String description, 
                   String reference, 
                   String paymentProvider, 
                   String createdDate) {
        this.paymentId = chargeId;
        this.amount = amount;
        this.description = description;
        this.reference = reference;
        this.paymentProvider = paymentProvider;
        this.createdDate = createdDate;
    }

    @ApiModelProperty(example = "2016-01-21T17:15:000Z")
    @Schema(example = "2016-01-21T17:15:000Z", accessMode = READ_ONLY)
    public String getCreatedDate() {
        return createdDate;
    }

    @ApiModelProperty(example = "hu20sqlact5260q2nanm0q8u93")
    @Schema(example = "hu20sqlact5260q2nanm0q8u93", accessMode = READ_ONLY)
    public String getPaymentId() {
        return paymentId;
    }

    @ApiModelProperty(example = "1200")
    @Schema(example = "1200")
    public long getAmount() {
        return amount;
    }

    @ApiModelProperty(example = "Your Service Description")
    @Schema(example = "Your Service Description")
    public String getDescription() {
        return description;
    }

    @ApiModelProperty(example = "your-reference")
    @Schema(example = "your-reference")
    public String getReference() {
        return reference;
    }

    @ApiModelProperty(example = "worldpay")
    @Schema(example = "worldpay", accessMode = READ_ONLY)
    public String getPaymentProvider() {
        return paymentProvider;
    }
}
