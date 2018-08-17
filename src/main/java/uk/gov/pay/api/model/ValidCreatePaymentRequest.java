package uk.gov.pay.api.model;

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

    private final int amount;
    private final String reference;
    private final String description;
    private String returnUrl;
    private String agreementId;
    private SupportedLanguage language;

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

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public SupportedLanguage getLanguage() {
        return language;
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
        return joiner.toString();
    }

}
