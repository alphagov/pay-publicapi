package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.CreateMandateException;
import uk.gov.pay.api.model.directdebit.mandates.MandateError;
import uk.gov.pay.api.model.directdebit.mandates.MandateError.Code;
import uk.gov.pay.commons.model.ErrorIdentifier;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

public class CreateAgreementExceptionMapper implements ExceptionMapper<CreateMandateException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateAgreementExceptionMapper.class);
    
    @Override
    public Response toResponse(CreateMandateException exception) {
        MandateError mandateError;
        if (exception.getErrorStatus() == NOT_FOUND.getStatusCode()) {
            mandateError = MandateError.aMandateError(MandateError.Code.CREATE_MANDATE_ACCOUNT_ERROR);
        } else if (exception.getErrorIdentifier() == ErrorIdentifier.GO_CARDLESS_ACCOUNT_NOT_LINKED) {
            mandateError = MandateError.aMandateError(Code.CREATE_MANDATE_ACCOUNT_ERROR);
        }
        else {
            mandateError = MandateError.aMandateError(MandateError.Code.CREATE_MANDATE_CONNECTOR_ERROR);
        }

        LOGGER.info("Direct Debit connector invalid response was {}.\n Returning http status {} with error body {}",
                exception.getMessage(), INTERNAL_SERVER_ERROR, mandateError);

        return Response
                .status(INTERNAL_SERVER_ERROR)
                .entity(mandateError)
                .build();
    }
}
