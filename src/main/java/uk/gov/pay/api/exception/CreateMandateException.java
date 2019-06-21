package uk.gov.pay.api.exception;

import javax.ws.rs.core.Response;

public class CreateMandateException extends ConnectorResponseErrorException {

    public CreateMandateException(Response response) {
        super(response);
    }
}
