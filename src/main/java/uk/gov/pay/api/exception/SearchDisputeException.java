package uk.gov.pay.api.exception;

import javax.ws.rs.core.Response;

public class SearchDisputeException extends ConnectorResponseErrorException {

    public SearchDisputeException(Response response) {
        super(response);
    }

    public SearchDisputeException(Throwable cause) {
        super(cause);
    }
}
