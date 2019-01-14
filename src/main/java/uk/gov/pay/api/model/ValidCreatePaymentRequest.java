package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * A create payment request that has been validated successfully
 * Note that this class does not actually do any validation itself:
 * do that yourself before constructing an instance of this object
 **/
public class ValidCreatePaymentRequest {

    @ApiModelProperty(value = "amount in pence", required = true, allowableValues = "range[1, 10000000]", example = "12000")
    private final int amount;
    @ApiModelProperty(value = "payment reference", required = true, example = "12345")
    private final String reference;
    @ApiModelProperty(name = "return_url", value = "service return url", required = true, example = "https://service-name.gov.uk/transactions/12345")
    private String returnUrl;
    @ApiModelProperty(value = "payment description", required = true, example = "New passport application")
    private final String description;
    @ApiModelProperty(name = "agreement_id", value = "ID of the agreement being used to collect the payment", required = false, example = "33890b55-b9ea-4e2f-90fd-77ae0e9009e2")
    private String agreementId;
    @ApiModelProperty(name = "language", value = "ISO-639-1 Alpha-2 code of a supported language to use on the payment pages", required = false, example = "en", allowableValues = "en,cy")
    private SupportedLanguage language;
    @ApiModelProperty(name = "delayed_capture", value = "delayed capture flag", required = false, example = "false" )
    @JsonProperty("delayed_capture")
    private Boolean delayedCapture;

    public ValidCreatePaymentRequest(CreatePaymentRequest createPaymentRequest) {
        amount = Objects.requireNonNull(createPaymentRequest.getAmount());
        reference = Objects.requireNonNull(createPaymentRequest.getReference());
        description = Objects.requireNonNull(createPaymentRequest.getDescription());

        returnUrl = createPaymentRequest.getReturnUrl();
        agreementId = createPaymentRequest.getAgreementId();

        String createPaymentRequestLanguage = createPaymentRequest.getLanguage();
        if (createPaymentRequestLanguage != null) {
            language = SupportedLanguage.fromIso639AlphaTwoCode(createPaymentRequestLanguage);
        }

        delayedCapture = createPaymentRequest.getDelayedCapture();
    }

    public int getAmount() {
        return amount;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public Optional<String> getReturnUrl() {
        return Optional.ofNullable(returnUrl);
    }

    @ApiModelProperty(name = "agreementId", access = "agreementId")
    public Optional<String> getAgreementId() {
        return Optional.ofNullable(agreementId);
    }

    public Optional<SupportedLanguage> getLanguage() {
        return Optional.ofNullable(language);
    }

    public Optional<Boolean> getDelayedCapture() {
        return Optional.ofNullable(delayedCapture);
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
        Optional.ofNullable(language).ifPresent(value -> joiner.add("language: ").add(value.toString()));
        Optional.ofNullable(delayedCapture).ifPresent(value -> joiner.add("delayed_capture: ").add(value.toString()));
        return joiner.toString();
    }

}
