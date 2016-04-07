package uk.gov.pay.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static uk.gov.pay.api.exception.PaymentError.ErrorCode.P0198;
import static uk.gov.pay.api.exception.PaymentError.ErrorCode.P0199;
import static uk.gov.pay.api.exception.PaymentError.serverError;

public class CreateChargeConnectorErrorResponseExceptionMapper implements ExceptionMapper<CreateChargeConnectorErrorResponseException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateChargeConnectorErrorResponseException.class);

    @Override
    public Response toResponse(CreateChargeConnectorErrorResponseException exception) {

        int errorStatus = exception.getErrorStatus();

        if (errorStatus == NOT_FOUND.getStatusCode()) {
            LOGGER.error("Authorization succeeded but Connector response was Gateway account not found: {}", exception.getErrorBody());
            return P0199.response();

            LOGGER.warn("Authorization succeeded but Connector response Gateway account was not found (404 Not Found)");

            return serverError(P0199, "There is an error with this account. Please contact support")
                    .asResponse();
        }

        LOGGER.info("Error response from Connector unrecognised. status {}, body {}", errorStatus, exception.getErrorBody());

        return serverError(P0198, "Downstream system error")
                .asResponse();
    }
}
