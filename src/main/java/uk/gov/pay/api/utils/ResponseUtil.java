package uk.gov.pay.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static uk.gov.pay.api.model.BadRequest400Response.createBadRequest400Response;
import static uk.gov.pay.api.model.NotFound404Response.createNotFound404Response;

public class ResponseUtil {

    public static Response notFoundResponse(Logger logger, JsonNode node) {
        String message = nodeMessage(node, "Not found.");
        return messageResponse(logger, message, createNotFound404Response(message), NOT_FOUND);
    }

    public static Response badRequestResponse(Logger logger, JsonNode node) {
        return badRequestResponse(logger, nodeMessage(node, "Bad request."));
    }

    public static Response badRequestResponse(Logger logger, String message) {
        return messageResponse(logger, message, createBadRequest400Response(message), BAD_REQUEST);
    }

    private static Response messageResponse(Logger logger, String internalMessage, Object entity, Response.Status status) {
        logger.error(internalMessage);
        return Response.status(status)
                .entity(entity)
                .build();
    }

    private static String nodeMessage(JsonNode node, String defaultMessage) {
        return node.has("message")
                ? node.get("message").asText()
                : defaultMessage;
    }
}
