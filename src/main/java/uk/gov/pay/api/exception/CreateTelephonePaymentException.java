package uk.gov.pay.api.exception;

import javax.ws.rs.core.Response;

public class CreateTelephonePaymentException extends ConnectorResponseErrorException{

    public CreateTelephonePaymentException(Response response) {
        super(response);
    }
}
