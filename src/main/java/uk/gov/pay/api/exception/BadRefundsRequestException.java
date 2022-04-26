package uk.gov.pay.api.exception;

import uk.gov.pay.api.model.RequestError;

public class BadRefundsRequestException extends RuntimeException {

    private RequestError requestError;

    public BadRefundsRequestException(RequestError requestError) {
        this.requestError = requestError;
    }
    
    public RequestError getRequestError() {
        return requestError;
    }

    @Override
    public String toString() {
        return "BadRefundsRequestException{" +
                "refundError=" + requestError +
                '}';
    }
}
