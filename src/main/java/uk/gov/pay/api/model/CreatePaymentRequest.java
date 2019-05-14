package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import uk.gov.pay.commons.model.charge.ExternalMetadata;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.Optional;
import java.util.StringJoiner;

import static uk.gov.pay.api.validation.PaymentRequestValidator.AGREEMENT_ID_MAX_LENGTH;

@ApiModel(value = "CreatePaymentRequest", description = "The Payment Request Payload")
public class CreatePaymentRequest {

    public static final int EMAIL_MAX_LENGTH = 254;
    public static final String AMOUNT_FIELD_NAME = "amount";
    public static final String RETURN_URL_FIELD_NAME = "return_url";
    public static final String REFERENCE_FIELD_NAME = "reference";
    public static final String DESCRIPTION_FIELD_NAME = "description";
    public static final String AGREEMENT_ID_FIELD_NAME = "agreement_id";
    public static final String LANGUAGE_FIELD_NAME = "language";
    public static final String DELAYED_CAPTURE_FIELD_NAME = "delayed_capture";
    public static final String METADATA = "metadata";
    public static final String EMAIL_FIELD_NAME = "email";
    public static final String PREFILLED_CARDHOLDER_DETAILS_FIELD_NAME = "prefilled_cardholder_details";
    public static final String PREFILLED_CARDHOLDER_NAME_FIELD_NAME = "cardholder_name";
    public static final String PREFILLED_BILLING_ADDRESS_FIELD_NAME = "billing_address";
    public static final String PREFILLED_ADDRESS_LINE1_FIELD_NAME = "line1";
    public static final String PREFILLED_ADDRESS_LINE2_FIELD_NAME = "line2";
    public static final String PREFILLED_ADDRESS_CITY_FIELD_NAME = "city";
    public static final String PREFILLED_ADDRESS_POSTCODE_FIELD_NAME = "postcode";
    public static final String PREFILLED_ADDRESS_COUNTRY_FIELD_NAME = "country";
    public static final int REFERENCE_MAX_LENGTH = 255;
    public static final int AMOUNT_MAX_VALUE = 10000000;
    public static final int AMOUNT_MIN_VALUE = 1;
    public static final int DESCRIPTION_MAX_LENGTH = 255;
    public static final int URL_MAX_LENGTH = 2000;
    
    @Min(value = AMOUNT_MIN_VALUE, message = "Must be greater than or equal to {value}")
    @Max(value = AMOUNT_MAX_VALUE, message = "Must be less than or equal to {value}")
    private final int amount;

    private final String returnUrl;

