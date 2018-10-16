package uk.gov.pay.api.exception;

import uk.gov.pay.api.model.RefundError;

public class RefundsValidationException extends RuntimeException {

    private RefundError refundError;

    public RefundsValidationException(RefundError refundError) {
        this.refundError = refundError;
    }

    public RefundError getRefundError() {
        return refundError;
    }

    @Override
    public String toString() {
        return "RefundsValidationException{" +
                "refundError=" + refundError +
                '}';
    }
}
