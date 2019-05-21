package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.api.validation.ValidReturnUrl;
import uk.gov.pay.commons.model.charge.ExternalMetadata;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.Optional;
import java.util.StringJoiner;

@ApiModel(value = "CreatePaymentRequest", description = "The Payment Request Payload")
public class CreateCardPaymentRequest extends CreatePaymentRequest {

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

    @ValidReturnUrl
    @Size(max = URL_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    @JsonProperty("return_url")
    private final String returnUrl;

    private final Boolean delayedCapture;

    private final ExternalMetadata metadata;

    @Valid
    private final PrefilledCardholderDetails prefilledCardholderDetails;

    public CreateCardPaymentRequest(CreatePaymentRequestBuilder createPaymentRequestBuilder) {
        super(createPaymentRequestBuilder);
        this.prefilledCardholderDetails = createPaymentRequestBuilder.getPrefilledCardholderDetails();
        this.delayedCapture = createPaymentRequestBuilder.getDelayedCapture();
        this.returnUrl = createPaymentRequestBuilder.getReturnUrl();
        this.metadata = createPaymentRequestBuilder.getMetadata();
    }

    @Override
    public TokenPaymentType getRequestType() {
        return TokenPaymentType.CARD;
    }

    @ApiModelProperty(value = "service return url", required = true, example = "https://service-name.gov.uk/transactions/12345")
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "prefilled_cardholder_details", required = false)
    @JsonProperty(CreateCardPaymentRequest.PREFILLED_CARDHOLDER_DETAILS_FIELD_NAME)
    public Optional<PrefilledCardholderDetails> getPrefilledCardholderDetails() {
        return Optional.ofNullable(prefilledCardholderDetails);
    }

    @ApiModelProperty(value = "delayed capture flag", required = false, example = "false")
    @JsonProperty(DELAYED_CAPTURE_FIELD_NAME)
    public Optional<Boolean> getDelayedCapture() {
        return Optional.ofNullable(delayedCapture);
    }

    @JsonProperty("metadata")
    public Optional<ExternalMetadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    @Override
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
        joiner.add("amount: ").add(String.valueOf(super.getAmount()));
        joiner.add("reference: ").add(super.getReference());
        joiner.add("return_url: ").add(returnUrl);
        super.getLanguage().ifPresent(value -> joiner.add("language: ").add(value.toString()));
        getDelayedCapture().ifPresent(value -> joiner.add("delayed_capture: ").add(value.toString()));
        getMetadata().ifPresent(value -> joiner.add("metadata: ").add(value.toString()));
        return joiner.toString();
    }
}
