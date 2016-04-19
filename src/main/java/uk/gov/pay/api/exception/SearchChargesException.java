package uk.gov.pay.api.exception;

import javax.ws.rs.core.Response;

public class SearchChargesException extends ConnectorResponseErrorException {

    public SearchChargesException(Response response) {
        super(response);
    }

    public SearchChargesException(Throwable cause) {
        super(cause);
    }
}
