package uk.gov.pay.api.exception;

import javax.ws.rs.core.Response;

class ConnectorResponseErrorException extends RuntimeException {

    private int status;

    ConnectorResponseErrorException(Response response) {
        super(response.toString());
        this.status = response.getStatus();
        response.close();
    }

    ConnectorResponseErrorException(Throwable cause) {
        super(cause);
    }

    public int getErrorStatus() {
        return status;
    }
}
