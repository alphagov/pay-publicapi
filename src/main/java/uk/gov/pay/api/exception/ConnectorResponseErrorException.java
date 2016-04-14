package uk.gov.pay.api.exception;

import javax.ws.rs.core.Response;

class ConnectorResponseErrorException extends RuntimeException {

    private int errorStatus;
    private String errorBody;

    ConnectorResponseErrorException(Response response) {
        this.errorStatus = response.getStatus();
        this.errorBody = response.readEntity(String.class);
    }

    ConnectorResponseErrorException(Throwable cause) {
        super(cause);
    }

    public int getErrorStatus() {
        return errorStatus;
    }

    public String getErrorBody() {
        return errorBody;
    }
}
