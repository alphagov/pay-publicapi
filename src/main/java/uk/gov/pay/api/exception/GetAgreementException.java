package uk.gov.pay.api.exception;

import jakarta.ws.rs.core.Response;

public class GetAgreementException extends ConnectorResponseErrorException {

    public GetAgreementException(Response response) {
        super(response);
    }
}
