package uk.gov.pay.api.exception.mapper;

import com.google.common.base.CaseFormat;
import io.dropwizard.jersey.validation.JerseyViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.model.PaymentError;

import javax.annotation.Priority;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_VALIDATION_ERROR;

@Priority(1)
public class ViolationExceptionMapper implements ExceptionMapper<JerseyViolationException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViolationExceptionMapper.class);

    @Override
    public Response toResponse(JerseyViolationException exception) {
        LOGGER.error(exception.getMessage());
        ConstraintViolation<?> firstException = exception.getConstraintViolations().iterator().next();
        String message = firstException.getMessage();
        String fieldName = getApiFieldName(firstException.getPropertyPath());
        PaymentError paymentError = PaymentError.aPaymentError(fieldName, CREATE_PAYMENT_VALIDATION_ERROR, message);

        return Response.status(422)
                .entity(paymentError)
                .build();
    }

    private String getApiFieldName(Path path) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, getFieldNameFromPath(path));
    }
    
    private String getFieldNameFromPath(Path path) {
        String[] pathParts = path.toString().split("\\.");
        return pathParts[pathParts.length - 1];
    }
}

