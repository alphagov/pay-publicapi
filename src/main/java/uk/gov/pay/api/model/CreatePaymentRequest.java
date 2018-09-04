package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.StringJoiner;

@ApiModel(value = "CreatePaymentRequest", description = "The Payment Request Payload")
public class CreatePaymentRequest {

    public static final String AMOUNT_FIELD_NAME = "amount";
    public static final String RETURN_URL_FIELD_NAME = "return_url";
    public static final String REFERENCE_FIELD_NAME = "reference";
    public static final String DESCRIPTION_FIELD_NAME = "description";
    public static final String AGREEMENT_ID_FIELD_NAME = "agreement_id";
    public static final String LANGUAGE_FIELD_NAME = "language";
    public static final String DELAYED_CAPTURE_FIELD_NAME = "delayed_capture";

    private final int amount;
    private final String returnUrl;
    private final String reference;
    private final String description;
    private final String agreementId;
    private final String language;
    private final Boolean delayedCapture;

    public static class CreatePaymentRequestBuilder {
        private int amount;
        private String returnUrl;
        private String reference;
        private String description;
        private String agreementId;
        private String language;
        private Boolean delayedCapture;

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

        public CreatePaymentRequest build() {
            return new CreatePaymentRequest(this);
        }
    }

    public static CreatePaymentRequestBuilder builder() {
        return new CreatePaymentRequestBuilder();
    }

    private CreatePaymentRequest(CreatePaymentRequestBuilder createPaymentRequestBuilder) {
        this.amount = createPaymentRequestBuilder.amount;
        this.returnUrl = createPaymentRequestBuilder.returnUrl;
        this.reference = createPaymentRequestBuilder.reference;
        this.description = createPaymentRequestBuilder.description;
        this.agreementId = createPaymentRequestBuilder.agreementId;
        this.language = createPaymentRequestBuilder.language;
        this.delayedCapture = createPaymentRequestBuilder.delayedCapture;
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

    public boolean hasReturnUrl() {
        return StringUtils.isNotBlank(returnUrl);
    }

    public boolean hasAgreementId() {
        return StringUtils.isNotBlank(agreementId);
    }

    public boolean hasLanguage() {
        return StringUtils.isNotBlank(language);
    }

    public boolean hasDelayedCapture() {
        return delayedCapture != null;
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
        return joiner.toString();
    }

}
