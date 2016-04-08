package uk.gov.pay.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.model.PaymentErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static uk.gov.pay.api.exception.CreateChargeConnectorErrorResponseExceptionMapper.CreateChargeErrorResponse.P0198;
import static uk.gov.pay.api.exception.CreateChargeConnectorErrorResponseExceptionMapper.CreateChargeErrorResponse.P0199;

public class CreateChargeConnectorErrorResponseExceptionMapper implements ExceptionMapper<CreateChargeConnectorErrorResponseException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateChargeConnectorErrorResponseException.class);

    @Override
    public Response toResponse(CreateChargeConnectorErrorResponseException exception) {

        int errorStatus = exception.getErrorStatus();

        if (errorStatus == NOT_FOUND.getStatusCode()) {
            LOGGER.error("Authorization succeeded but Gateway account was not found in Connector", errorStatus);
            return P0199.response();

        }

        LOGGER.info("Error response from Connector unrecognised. status {}, body {}", errorStatus, exception.getErrorBody());
        return P0198.response();
    }

    enum CreateChargeErrorResponse {

        P0199(INTERNAL_SERVER_ERROR, "There is an error with this account. Please contact support"),
        P0198(INTERNAL_SERVER_ERROR, "Downstream system error");

        private final Response.Status status;
        private final String message;

        CreateChargeErrorResponse(Response.Status status, String message) {
            this.status = status;
            this.message = message;
        }

        public Response response() {
            return Response.status(status)
                    .entity(new PaymentErrorResponse(name(), message))
                    .build();
        }
    }
}
