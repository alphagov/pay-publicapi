package uk.gov.pay.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.*;

public class CreateChargeConnectorErrorResponseExceptionMapper implements ExceptionMapper<CreateChargeConnectorErrorResponseException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateChargeConnectorErrorResponseException.class);

    @Override
    public Response toResponse(CreateChargeConnectorErrorResponseException exception) {

        int errorStatus = exception.getErrorStatus();

        if (errorStatus == NOT_FOUND.getStatusCode()) {
            LOGGER.warn("Authorization succeeded but Gateway account was not found in Connector", errorStatus);
            return Response.serverError()
                    .entity("There is an error with this account. Please contact support")
                    .build();

        }

        LOGGER.info("Error response from Connector unrecognised. status {}, body {}", errorStatus, exception.getErrorBody());
        return Response.serverError().entity("Downstream system error").build();
    }
}
