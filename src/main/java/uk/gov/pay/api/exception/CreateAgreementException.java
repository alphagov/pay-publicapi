package uk.gov.pay.api.exception;
import uk.gov.pay.api.resources.error.ErrorResponse;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

public class CreateAgreementException extends ConnectorResponseErrorException {
    public CreateAgreementException(Response response) {
        super(response);
    }
//    public CreateAgreementException(String message) {
//        super(buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, message));
//    }
//
//    private static Response buildErrorResponse(Response.Status status, String message) {
//        ErrorResponse errorResponse = new ErrorResponse(ErrorIdentifier.GENERIC, message);
//        return Response.status(status).entity(errorResponse).build();//responseWithEntity(status, errorResponse);
//    }
   

}
