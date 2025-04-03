package uk.gov.pay.api.exception;

import jakarta.ws.rs.core.Response;

public class CreateRefundException extends ConnectorResponseErrorException {

    public CreateRefundException(Response response) {
        super(response);
    }
}
