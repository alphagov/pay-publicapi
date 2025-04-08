package uk.gov.pay.api.exception;
import jakarta.ws.rs.core.Response;

public class CreateAgreementException extends ConnectorResponseErrorException {
    public CreateAgreementException(Response response) {
        super(response);
    }
}
