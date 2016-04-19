package uk.gov.pay.api.validation;

import uk.gov.pay.api.exception.ValidationException;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.PaymentError;

import static java.lang.String.format;
import static uk.gov.pay.api.model.CreatePaymentRequest.*;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;
import static uk.gov.pay.api.validation.MaxLengthValidator.isValid;

public class PaymentRequestValidator {

    private static final String CONSTRAINT_GREATER_THAN_MESSAGE_INT_TEMPLATE = "Must be greater than or equal to %d";
    private static final String CONSTRAINT_LESS_THAN_MESSAGE_INT_TEMPLATE = "Must be less than or equal to %d";
    private static final String CONSTRAINT_MESSAGE_STRING_TEMPLATE = "Must be less than or equal to %d characters length";
    private static final String URL_FORMAT_MESSAGE = "Must be a valid URL format";

    private static final int AMOUNT_MAX_VALUE = 10000000;
    private static final int AMOUNT_MIN_VALUE = 1;
    private static final int DESCRIPTION_MAX_LENGTH = 255;
    private static final int URL_MAX_LENGTH = 2000;
    static final int REFERENCE_MAX_LENGTH = 255;

    private URLValidator urlValidator;

    public PaymentRequestValidator(URLValidator urlValidator) {
        this.urlValidator = urlValidator;
    }

    public void validate(CreatePaymentRequest paymentRequest) {
        validateAmount(paymentRequest.getAmount());
        validateReturnUrl(paymentRequest.getReturnUrl());
        validateReference(paymentRequest.getReference());
        validateDescription(paymentRequest.getDescription());
    }

    private void validateAmount(int amount) {
        validate(amount >= AMOUNT_MIN_VALUE,
                aPaymentError(AMOUNT_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_GREATER_THAN_MESSAGE_INT_TEMPLATE, AMOUNT_MIN_VALUE)));

        validate(amount <= AMOUNT_MAX_VALUE,
                aPaymentError(AMOUNT_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_LESS_THAN_MESSAGE_INT_TEMPLATE, AMOUNT_MAX_VALUE)));
    }

    private void validateReturnUrl(String returnUrl) {
        validate(isValid(returnUrl, URL_MAX_LENGTH),
                aPaymentError(RETURN_URL_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, URL_MAX_LENGTH)));

        validate(urlValidator.isValid(returnUrl),
                aPaymentError(RETURN_URL_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, URL_FORMAT_MESSAGE));
    }

    private void validateReference(String reference) {
        validate(isValid(reference, REFERENCE_MAX_LENGTH),
                aPaymentError(REFERENCE_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, REFERENCE_MAX_LENGTH)));
    }

    private void validateDescription(String description) {
        validate(isValid(description, DESCRIPTION_MAX_LENGTH),
                aPaymentError(DESCRIPTION_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, DESCRIPTION_MAX_LENGTH)));
    }

    private static void validate(boolean condition, PaymentError error) {
        if (!condition) {
            throw new ValidationException(error);
        }
    }
}
