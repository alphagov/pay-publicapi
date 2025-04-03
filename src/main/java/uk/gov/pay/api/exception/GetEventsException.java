package uk.gov.pay.api.exception;

import jakarta.ws.rs.core.Response;

public class GetEventsException extends ConnectorResponseErrorException {

    public GetEventsException(Response response) {
        super(response);
    }
}
