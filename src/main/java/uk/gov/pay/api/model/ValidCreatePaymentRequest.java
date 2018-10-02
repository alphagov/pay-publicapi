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

    private int amount;
    private String reference;
    @JsonProperty("return_url")
    private String returnUrl;
    private String description;
    private String agreementId;
    private SupportedLanguage language;
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

    public ValidCreatePaymentRequest() {
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public void setAgreementId(String agreementId) {
        this.agreementId = agreementId;
    }

    public void setLanguage(SupportedLanguage language) {
        this.language = language;
    }

    public void setDelayedCapture(Boolean delayedCapture) {
        this.delayedCapture = delayedCapture;
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