    @Size(max = REFERENCE_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private final String reference;

    @Size(max = DESCRIPTION_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private final String description;

    @Size(max = AGREEMENT_ID_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    @JsonProperty(value = "agreement_id")
    private final String agreementId;
    
    private final String language;
    
    private final Boolean delayedCapture;
    
    private final ExternalMetadata metadata;

    @Length(max = EMAIL_MAX_LENGTH, message = "Must be less than or equal to {max} characters length")
    private final String email;
    
    private final PrefilledCardholderDetails prefilledCardholderDetails;
    
    private CreatePaymentRequest(CreatePaymentRequestBuilder createPaymentRequestBuilder) {
        this.amount = createPaymentRequestBuilder.amount;
        this.returnUrl = createPaymentRequestBuilder.returnUrl;
        this.reference = createPaymentRequestBuilder.reference;
        this.description = createPaymentRequestBuilder.description;
        this.agreementId = createPaymentRequestBuilder.agreementId;
        this.language = createPaymentRequestBuilder.language;
        this.delayedCapture = createPaymentRequestBuilder.delayedCapture;
        this.metadata = createPaymentRequestBuilder.metadata;
        this.email = createPaymentRequestBuilder.email;
        this.prefilledCardholderDetails = createPaymentRequestBuilder.getPrefilledCardholderDetails();
    }

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

    @ApiModelProperty(value = "service return url", required = false, example = "https://service-name.gov.uk/transactions/12345")
    @JsonProperty("return_url")
    public String getReturnUrl() {
        return returnUrl;
    }

    @ApiModelProperty(value = "ID of the agreement being used to collect the payment", required = false, example = "33890b55-b9ea-4e2f-90fd-77ae0e9009e2")
    @JsonProperty(AGREEMENT_ID_FIELD_NAME)
    public String getAgreementId() {
        return agreementId;
    }

    @ApiModelProperty(value = "ISO-639-1 Alpha-2 code of a supported language to use on the payment pages", required = false, example = "en")
    @JsonProperty(LANGUAGE_FIELD_NAME)
    public String getLanguage() {
        return language;
    }

    @ApiModelProperty(value = "delayed capture flag", required = false, example = "false")
    @JsonProperty(DELAYED_CAPTURE_FIELD_NAME)
    public Boolean getDelayedCapture() {
        return delayedCapture;
    }

    @JsonProperty("metadata")
    public ExternalMetadata getMetadata() {
        return metadata;
    }

    @ApiModelProperty(value = "email", required = false, example = "Joe.Bogs@example.org")
    @JsonProperty(EMAIL_FIELD_NAME)
    public String getEmail() {
        return email;
    }

    @ApiModelProperty(value = "prefilled_cardholder_details", required = false)
    @JsonProperty(PREFILLED_CARDHOLDER_DETAILS_FIELD_NAME)
    public PrefilledCardholderDetails getPrefilledCardholderDetails() {
        return prefilledCardholderDetails;
    }

    public boolean hasReturnUrl() {
        return StringUtils.isNotBlank(returnUrl);
    }

    public boolean hasAgreementId() {
        return StringUtils.isNotBlank(agreementId);
    }

    public boolean hasLanguage() {
        return StringUtils.isNotBlank(language);
    }

    public boolean hasEmail() {
        return StringUtils.isNotBlank(email);
    }

    public boolean hasPrefilledCardholderDetails() {
        return prefilledCardholderDetails != null;
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
        Optional.ofNullable(returnUrl).ifPresent(value -> joiner.add("return_url: ").add(value));
        Optional.ofNullable(agreementId).ifPresent(value -> joiner.add("agreement_id: ").add(value));
        Optional.ofNullable(language).ifPresent(value -> joiner.add("language: ").add(value));
        Optional.ofNullable(delayedCapture).ifPresent(value -> joiner.add("delayed_capture: ").add(value.toString()));
        Optional.ofNullable(metadata).ifPresent(value -> joiner.add("metadata: ").add(value.toString()));
        return joiner.toString();
    }

    public static class CreatePaymentRequestBuilder {
        private ExternalMetadata metadata;
        private int amount;
        private String returnUrl;
        private String reference;
        private String description;
        private String agreementId;
        private String language;
        private Boolean delayedCapture;
        private String email;
        private String cardholderName;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String postcode;
        private String country;
        private PrefilledCardholderDetails prefilledCardholderDetails;

        public CreatePaymentRequestBuilder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public CreatePaymentRequestBuilder returnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
            return this;
        }

        public CreatePaymentRequestBuilder reference(String reference) {
            this.reference = reference;
            return this;
        }

        public CreatePaymentRequestBuilder description(String description) {
            this.description = description;
            return this;
        }

        public CreatePaymentRequestBuilder agreementId(String agreementId) {
            this.agreementId = agreementId;
            return this;
        }

        public CreatePaymentRequestBuilder language(String language) {
            this.language = language;
            return this;
        }

        public CreatePaymentRequestBuilder delayedCapture(Boolean delayedCapture) {
            this.delayedCapture = delayedCapture;
            return this;
        }

        public CreatePaymentRequestBuilder metadata(ExternalMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public CreatePaymentRequestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public CreatePaymentRequestBuilder cardholderName(String cardHolderName) {
            this.cardholderName = cardHolderName;
            return this;
        }

        public CreatePaymentRequestBuilder addressLine1(String addressLine1) {
            this.addressLine1 = addressLine1;
            return this;
        }

        public CreatePaymentRequestBuilder addressLine2(String addressLine2) {
            this.addressLine2 = addressLine2;
            return this;
        }

        public CreatePaymentRequestBuilder city(String city) {
            this.city = city;
            return this;
        }

        public CreatePaymentRequestBuilder postcode(String postcode) {
            this.postcode = postcode;
            return this;
        }

        public CreatePaymentRequestBuilder country(String country) {
            this.country = country;
            return this;
        }

        private PrefilledCardholderDetails getPrefilledCardholderDetails() {
            if (cardholderName != null) {
                this.prefilledCardholderDetails = new PrefilledCardholderDetails();
                this.prefilledCardholderDetails.setCardholderName(cardholderName);
            }
            if (addressLine1 != null || addressLine2 != null ||
                    postcode != null || city != null || country != null) {
                if (this.prefilledCardholderDetails == null) {
                    this.prefilledCardholderDetails = new PrefilledCardholderDetails();
                }
                this.prefilledCardholderDetails.setAddress(addressLine1, addressLine2, postcode, city, country);
            }
            return prefilledCardholderDetails;
        }


        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }

    public static CreatePaymentRequestBuilder builder() {
        return new CreatePaymentRequestBuilder();
    }
}
