package uk.gov.pay.api.exception;

import uk.gov.pay.api.model.RefundError;

public class BadRefundsRequestException extends RuntimeException {

    private RefundError refundError;

    public BadRefundsRequestException(RefundError refundError) {
        this.refundError = refundError;
    }
    
    public RefundError getRefundError() {
        return refundError;
    }

    @Override
    public String toString() {
        return "BadRefundsRequestException{" +
                "refundError=" + refundError +
                '}';
    }
}
