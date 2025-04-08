package uk.gov.pay.api.exception;

import jakarta.ws.rs.core.Response;

public class SearchDisputesException extends ConnectorResponseErrorException {

    public SearchDisputesException(Response response) {
        super(response);
    }

    public SearchDisputesException(Throwable cause) {
        super(cause);
    }
}
