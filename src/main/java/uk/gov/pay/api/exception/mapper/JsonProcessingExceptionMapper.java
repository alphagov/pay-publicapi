package uk.gov.pay.api.exception.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import org.apache.http.HttpStatus;
import uk.gov.pay.api.exception.PaymentValidationException;
import uk.gov.pay.api.model.PaymentError;

import javax.annotation.Priority;
import javax.ws.rs.core.Response;

import static uk.gov.pay.api.model.PaymentError.aPaymentError;

@Priority(1)
public class JsonProcessingExceptionMapper extends LoggingExceptionMapper<JsonProcessingException> {
    @Override
    public Response toResponse(JsonProcessingException exception) {
        if (exception.getCause() instanceof PaymentValidationException) {
            PaymentError paymentError = ((PaymentValidationException) exception.getCause()).getPaymentError();
            return Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity(paymentError).build();
        } else if (exception instanceof MismatchedInputException) {
            MismatchedInputException mismatchedInputException = (MismatchedInputException) exception;
            String typeName = isNumeric(mismatchedInputException.getTargetType()) ?
                    "numeric" :
                    mismatchedInputException.getTargetType().getSimpleName();

            String message = String.format("Must be a valid %s format", typeName);
            var paymentError = aPaymentError(mismatchedInputException.getPath().get(0).getFieldName(),
                    PaymentError.Code.CREATE_PAYMENT_VALIDATION_ERROR,
                    message);
            
            return Response.status(HttpStatus.SC_UNPROCESSABLE_ENTITY).entity(paymentError).build();
        } else {
            return super.toResponse(exception);
        }
    }
    
    private boolean isNumeric(Class type) {
        return type == int.class || type == long.class || type == double.class || Number.class.isAssignableFrom(type);
    }
}
