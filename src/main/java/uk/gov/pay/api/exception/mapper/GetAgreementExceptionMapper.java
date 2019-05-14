package uk.gov.pay.api.exception.mapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.GetAgreementException;
import uk.gov.pay.api.model.directdebit.agreement.AgreementError;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static uk.gov.pay.api.model.directdebit.agreement.AgreementError.Code.GET_AGREEMENT_CONNECTOR_ERROR;
import static uk.gov.pay.api.model.directdebit.agreement.AgreementError.Code.GET_AGREEMENT_NOT_FOUND_ERROR;
import static uk.gov.pay.api.model.directdebit.agreement.AgreementError.anAgreementError;

public class GetAgreementExceptionMapper implements ExceptionMapper<GetAgreementException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetAgreementExceptionMapper.class);

    @Override
    public Response toResponse(GetAgreementException exception) {
        Response.Status status;

        AgreementError paymentError;

        if (exception.getErrorStatus() == NOT_FOUND.getStatusCode()) {
            paymentError = anAgreementError(GET_AGREEMENT_NOT_FOUND_ERROR);
            status = NOT_FOUND;
        } else {
            paymentError = anAgreementError(GET_AGREEMENT_CONNECTOR_ERROR);
            status = INTERNAL_SERVER_ERROR;
            LOGGER.error("Connector invalid response was {}.\n Returning http status {} with error body {}", exception.getMessage(), status, paymentError);
        }

        return Response
                .status(status)
                .entity(paymentError)
                .build();
    }
}
