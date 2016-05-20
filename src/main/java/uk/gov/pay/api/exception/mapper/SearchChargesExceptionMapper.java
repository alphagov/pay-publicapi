package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.SearchChargesException;
import uk.gov.pay.api.model.PaymentError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static uk.gov.pay.api.model.PaymentError.Code.SEARCH_PAYMENTS_CONNECTOR_ERROR;
import static uk.gov.pay.api.model.PaymentError.Code.SEARCH_PAYMENTS_NOT_FOUND;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class SearchChargesExceptionMapper implements ExceptionMapper<SearchChargesException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchChargesExceptionMapper.class);

    @Override
    public Response toResponse(SearchChargesException exception) {
        if (exception.getErrorStatus() == NOT_FOUND.getStatusCode()) {
            return buildResponse(exception, SEARCH_PAYMENTS_NOT_FOUND, NOT_FOUND);
        }
        return buildResponse(exception, SEARCH_PAYMENTS_CONNECTOR_ERROR, INTERNAL_SERVER_ERROR);
    }

    private Response buildResponse(SearchChargesException exception, PaymentError.Code searchPaymentsConnectorError, Response.Status status) {
        PaymentError paymentError;
        paymentError = aPaymentError(searchPaymentsConnectorError);
        LOGGER.error("Connector response was {}.\n Returning http status {} with error body {}", exception.getMessage(), status, paymentError);
        return Response
                .status(status)
                .entity(paymentError)
                .build();
    }
}
