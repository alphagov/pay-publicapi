package uk.gov.pay.api.exception;

import jakarta.ws.rs.core.Response;

public class SearchPaymentsException extends ConnectorResponseErrorException {

    public SearchPaymentsException(Response response) {
        super(response);
    }

    public SearchPaymentsException(Throwable cause) {
        super(cause);
    }
}
