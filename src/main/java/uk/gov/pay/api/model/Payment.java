package uk.gov.pay.api.model;

import black.door.hate.JacksonHalResource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@Schema(name = "Payment", subTypes = {CardPayment.class})
public abstract class Payment implements JacksonHalResource {
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

    @Schema(example = "2016-01-21T17:15:00.000Z", accessMode = READ_ONLY)
    public String getCreatedDate() {
        return createdDate;
    }

    @Schema(example = "hu20sqlact5260q2nanm0q8u93", 
            description = "The unique ID GOV.UK Pay automatically associated " +
                    "with this payment when you created it.", 
            accessMode = READ_ONLY)
    public String getPaymentId() {
        return paymentId;
    }

    @Schema(example = "1200", description = "The description assigned to the payment when it was created.")
    public long getAmount() {
        return amount;
    }

    @Schema(example = "Your Service Description", description = "The description assigned to the payment when it was created.")
    public String getDescription() {
        return description;
    }

    @Schema(example = "your-reference", 
            description = "The reference associated with the payment when it was created. " +
            "`reference` is not unique - multiple payments can have the same `reference` value.")
    public String getReference() {
        return reference;
    }

    @Schema(example = "worldpay", 
            description = "The payment service provider that processed this payment.", 
            accessMode = READ_ONLY)
    public String getPaymentProvider() {
        return paymentProvider;
    }
    
    
}
