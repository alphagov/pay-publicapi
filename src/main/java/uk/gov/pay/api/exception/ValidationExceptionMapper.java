package uk.gov.pay.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateChargeConnectorErrorResponseException.class);

    @Override
    public Response toResponse(ValidationException exception) {
        PaymentError paymentError = exception.getPaymentError();
        LOGGER.debug("Validation exception {}", paymentError);
        return paymentError.asResponse();
    }
}
