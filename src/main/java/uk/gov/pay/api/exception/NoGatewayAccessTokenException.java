package uk.gov.pay.api.exception;

import javax.ws.rs.core.Response;

public class NoGatewayAccessTokenException extends ConnectorResponseErrorException {
    public NoGatewayAccessTokenException(Response response) {
        super(response);
    }
}
