package uk.gov.pay.api.exception;

import javax.ws.rs.core.Response;

public class CancelPaymentException extends ConnectorResponseErrorException {

    public CancelPaymentException(Response response) {
        super(response);
    }
}
