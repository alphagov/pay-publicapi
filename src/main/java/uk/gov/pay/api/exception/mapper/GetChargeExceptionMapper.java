package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.GetChargeException;
import uk.gov.pay.api.model.PaymentError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static uk.gov.pay.api.model.PaymentError.Code.GET_PAYMENT_CONNECTOR_ERROR;
import static uk.gov.pay.api.model.PaymentError.Code.GET_PAYMENT_NOT_FOUND_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class GetChargeExceptionMapper implements ExceptionMapper<GetChargeException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetChargeExceptionMapper.class);

    @Override
    public Response toResponse(GetChargeException exception) {

        PaymentError paymentError;
        Response.Status status;

        if (exception.getErrorStatus() == NOT_FOUND.getStatusCode()) {
            paymentError = aPaymentError(GET_PAYMENT_NOT_FOUND_ERROR);
            status = NOT_FOUND;
        } else {
            paymentError = aPaymentError(GET_PAYMENT_CONNECTOR_ERROR);
            status = INTERNAL_SERVER_ERROR;
            LOGGER.error("Connector invalid response was {}.\n Returning http status {} with error body {}", exception.getMessage(), status, paymentError);
        }

        return Response
                .status(status)
                .entity(paymentError)
                .build();
    }
}
