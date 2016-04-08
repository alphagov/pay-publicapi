package uk.gov.pay.api.exception.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.CreateChargeConnectorErrorResponseException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static uk.gov.pay.api.model.PaymentError.Code.P0198;
import static uk.gov.pay.api.model.PaymentError.Code.P0199;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class CreateChargeConnectorErrorResponseExceptionMapper implements ExceptionMapper<CreateChargeConnectorErrorResponseException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateChargeConnectorErrorResponseException.class);

    @Override
    public Response toResponse(CreateChargeConnectorErrorResponseException exception) {

        int errorStatus = exception.getErrorStatus();

        if (errorStatus == NOT_FOUND.getStatusCode()) {
            LOGGER.error("Authorization succeeded but Connector response was Gateway account not found: {}", exception.getErrorBody());

            return Response
                    .status(INTERNAL_SERVER_ERROR)
                    .entity(aPaymentError(P0199, "There is an error with this account. Please contact support"))
                    .build();
        }

        LOGGER.info("Error response from Connector unrecognised. status {}, body {}", errorStatus, exception.getErrorBody());

        return Response
                .status(INTERNAL_SERVER_ERROR)
                .entity(aPaymentError(P0198, "Downstream system error"))
                .build();
    }
}
