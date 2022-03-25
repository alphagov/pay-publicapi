package uk.gov.pay.api.exception.mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.CreateAgreementException;
import uk.gov.pay.api.resources.error.ErrorResponse;
import uk.gov.service.payments.commons.model.ErrorIdentifier;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
public class CreateAgreementExceptionMapper implements ExceptionMapper<CreateAgreementException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateAgreementExceptionMapper.class);

    @Override
    public Response toResponse(CreateAgreementException exception) {
        LOGGER.info(exception.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(ErrorIdentifier.AGREEMENT_NOT_FOUND, exception.getMessage());
        return Response.status(Response.Status.NOT_FOUND)
                .entity(errorResponse)
                .type(APPLICATION_JSON)
                .build();
    }
}
