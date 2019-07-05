package uk.gov.pay.api.exception.mapper;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.commons.model.ErrorIdentifier;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_ACCOUNT_ERROR;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_CONNECTOR_ERROR;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_MANDATE_STATE_INVALID;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class CreateChargeExceptionMapper implements ExceptionMapper<CreateChargeException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateChargeExceptionMapper.class);

    @Override
    public Response toResponse(CreateChargeException exception) {

        PaymentError paymentError;
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR_500;

        if (exception.getErrorStatus() == NOT_FOUND.getStatusCode()) {
            paymentError = aPaymentError(CREATE_PAYMENT_ACCOUNT_ERROR);
        } else if (exception.getErrorIdentifier() == ErrorIdentifier.ZERO_AMOUNT_NOT_ALLOWED) {
            statusCode = HttpStatus.UNPROCESSABLE_ENTITY_422;
            paymentError = aPaymentError("amount", CREATE_PAYMENT_VALIDATION_ERROR,
                    "Must be greater than or equal to 1");
        } else if (exception.getErrorIdentifier() == ErrorIdentifier.MANDATE_STATE_INVALID) {
            statusCode = HttpStatus.CONFLICT_409;
            paymentError = aPaymentError(CREATE_PAYMENT_MANDATE_STATE_INVALID);
        } else {
            paymentError = aPaymentError(CREATE_PAYMENT_CONNECTOR_ERROR);
        }

        LOGGER.error("Connector invalid response was {}.\n Returning http status {} with error body {}", 
                exception.getMessage(), INTERNAL_SERVER_ERROR, paymentError);

        return Response
                .status(statusCode)
                .entity(paymentError)
                .build();
    }
}
