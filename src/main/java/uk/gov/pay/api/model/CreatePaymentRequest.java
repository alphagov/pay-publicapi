package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;
import uk.gov.pay.commons.model.SupportedLanguage;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Optional;
import java.util.StringJoiner;

@ApiModel(value = "CreatePaymentRequest", description = "The Payment Request Payload")
public abstract class CreatePaymentRequest {

    public static final int EMAIL_MAX_LENGTH = 254;
    public static final String AMOUNT_FIELD_NAME = "amount";
    public static final String REFERENCE_FIELD_NAME = "reference";
    public static final String DESCRIPTION_FIELD_NAME = "description";
    public static final String LANGUAGE_FIELD_NAME = "language";
    public static final String EMAIL_FIELD_NAME = "email";
    public static final int REFERENCE_MAX_LENGTH = 255;
    public static final int AMOUNT_MAX_VALUE = 10000000;
    public static final int AMOUNT_MIN_VALUE = 0;
    public static final int DESCRIPTION_MAX_LENGTH = 255;
    
    // Even though the minimum is 0, this is only allowed for accounts this is enabled for and is a hidden feature
    // so the validation error message will always state that the minimum is 1 for consistency.
    @Min(value = AMOUNT_MIN_VALUE, message = "Must be greater than or equal to 1")
    @Max(value = AMOUNT_MAX_VALUE, message = "Must be less than or equal to {value}")
    private final int amount;

    @Size(max = REFERENCE_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private final String reference;

    @Size(max = DESCRIPTION_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private final String description;
    
    private final SupportedLanguage language;

    @Length(max = EMAIL_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private final String email;
    
    public CreatePaymentRequest(CreatePaymentRequestBuilder createPaymentRequestBuilder) {
        this.amount = createPaymentRequestBuilder.getAmount();
        this.reference = createPaymentRequestBuilder.getReference();
        this.description = createPaymentRequestBuilder.getDescription();
        this.language = createPaymentRequestBuilder.getLanguage();
        this.email = createPaymentRequestBuilder.getEmail();
    }

    public abstract String toConnectorPayload();

    public abstract TokenPaymentType getRequestType();

    @ApiModelProperty(value = "amount in pence", required = true, allowableValues = "range[1, 10000000]", example = "12000")
    public int getAmount() {
        return amount;
    }

    @ApiModelProperty(value = "payment reference", required = true, example = "12345")
    public String getReference() {
        return reference;
    }

    @ApiModelProperty(value = "payment description", required = true, example = "New passport application")
    public String getDescription() {
        return description;
    }

    @ApiModelProperty(value = "ISO-639-1 Alpha-2 code of a supported language to use on the payment pages", required = false, example = "en")
    @JsonProperty(LANGUAGE_FIELD_NAME)
    public Optional<SupportedLanguage> getLanguage() {
        return Optional.ofNullable(language);
    }

    @ApiModelProperty(value = "email", required = false, example = "Joe.Bogs@example.org")
    @JsonProperty(EMAIL_FIELD_NAME)
    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
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
        Optional.ofNullable(language).ifPresent(value -> joiner.add("language: ").add(value.toString()));
        return joiner.toString();
    }
}
