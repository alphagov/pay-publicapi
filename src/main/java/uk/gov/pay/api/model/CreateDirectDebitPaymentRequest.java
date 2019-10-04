package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.StringJoiner;

@ApiModel(description = "The structure of your request to the API when you collect a payment against a Direct Debit mandate.")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateDirectDebitPaymentRequest {

    public static final int REFERENCE_MAX_LENGTH = 255;
    public static final int AMOUNT_MAX_VALUE = 10000000;
    public static final int AMOUNT_MIN_VALUE = 100;
    public static final int DESCRIPTION_MAX_LENGTH = 255;
    public static final int MANDATE_ID_MAX_LENGTH = 26;

    @JsonProperty("amount")
    @Min(value = AMOUNT_MIN_VALUE, message = "Must be greater than or equal to {value}")
    @Max(value = AMOUNT_MAX_VALUE, message = "Must be less than or equal to {value}")
    private int amount;

    @JsonProperty("reference")
    @Size(max = REFERENCE_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    @NotBlank
    private String reference;

    @JsonProperty("description")
    @Size(max = DESCRIPTION_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private String description;

    @JsonProperty("mandate_id")
    @Size(max = MANDATE_ID_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    @NotBlank
    private String mandateId;

    public CreateDirectDebitPaymentRequest() {
        //To enable Jackson serialisation we need a default constructor
    }

    @ApiModelProperty(value = "The amount in pence.", required = true, allowableValues = "range[1, 10000000]", example = "12000")
    public int getAmount() {
        return amount;
    }

    @ApiModelProperty(value = "The reference number you want to associate with this payment.", required = true, example = "12345")
    public String getReference() {
        return reference;
    }

    @ApiModelProperty(value = "A human-readable description of the payment.", required = true, example = "New passport application")
    public String getDescription() {
        return description;
    }

    @ApiModelProperty(value = "The identifier of the mandate you want to collect the payment against.", required = false, example = "33890b55-b9ea-4e2f-90fd-77ae0e9009e2")
    public String getMandateId() {
        return mandateId;
    }

    /**
     * This looks JSONesque but is not identical to the received request
     */
    @Override
    public String toString() {
        // Some services put PII in the description, so don’t include it in the stringification
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        joiner.add("amount: ").add(String.valueOf(amount));
        joiner.add("reference: ").add(reference);
        joiner.add("mandate_id: ").add(mandateId);
        return joiner.toString();
    }
}
