package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.PaymentValidationException;
import uk.gov.pay.api.model.PaymentError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class PaymentValidationExceptionMapper implements ExceptionMapper<PaymentValidationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentValidationExceptionMapper.class);

    @Override
    public Response toResponse(PaymentValidationException exception) {

        PaymentError paymentError = exception.getPaymentError();
        LOGGER.debug("Payment Validation exception {}", paymentError);

        return Response.status(422)
                .entity(paymentError)
                .build();
    }
}
