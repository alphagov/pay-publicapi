package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.generated.PaymentError;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BadRequestExceptionMapper.class);

    @Override
    public Response toResponse(BadRequestException exception) {

        PaymentError paymentError = exception.getPaymentError();
        LOGGER.debug("Bad Request exception {}", paymentError);

        return Response.status(Status.BAD_REQUEST)
                .entity(exception.getPaymentError())
                .build();
    }
}
