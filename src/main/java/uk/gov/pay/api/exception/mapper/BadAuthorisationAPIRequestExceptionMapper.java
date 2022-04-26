package uk.gov.pay.api.exception.mapper;

import uk.gov.pay.api.exception.BadAuthorisationRequestException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static org.eclipse.jetty.http.HttpStatus.UNPROCESSABLE_ENTITY_422;

public class BadAuthorisationAPIRequestExceptionMapper implements ExceptionMapper<BadAuthorisationRequestException> {
    @Override
    public Response toResponse(BadAuthorisationRequestException exception) {
        return Response.status(UNPROCESSABLE_ENTITY_422)
                .entity(exception.getRequestError())
                .build();
    }
}
