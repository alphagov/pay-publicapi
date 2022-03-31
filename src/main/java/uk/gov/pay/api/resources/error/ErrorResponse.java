package uk.gov.pay.api.resources.error;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

public class ErrorResponse {
    private ErrorIdentifier errorIdentifier;
    private String message;
    public ErrorResponse(ErrorIdentifier errorIdentifier, String message) {
        this.errorIdentifier = errorIdentifier;
        this.message = message;
    }
}
