package uk.gov.pay.api.exception;

import uk.gov.pay.api.model.PaymentError;

public class ValidationException extends RuntimeException {

    private PaymentError paymentError;

    public ValidationException(PaymentError paymentError) {
        this.paymentError = paymentError;
    }

    public PaymentError getPaymentError() {
        return paymentError;
    }

    @Override
    public String toString() {
        return "ValidationException{" +
                "paymentError=" + paymentError +
                '}';
    }
}
