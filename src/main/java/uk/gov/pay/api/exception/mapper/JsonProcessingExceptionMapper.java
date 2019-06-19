package uk.gov.pay.api.exception.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import org.apache.http.HttpStatus;
import uk.gov.pay.api.exception.PaymentValidationException;
import uk.gov.pay.api.model.PaymentError;

import javax.annotation.Priority;
import javax.ws.rs.core.Response;

@Priority(1)
public class JsonProcessingExceptionMapper extends LoggingExceptionMapper<JsonProcessingException> {
    @Override
    public Response toResponse(JsonProcessingException exception) {
        if (exception.getCause() instanceof PaymentValidationException) {
            PaymentError paymentError = ((PaymentValidationException) exception.getCause()).getPaymentError();
            return Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity(paymentError).build();
        } else {
            return super.toResponse(exception);
        }
    }
}
