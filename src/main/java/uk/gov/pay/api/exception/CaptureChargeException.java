package uk.gov.pay.api.exception;

import jakarta.ws.rs.core.Response;

public class CaptureChargeException extends ConnectorResponseErrorException {

    public CaptureChargeException(Response response) {
        super(response);
    }

}
