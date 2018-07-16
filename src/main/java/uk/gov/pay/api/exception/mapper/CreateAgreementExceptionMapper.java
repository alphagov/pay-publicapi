package uk.gov.pay.api.exception.mapper;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.CreateAgreementException;
import uk.gov.pay.api.model.directdebit.agreement.AgreementError;
import uk.gov.pay.api.model.directdebit.agreement.AgreementError.Code;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static uk.gov.pay.api.model.directdebit.agreement.AgreementError.anAgreementError;

public class CreateAgreementExceptionMapper implements ExceptionMapper<CreateAgreementException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateAgreementExceptionMapper.class);
    
    @Override
    public Response toResponse(CreateAgreementException exception) {
        AgreementError agreementError;
        if (exception.getErrorStatus() == NOT_FOUND.getStatusCode()) {
            agreementError = anAgreementError(AgreementError.Code.CREATE_AGREEMENT_ACCOUNT_ERROR);
        } else if (exception.getErrorStatus() == PRECONDITION_FAILED.getStatusCode()) {
            agreementError = anAgreementError(Code.CREATE_AGREEMENT_TYPE_ERROR);
        } else {
            agreementError = anAgreementError(AgreementError.Code.CREATE_AGREEMENT_CONNECTOR_ERROR);
        }

        LOGGER.error("Direct Debit connector invalid response was {}.\n Returning http status {} with error body {}",
                exception.getMessage(), INTERNAL_SERVER_ERROR, agreementError);

        return Response
                .status(INTERNAL_SERVER_ERROR)
                .entity(agreementError)
                .build();
    }
}
