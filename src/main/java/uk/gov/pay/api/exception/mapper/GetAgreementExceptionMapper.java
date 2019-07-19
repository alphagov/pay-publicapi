package uk.gov.pay.api.exception.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.GetMandateException;
import uk.gov.pay.api.model.directdebit.mandates.MandateError;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static uk.gov.pay.api.model.directdebit.mandates.MandateError.Code.GET_MANDATE_CONNECTOR_ERROR;
import static uk.gov.pay.api.model.directdebit.mandates.MandateError.Code.GET_MANDATE_NOT_FOUND_ERROR;
import static uk.gov.pay.api.model.directdebit.mandates.MandateError.aMandateError;

public class GetAgreementExceptionMapper implements ExceptionMapper<GetMandateException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetAgreementExceptionMapper.class);

    @Override
    public Response toResponse(GetMandateException exception) {
        Response.Status status;

        MandateError paymentError;

        if (exception.getErrorStatus() == NOT_FOUND.getStatusCode()) {
            paymentError = MandateError.aMandateError(GET_MANDATE_NOT_FOUND_ERROR);
            status = NOT_FOUND;
        } else {
            paymentError = MandateError.aMandateError(GET_MANDATE_CONNECTOR_ERROR);
            status = INTERNAL_SERVER_ERROR;
            LOGGER.error("Connector invalid response was {}.\n Returning http status {} with error body {}", exception.getMessage(), status, paymentError);
        }

        return Response
                .status(status)
                .entity(paymentError)
                .build();
    }
}
