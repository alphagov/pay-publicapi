package uk.gov.pay.api.exception;

import uk.gov.pay.api.model.PaymentError;

public class PaymentValidationException extends RuntimeException {

    private PaymentError paymentError;

    public PaymentValidationException(PaymentError paymentError) {
        this.paymentError = paymentError;
    }

    public PaymentError getPaymentError() {
        return paymentError;
    }

    @Override
    public String toString() {
        return "PaymentValidationException{" +
                "paymentError=" + paymentError +
                '}';
    }
}
