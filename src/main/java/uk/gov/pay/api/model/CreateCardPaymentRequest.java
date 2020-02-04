package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hibernate.validator.constraints.Length;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.api.validation.ValidReturnUrl;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.model.charge.ExternalMetadata;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Optional;
import java.util.StringJoiner;

@ApiModel(description = "The Payment Request Payload")
@Schema(description = "The Payment Request Payload")
public class CreateCardPaymentRequest {

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
    public static final String RETURN_URL_FIELD_NAME = "return_url";
    public static final int URL_MAX_LENGTH = 2000;
    public static final String PREFILLED_CARDHOLDER_DETAILS_FIELD_NAME = "prefilled_cardholder_details";
    public static final String PREFILLED_CARDHOLDER_NAME_FIELD_NAME = "cardholder_name";
    public static final String PREFILLED_BILLING_ADDRESS_FIELD_NAME = "billing_address";
    public static final String PREFILLED_ADDRESS_LINE1_FIELD_NAME = "line1";
    public static final String PREFILLED_ADDRESS_LINE2_FIELD_NAME = "line2";
    public static final String PREFILLED_ADDRESS_CITY_FIELD_NAME = "city";
    public static final String PREFILLED_ADDRESS_POSTCODE_FIELD_NAME = "postcode";
    public static final String PREFILLED_ADDRESS_COUNTRY_FIELD_NAME = "country";
    public static final String DELAYED_CAPTURE_FIELD_NAME = "delayed_capture";
    public static final String MOTO_FIELD_NAME = "moto";
    public static final String SOURCE_FIELD_NAME = "source";
    public static final String METADATA = "metadata";
    public static final String INTERNAL = "internal";
    private static final String PREFILLED_CARDHOLDER_DETAILS = "prefilled_cardholder_details";
    private static final String BILLING_ADDRESS = "billing_address";
    
    // Even though the minimum is 0, this is only allowed for accounts this is enabled for and is a hidden feature
    // so the validation error message will always state that the minimum is 1 for consistency.
    @JsonProperty("amount")
    @Min(value = AMOUNT_MIN_VALUE, message = "Must be greater than or equal to 1")
    @Max(value = AMOUNT_MAX_VALUE, message = "Must be less than or equal to {value}")
    private int amount;

