package uk.gov.pay.api.exception;

public class CreateChargeConnectorErrorResponseException extends RuntimeException {

    private final int errorStatus;
    private String errorBody;

    public CreateChargeConnectorErrorResponseException(int errorStatus, String errorBody) {
        this.errorStatus = errorStatus;
        this.errorBody = errorBody;
    }

    int getErrorStatus() {
        return errorStatus;
    }

    String getErrorBody() {
        return errorBody;
    }
}
