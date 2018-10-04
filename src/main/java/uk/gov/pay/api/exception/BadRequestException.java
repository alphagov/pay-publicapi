package uk.gov.pay.api.exception;

import uk.gov.pay.api.model.generated.PaymentError;

public class BadRequestException extends RuntimeException {

    private PaymentError paymentError;

    public BadRequestException(PaymentError paymentError) {
        this.paymentError = paymentError;
    }

    public PaymentError getPaymentError() {
        return paymentError;
    }

    @Override
    public String toString() {
        return "BadRequestException{" +
                "paymentError=" + paymentError +
                '}';
    }
}
