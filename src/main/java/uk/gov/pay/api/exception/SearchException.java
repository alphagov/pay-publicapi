package uk.gov.pay.api.exception;

import javax.ws.rs.core.Response;

public class SearchException extends ConnectorResponseErrorException {

    public SearchException(Response response) {
        super(response);
    }

    public SearchException(Throwable cause) {
        super(cause);
    }
}
