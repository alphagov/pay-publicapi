package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.RefundsValidationException;
import uk.gov.pay.api.model.RefundError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class RefundsValidationExceptionMapper implements ExceptionMapper<RefundsValidationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RefundsValidationExceptionMapper.class);

    @Override
    public Response toResponse(RefundsValidationException exception) {

        RefundError refundError = exception.getRefundError();
        LOGGER.debug("Refunds Validation exception {}", refundError);

        return Response.status(422)
                .entity(refundError)
                .build();
    }
}
