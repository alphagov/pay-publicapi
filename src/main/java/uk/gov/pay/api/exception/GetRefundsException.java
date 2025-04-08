package uk.gov.pay.api.exception;

import jakarta.ws.rs.core.Response;

public class GetRefundsException extends ConnectorResponseErrorException {
    public GetRefundsException(Response response) {
        super(response);
    }
}
