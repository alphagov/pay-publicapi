package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.ValidationException;
import uk.gov.pay.api.model.PaymentError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationException.class);

    @Override
    public Response toResponse(ValidationException exception) {

        PaymentError paymentError = exception.getPaymentError();
        LOGGER.debug("Validation exception {}", paymentError);

        return Response.status(422)
                .entity(paymentError)
                .build();
    }
}