    @JsonProperty("reference")
    @Size(max = REFERENCE_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private String reference;

    @JsonProperty("description")
    @Size(max = DESCRIPTION_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private String description;

    @JsonProperty("language")
    private SupportedLanguage language;

    @JsonProperty("email")
    @Length(max = EMAIL_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private String email;
    
    @ValidReturnUrl
    @Size(max = URL_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    @JsonProperty("return_url")
    private final String returnUrl;

    private final Boolean delayedCapture;
    
    private final Boolean moto;

    @ApiModelProperty(name = "metadata", dataType = "Map[String,String]")
    @Schema(name = "metadata", example = "{\"property1\": \"value1\", \"property2\": \"value2\"}\"")
    private final ExternalMetadata metadata;
    
    private final Internal internal;

    @Valid
    private final PrefilledCardholderDetails prefilledCardholderDetails;
    
    public CreateCardPaymentRequest(CreateCardPaymentRequestBuilder builder) {
        this.amount = builder.getAmount();
        this.reference = builder.getReference();
        this.description = builder.getDescription();
        this.language = builder.getLanguage();
        this.email = builder.getEmail();
        this.returnUrl = builder.getReturnUrl();
        this.delayedCapture = builder.getDelayedCapture();
        this.moto = builder.isMoto();
        this.metadata = builder.getMetadata();
        this.prefilledCardholderDetails = builder.getPrefilledCardholderDetails();
        this.internal = builder.getInternal();
    }
    
    @ApiModelProperty(value = "amount in pence", required = true, allowableValues = "range[1, 10000000]", example = "12000")
    @Schema(description = "amount in pence", required = true, minimum = "1", maximum = "10000000", example = "12000")
    public int getAmount() {
        return amount;
    }

    @ApiModelProperty(value = "payment reference", required = true, example = "12345")
    @Schema(description = "payment reference", required = true, example = "12345")
    public String getReference() {
        return reference;
    }

    @ApiModelProperty(value = "payment description", required = true, example = "New passport application")
    @Schema(description = "payment description", required = true, example = "New passport application")
    public String getDescription() {
        return description;
    }

    @ApiModelProperty(value = "ISO-639-1 Alpha-2 code of a supported language to use on the payment pages", required = false, example = "en", allowableValues = "en,cy")
    @Schema(description = "ISO-639-1 Alpha-2 code of a supported language to use on the payment pages", example = "en")
    @JsonProperty(LANGUAGE_FIELD_NAME)
    public Optional<SupportedLanguage> getLanguage() {
        return Optional.ofNullable(language);
    }

    @ApiModelProperty(value = "email", required = false, example = "Joe.Bogs@example.org")
    @Schema(name = "email", example = "Joe.Bogs@example.org", description = "email")
    @JsonProperty(EMAIL_FIELD_NAME)
    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }
    
    @ApiModelProperty(value = "service return url", required = true, example = "https://service-name.gov.uk/transactions/12345")
    @Schema(description = "service return url", required = true, example = "https://service-name.gov.uk/transactions/12345")
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "prefilled_cardholder_details", required = false)
    @Schema(description = "prefilled_cardholder_details")
    @JsonProperty(CreateCardPaymentRequest.PREFILLED_CARDHOLDER_DETAILS_FIELD_NAME)
    public Optional<PrefilledCardholderDetails> getPrefilledCardholderDetails() {
        return Optional.ofNullable(prefilledCardholderDetails);
    }

    @ApiModelProperty(value = "delayed capture flag", required = false, example = "false")
    @Schema(description = "delayed capture flag", example = "false")
    @JsonProperty(DELAYED_CAPTURE_FIELD_NAME)
    public Optional<Boolean> getDelayedCapture() {
        return Optional.ofNullable(delayedCapture);
    }

    @JsonProperty(MOTO_FIELD_NAME)
    public Optional<Boolean> getMoto() {
        return Optional.ofNullable(moto);
    }

    @JsonProperty("metadata")
    @ApiModelProperty(value = "Additional metadata - up to 10 name/value pairs - on the payment. " +
            "Each key must be between 1 and 30 characters long. " +
            "The value, if a string, must be no greater than 50 characters long. " +
            "Other permissible value types: boolean, number.",
            dataType = "java.util.Map", example = "{\"ledger_code\":\"123\", \"reconciled\": true}")
    @Schema(description = "Additional metadata - up to 10 name/value pairs - on the payment. " +
            "Each key must be between 1 and 30 characters long. " +
            "The value, if a string, must be no greater than 50 characters long. " +
            "Other permissible value types: boolean, number.",
            example = "{\"ledger_code\":\"123\", \"reconciled\": true}")
    public Optional<ExternalMetadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    @JsonProperty("internal")
    @ApiModelProperty(hidden = true)
    @Schema(hidden = true)
    public Optional<Internal> getInternal() {
        return Optional.ofNullable(internal);
    }

    public String toConnectorPayload() {
        JsonStringBuilder request = new JsonStringBuilder()
                .add("amount", this.getAmount())
                .add("reference", this.getReference())
                .add("description", this.getDescription())
                .add("return_url", this.getReturnUrl());
        getLanguage().ifPresent(language -> request.add("language", language.toString()));
        getDelayedCapture().ifPresent(delayedCapture -> request.add("delayed_capture", delayedCapture));
        getMoto().ifPresent(moto -> request.add("moto", moto));
        getMetadata().ifPresent(metadata -> request.add("metadata", metadata.getMetadata()));
        getEmail().ifPresent(email -> request.add("email", email));
        getInternal().flatMap(Internal::getSource).ifPresent(source -> request.add("source", source));
        
        getPrefilledCardholderDetails().ifPresent(prefilledDetails -> {
            prefilledDetails.getCardholderName().ifPresent(name -> request.addToMap(PREFILLED_CARDHOLDER_DETAILS, "cardholder_name", name));
            prefilledDetails.getBillingAddress().ifPresent(address -> {
                request.addToNestedMap("line1", address.getLine1(), PREFILLED_CARDHOLDER_DETAILS, BILLING_ADDRESS);
                request.addToNestedMap("line2", address.getLine2(), PREFILLED_CARDHOLDER_DETAILS, BILLING_ADDRESS);
                request.addToNestedMap("postcode", address.getPostcode(), PREFILLED_CARDHOLDER_DETAILS, BILLING_ADDRESS);
                request.addToNestedMap("city", address.getCity(), PREFILLED_CARDHOLDER_DETAILS, BILLING_ADDRESS);
                request.addToNestedMap("country", address.getCountry(), PREFILLED_CARDHOLDER_DETAILS, BILLING_ADDRESS);
            });
        });
        
        return request.build();
    }

    /**
     * This looks JSONesque but is not identical to the received request
     */
    @Override
    public String toString() {
        // Some services put PII in the description, so don’t include it in the stringification
        StringJoiner joiner = new StringJoiner(", ", "{", "}");
        joiner.add("amount: ").add(String.valueOf(getAmount()));
        joiner.add("reference: ").add(getReference());
        joiner.add("return_url: ").add(returnUrl);
        getInternal().flatMap(Internal::getSource).ifPresent(source -> joiner.add("source: ").add(source.toString()));
        getLanguage().ifPresent(value -> joiner.add("language: ").add(value.toString()));
        getDelayedCapture().ifPresent(value -> joiner.add("delayed_capture: ").add(value.toString()));
        getMoto().ifPresent(value -> joiner.add("moto: ").add(value.toString()));
        getMetadata().ifPresent(value -> joiner.add("metadata: ").add(value.toString()));
        return joiner.toString();
    }
}
