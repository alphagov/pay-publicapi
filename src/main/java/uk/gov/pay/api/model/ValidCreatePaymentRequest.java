package uk.gov.pay.api.model;

import uk.gov.pay.commons.model.SupportedLanguage;

import java.util.Objects;

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

}
