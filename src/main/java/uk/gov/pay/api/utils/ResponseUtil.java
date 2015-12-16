package uk.gov.pay.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;

import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.*;

public class ResponseUtil {

    public static final Joiner COMMA_JOINER = Joiner.on(", ");

    public static Response notFoundResponse(Logger logger, JsonNode node) {
        return messageResponse(logger, nodeMessage(node, "Not found."), NOT_FOUND);
    }

    public static Response badRequestResponse(Logger logger, JsonNode node) {
        return badRequestResponse(logger, nodeMessage(node, "Bad request."));
    }

    public static Response badRequestResponse(Logger logger, String message) {
        return messageResponse(logger, message, BAD_REQUEST);
    }

    private static Response messageResponse(Logger logger, String message, Response.Status status) {
        return messageResponse(logger, message, message, status);
    }

    private static Response messageResponse(Logger logger, String internalMessage, String externalMessage, Response.Status status) {
        logger.error(internalMessage);
        return Response.status(status)
                .entity(ImmutableMap.of("message", externalMessage))
                .build();
    }

    private static String nodeMessage(JsonNode node, String defaultMessage) {
        return node.has("message")
                ? node.get("message").asText()
                : defaultMessage;
    }
}
