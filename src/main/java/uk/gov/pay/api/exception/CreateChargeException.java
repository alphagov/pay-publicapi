package uk.gov.pay.api.exception;

import jakarta.ws.rs.core.Response;

public class CreateChargeException extends ConnectorResponseErrorException {

    public CreateChargeException(Response response) {
        super(response);
    }
}
