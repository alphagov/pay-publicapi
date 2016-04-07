package uk.gov.pay.api.exception;

public class ValidationException extends RuntimeException {

    private PaymentError paymentError;

    public ValidationException(PaymentError paymentError) {
        this.paymentError = paymentError;
    }

    public PaymentError getPaymentError() {
        return paymentError;
    }
}
