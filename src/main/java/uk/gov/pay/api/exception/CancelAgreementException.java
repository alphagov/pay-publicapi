package uk.gov.pay.api.exception;

import jakarta.ws.rs.core.Response;

public class CancelAgreementException extends ConnectorResponseErrorException {

    public CancelAgreementException(Response response) {
        super(response);
    }

}
