package uk.gov.pay.api.exception;

import javax.ws.rs.core.Response;

public class GetMandateException extends ConnectorResponseErrorException {

    public GetMandateException(Response response) {
        super(response);
    }
}
