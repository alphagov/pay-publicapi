package uk.gov.pay.api.auth;

import io.dropwizard.auth.UnauthorizedHandler;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static uk.gov.pay.api.model.Unauthorized401Response.createUnauthorized401Response;

public class JsonUnauthorizedHandler implements UnauthorizedHandler {

    @Override
    public Response buildResponse(String prefix, String realm) {
        // Create an HTTP 401 Unauthorized response with a JSON payload of a human readable error
        return Response.status(UNAUTHORIZED)
                .type(APPLICATION_JSON_TYPE)
                .entity(createUnauthorized401Response("Credentials are required to access this resource"))
                .build();
    }
}