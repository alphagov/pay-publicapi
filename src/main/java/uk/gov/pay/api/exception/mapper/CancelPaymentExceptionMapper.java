package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.CancelPaymentException;
import uk.gov.pay.api.model.PaymentError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.*;
import static uk.gov.pay.api.model.PaymentError.Code.*;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class CancelPaymentExceptionMapper implements ExceptionMapper<CancelPaymentException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CancelPaymentExceptionMapper.class);

    @Override
    public Response toResponse(CancelPaymentException exception) {

        int errorStatus = exception.getErrorStatus();
        PaymentError paymentError;
        Response.Status status;

        if (errorStatus == NOT_FOUND.getStatusCode()) {
            paymentError = aPaymentError(CANCEL_PAYMENT_NOT_FOUND_ERROR);
            status = NOT_FOUND;
        } else if (errorStatus == BAD_REQUEST.getStatusCode()) {
            paymentError = aPaymentError(CANCEL_PAYMENT_CONNECTOR_BAD_REQUEST_ERROR);
            status = BAD_REQUEST;
        } else {
            paymentError = aPaymentError(CANCEL_PAYMENT_CONNECTOR_ERROR);
            status = INTERNAL_SERVER_ERROR;
        }

        LOGGER.error("Connector invalid response was {}.\n Returning http status {} with error body {}", exception.getMessage(), status, paymentError);
        return Response
                .status(status)
                .entity(paymentError)
                .build();
    }
}
