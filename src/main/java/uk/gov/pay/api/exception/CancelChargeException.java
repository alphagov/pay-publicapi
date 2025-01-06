package uk.gov.pay.api.exception;

import jakarta.ws.rs.core.Response;

public class CancelChargeException extends ConnectorResponseErrorException {

    public CancelChargeException(Response response) {
        super(response);
    }
}
