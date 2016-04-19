package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.PaymentError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static uk.gov.pay.api.model.PaymentError.Code.P0198;
import static uk.gov.pay.api.model.PaymentError.Code.P0199;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class CreateChargeExceptionMapper implements ExceptionMapper<CreateChargeException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateChargeException.class);

    @Override
    public Response toResponse(CreateChargeException exception) {

        int errorStatus = exception.getErrorStatus();
        PaymentError paymentError;

        if (errorStatus == NOT_FOUND.getStatusCode()) {

            paymentError = aPaymentError(P0199, "There is an error with this account. Please contact support");
            LOGGER.error("Authorization succeeded but Connector response was Gateway account not found: {}.\n Returning {}", exception.getErrorBody(), paymentError);

        } else {
            paymentError = aPaymentError(P0198, "Downstream system error");
            LOGGER.error("Error response from Connector unrecognised. status {}, body {}.\n Returning {}", errorStatus, exception.getErrorBody(), paymentError);
        }

        return Response
                .status(INTERNAL_SERVER_ERROR)
                .entity(paymentError)
                .build();
    }
}
