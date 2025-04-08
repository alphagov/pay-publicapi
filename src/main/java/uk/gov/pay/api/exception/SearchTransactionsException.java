package uk.gov.pay.api.exception;

import jakarta.ws.rs.core.Response;

public class SearchTransactionsException extends ConnectorResponseErrorException {

    public SearchTransactionsException(Response response) {
        super(response);
    }

    public SearchTransactionsException(Throwable cause) {
        super(cause);
    }
}
