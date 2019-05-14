package uk.gov.pay.api.exception.mapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.jersey.validation.JerseyViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.model.PaymentError;

import javax.annotation.Priority;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;

import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_VALIDATION_ERROR;

@Priority(1)
public class ViolationExceptionMapper implements ExceptionMapper<JerseyViolationException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViolationExceptionMapper.class);

    @Override
    public Response toResponse(JerseyViolationException exception) {
        LOGGER.error(exception.getMessage());
        ConstraintViolation<?> firstException = exception.getConstraintViolations().iterator().next();
        String message = firstException.getMessage();
        String fieldName = getApiFieldName(firstException);
        PaymentError paymentError = PaymentError.aPaymentError(fieldName, CREATE_PAYMENT_VALIDATION_ERROR, message);

        return Response.status(422)
                .entity(paymentError)
                .build();
    }

    private String getApiFieldName(ConstraintViolation<?> firstException) {
        Field field = getField(firstException);
        return getJacksonPropertyName(field).orElse(field.getName());
    }

    private Optional<String> getJacksonPropertyName(Field field) {
        return Arrays.stream(field.getAnnotations())
                .filter(annotation -> annotation instanceof JsonProperty)
                .findFirst()
                .map(annotation -> ((JsonProperty) annotation).value());
    }
    
    private Field getField(ConstraintViolation<?> firstException) {
        Class<?> leafBean = firstException.getLeafBean().getClass();
        String fieldName = getFieldNameFromPath(firstException.getPropertyPath());
        try {
            Field f = leafBean.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            LOGGER.error(String.format("Cannot process violation exception. " +
                    "Field %s does not exist or is not public on class %s", fieldName, leafBean.toString()));
            throw new WebApplicationException(e);
        }
    }
    
    private String getFieldNameFromPath(Path path) {
        String[] pathParts = path.toString().split("\\.");
        return pathParts[pathParts.length - 1];
    }
}

