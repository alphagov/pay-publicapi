package uk.gov.pay.api.exception;

import jakarta.ws.rs.core.Response;

public class GetRefundException extends ConnectorResponseErrorException {
    public GetRefundException(Response response) {
        super(response);
    }

    public GetRefundException(GetTransactionException exception) {
        super(exception);
    }
}
