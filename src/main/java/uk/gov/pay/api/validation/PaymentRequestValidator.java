package uk.gov.pay.api.validation;

import uk.gov.pay.api.exception.ValidationException;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.PaymentError;

import static uk.gov.pay.api.model.CreatePaymentRequest.*;
import static uk.gov.pay.api.model.PaymentError.Code.P0101;
import static uk.gov.pay.api.model.PaymentError.Code.P0102;
import static uk.gov.pay.api.model.PaymentError.invalidAttributeValue;

public class PaymentRequestValidator {

    private static final int AMOUNT_MAX_VALUE = 10000000;
    private static final int AMOUNT_MIN_VALUE = 1;

    private static final int REFERENCE_MAX_LENGTH = 255;
    private static final int DESCRIPTION_MAX_LENGTH = 255;

    private static final int URL_MAX_LENGTH = 2000;

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
                invalidAttributeValue(P0101, AMOUNT_FIELD_NAME, "Must be greater than or equal to " + AMOUNT_MIN_VALUE));

        validate(amount <= AMOUNT_MAX_VALUE,
                invalidAttributeValue(P0102, AMOUNT_FIELD_NAME, "Must be less than or equal to " + AMOUNT_MAX_VALUE));
    }

    private void validateReturnUrl(String returnUrl) {
        validate(returnUrl.length() <= URL_MAX_LENGTH,
                invalidAttributeValue(P0102, RETURN_URL_FIELD_NAME, "Must be less than or equal to " + URL_MAX_LENGTH + " characters length"));

        validate(urlValidator.isValid(returnUrl),
                invalidAttributeValue(P0102, RETURN_URL_FIELD_NAME, "Must be a valid URL format"));
    }

    private void validateReference(String reference) {
        validate(reference.length() <= REFERENCE_MAX_LENGTH,
                invalidAttributeValue(P0102, REFERENCE_FIELD_NAME, "Must be less than or equal to " + REFERENCE_MAX_LENGTH + " characters length"));
    }

    private void validateDescription(String description) {
        validate(description.length() <= DESCRIPTION_MAX_LENGTH,
                invalidAttributeValue(P0102, DESCRIPTION_FIELD_NAME, "Must be less than or equal to " + DESCRIPTION_MAX_LENGTH + " characters length"));
    }

    private static void validate(boolean condition, PaymentError error) {
        if (!condition) {
            throw new ValidationException(error);
        }
    }
}
