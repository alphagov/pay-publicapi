package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
    public static final String METADATA = "metadata";
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

    @ApiModelProperty(name = "metadata", dataType = "Map[String,String]")
    private final ExternalMetadata metadata;

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
        this.metadata = builder.getMetadata();
        this.prefilledCardholderDetails = builder.getPrefilledCardholderDetails();
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

    @ApiModelProperty(value = "Which language your users will see on the payment pages when they make a payment.", required = false, example = "en", allowableValues = "en,cy")
    @JsonProperty(LANGUAGE_FIELD_NAME)
    public Optional<SupportedLanguage> getLanguage() {
        return Optional.ofNullable(language);
    }

    @ApiModelProperty(value = "The email address of your user.", required = false, example = "Joe.Bogs@example.org")
    @JsonProperty(EMAIL_FIELD_NAME)
    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    @ApiModelProperty(value = "An HTTPS URL on your site that your user will be sent back to once they have completed their payment attempt on GOV.UK Pay.", required = true, example = "https://service-name.gov.uk/transactions/12345")
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "End user details that you've collected in your service, which the API will use to [prefill fields]( https://docs.payments.service.gov.uk/optional_features/prefill_user_details/) on the payment pages.", required = false)
    @JsonProperty(CreateCardPaymentRequest.PREFILLED_CARDHOLDER_DETAILS_FIELD_NAME)
    public Optional<PrefilledCardholderDetails> getPrefilledCardholderDetails() {
        return Optional.ofNullable(prefilledCardholderDetails);
    }

    @ApiModelProperty(value = "Whether to [delay capturing](https://docs.payments.service.gov.uk/optional_features/delayed_capture/) this payment.", required = false, example = "false")
    @JsonProperty(DELAYED_CAPTURE_FIELD_NAME)
    public Optional<Boolean> getDelayedCapture() {
        return Optional.ofNullable(delayedCapture);
    }

    @JsonProperty("metadata")
    @ApiModelProperty(value = "[Custom metadata](https://docs.payments.service.gov.uk/optional_features/custom_metadata/) to add to the payment.",
            dataType = "java.util.Map", example = "{\"ledger_code\":\"123\", \"reconciled\": true}")
    public Optional<ExternalMetadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    public String toConnectorPayload() {
        JsonStringBuilder request = new JsonStringBuilder()
                .add("amount", this.getAmount())
                .add("reference", this.getReference())
                .add("description", this.getDescription())
                .add("return_url", this.returnUrl);
        getLanguage().ifPresent(language -> request.add("language", language.toString()));
        getDelayedCapture().ifPresent(delayedCapture -> request.add("delayed_capture", delayedCapture));
        getMetadata().ifPresent(metadata -> request.add("metadata", metadata.getMetadata()));
        getEmail().ifPresent(email -> request.add("email", email));

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
        getLanguage().ifPresent(value -> joiner.add("language: ").add(value.toString()));
        getDelayedCapture().ifPresent(value -> joiner.add("delayed_capture: ").add(value.toString()));
        getMetadata().ifPresent(value -> joiner.add("metadata: ").add(value.toString()));
        return joiner.toString();
    }
}
