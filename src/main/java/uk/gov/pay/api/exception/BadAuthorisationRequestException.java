package uk.gov.pay.api.exception;

import uk.gov.pay.api.model.RequestError;

public class BadAuthorisationRequestException extends RuntimeException {

    private RequestError requestError;

    public BadAuthorisationRequestException(RequestError requestError) {
        this.requestError = requestError;
    }

    public RequestError getRequestError() {
        return requestError;
    }

    @Override
    public String toString() {
        return "BadAuthorisationRequestException{" +
                "paymentError=" + requestError +
                '}';
    }
}
