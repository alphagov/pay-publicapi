package uk.gov.pay.api.validation;

import uk.gov.pay.api.exception.ValidationException;
import uk.gov.pay.api.model.generated.CreatePaymentRefundRequest;
import uk.gov.pay.api.model.generated.PaymentError;

import static java.lang.String.format;
import static uk.gov.pay.api.model.CreatePaymentRequest.AMOUNT_FIELD_NAME;
import static uk.gov.pay.api.model.PaymentErrorBuilder.aPaymentError;
import static uk.gov.pay.api.model.PaymentErrorCodes.CREATE_PAYMENT_REFUND_VALIDATION_ERROR;
import static uk.gov.pay.api.validation.PaymentRequestValidator.AMOUNT_MAX_VALUE;
import static uk.gov.pay.api.validation.PaymentRequestValidator.AMOUNT_MIN_VALUE;
import static uk.gov.pay.api.validation.PaymentRequestValidator.CONSTRAINT_GREATER_THAN_MESSAGE_INT_TEMPLATE;
import static uk.gov.pay.api.validation.PaymentRequestValidator.CONSTRAINT_LESS_THAN_MESSAGE_INT_TEMPLATE;

public class PaymentRefundRequestValidator {

    public void validate(CreatePaymentRefundRequest paymentRefundRequest) {
        validateAmount(paymentRefundRequest.getAmount());
    }

    private void validateAmount(int amount) {
        validate(amount >= AMOUNT_MIN_VALUE,
                aPaymentError(AMOUNT_FIELD_NAME, CREATE_PAYMENT_REFUND_VALIDATION_ERROR, format(CONSTRAINT_GREATER_THAN_MESSAGE_INT_TEMPLATE, AMOUNT_MIN_VALUE)));

        validate(amount <= AMOUNT_MAX_VALUE,
                aPaymentError(AMOUNT_FIELD_NAME, CREATE_PAYMENT_REFUND_VALIDATION_ERROR, format(CONSTRAINT_LESS_THAN_MESSAGE_INT_TEMPLATE, AMOUNT_MAX_VALUE)));
    }

    private static void validate(boolean condition, PaymentError error) {
        if (!condition) {
            throw new ValidationException(error);
        }
    }
}
