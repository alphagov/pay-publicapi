package uk.gov.pay.api.validation;

import uk.gov.pay.api.exception.PaymentValidationException;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.PaymentError;

import static java.lang.String.format;
import static uk.gov.pay.api.model.CreatePaymentRequest.AGREEMENT_ID_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.AMOUNT_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.DESCRIPTION_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.LANGUAGE_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.REFERENCE_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.RETURN_URL_FIELD_NAME;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class PaymentRequestValidator {

    static final String CONSTRAINT_GREATER_THAN_MESSAGE_INT_TEMPLATE = "Must be greater than or equal to %d";
    static final String CONSTRAINT_LESS_THAN_MESSAGE_INT_TEMPLATE = "Must be less than or equal to %d";
    private static final String CONSTRAINT_MESSAGE_STRING_TEMPLATE = "Must be less than or equal to %d characters length";
    private static final String URL_FORMAT_MESSAGE = "Must be a valid URL format";

    static final int AMOUNT_MAX_VALUE = 10000000;
    static final int AMOUNT_MIN_VALUE = 1;

    static final int DESCRIPTION_MAX_LENGTH = 255;
    static final int URL_MAX_LENGTH = 2000;
    static final int REFERENCE_MAX_LENGTH = 255;
    static final int EMAIL_MAX_LENGTH = 254;
    static final int CARD_BRAND_MAX_LENGTH = 20;
    static final int AGREEMENT_ID_MAX_LENGTH = 26;

    private URLValidator urlValidator;

    public PaymentRequestValidator(URLValidator urlValidator) {
        this.urlValidator = urlValidator;
    }

    public void validate(CreatePaymentRequest paymentRequest) {
        if (paymentRequest.hasAgreementId()) {
            validateAgreementId(paymentRequest.getAgreementId());
        }

        if (paymentRequest.hasReturnUrl()) {
            validateReturnUrl(paymentRequest.getReturnUrl());
        }

        if (paymentRequest.hasLanguage()) {
            validateLanguage(paymentRequest.getLanguage());
        }

        validateAmount(paymentRequest.getAmount());
        validateReference(paymentRequest.getReference());
        validateDescription(paymentRequest.getDescription());
    }

    private void validateAgreementId(String agreementId) {
        validate(MaxLengthValidator.isValid(agreementId, AGREEMENT_ID_MAX_LENGTH),
                aPaymentError(AGREEMENT_ID_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, AGREEMENT_ID_MAX_LENGTH)));
    }

    private void validateAmount(int amount) {
        validate(amount >= AMOUNT_MIN_VALUE,
                aPaymentError(AMOUNT_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_GREATER_THAN_MESSAGE_INT_TEMPLATE, AMOUNT_MIN_VALUE)));

        validate(amount <= AMOUNT_MAX_VALUE,
                aPaymentError(AMOUNT_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_LESS_THAN_MESSAGE_INT_TEMPLATE, AMOUNT_MAX_VALUE)));
    }

    private void validateReturnUrl(String returnUrl) {
        validate(MaxLengthValidator.isValid(returnUrl, URL_MAX_LENGTH),
                aPaymentError(RETURN_URL_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, URL_MAX_LENGTH)));

        validate(urlValidator.isValid(returnUrl),
                aPaymentError(RETURN_URL_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, URL_FORMAT_MESSAGE));
    }

    private void validateReference(String reference) {
        validate(MaxLengthValidator.isValid(reference, REFERENCE_MAX_LENGTH),
                aPaymentError(REFERENCE_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, REFERENCE_MAX_LENGTH)));
    }

    private void validateDescription(String description) {
        validate(MaxLengthValidator.isValid(description, DESCRIPTION_MAX_LENGTH),
                aPaymentError(DESCRIPTION_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, DESCRIPTION_MAX_LENGTH)));
    }

    private void validateLanguage(String language) {
        validate(LanguageValidator.isValid(language),
                aPaymentError(LANGUAGE_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, LanguageValidator.ERROR_MESSAGE));
    }

    private static void validate(boolean condition, PaymentError error) {
        if (!condition) {
            throw new PaymentValidationException(error);
        }
    }

}
