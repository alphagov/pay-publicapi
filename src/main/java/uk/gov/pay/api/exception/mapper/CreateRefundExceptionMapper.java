package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.CreateRefundException;
import uk.gov.pay.api.model.RequestError;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static uk.gov.pay.api.model.RequestError.Code.CREATE_PAYMENT_REFUND_AMOUNT_AVAILABLE_MISMATCH;
import static uk.gov.pay.api.model.RequestError.Code.CREATE_PAYMENT_REFUND_CONNECTOR_ERROR;
import static uk.gov.pay.api.model.RequestError.Code.CREATE_PAYMENT_REFUND_NOT_AVAILABLE;
import static uk.gov.pay.api.model.RequestError.Code.CREATE_PAYMENT_REFUND_NOT_FOUND_ERROR;
import static uk.gov.pay.api.model.RequestError.aRequestError;

public class CreateRefundExceptionMapper implements ExceptionMapper<CreateRefundException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateRefundExceptionMapper.class);

    @Override
    public Response toResponse(CreateRefundException exception) {

        RequestError requestError;
        Response.Status status;

        if (exception.getErrorStatus() == NOT_FOUND.getStatusCode()) {
            requestError = aRequestError(CREATE_PAYMENT_REFUND_NOT_FOUND_ERROR);
            status = NOT_FOUND;

        } else if (exception.getErrorIdentifier() == ErrorIdentifier.REFUND_NOT_AVAILABLE && exception.hasReason()) {
            requestError = aRequestError(CREATE_PAYMENT_REFUND_NOT_AVAILABLE, exception.getReason());
            status = BAD_REQUEST;
        } else if (exception.getErrorIdentifier() == ErrorIdentifier.REFUND_AMOUNT_AVAILABLE_MISMATCH) {
            requestError = aRequestError(CREATE_PAYMENT_REFUND_AMOUNT_AVAILABLE_MISMATCH);
            status = PRECONDITION_FAILED;
        } else {
            requestError = aRequestError(CREATE_PAYMENT_REFUND_CONNECTOR_ERROR);
            status = INTERNAL_SERVER_ERROR;
        }

        if (status == INTERNAL_SERVER_ERROR) {
            LOGGER.info("Connector invalid response was {}.\n Returning http status {} with error body {}", exception.getMessage(), status, requestError);
        }

        return Response
                .status(status)
                .entity(requestError)
                .build();
    }
}
