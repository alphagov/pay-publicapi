package uk.gov.pay.api.exception;

import javax.ws.rs.core.Response;

public class SearchMandatesException extends ConnectorResponseErrorException {

    public SearchMandatesException(Response response) {
        super(response);
    }

    public SearchMandatesException(Throwable cause) {
        super(cause);
    }
}
