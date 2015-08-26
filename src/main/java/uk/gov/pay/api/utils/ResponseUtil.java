package uk.gov.pay.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;

import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

public class ResponseUtil {

    public static final Joiner COMMA_JOINER = Joiner.on(", ");

    public static Response badRequestResponse(Logger logger, JsonNode node) {
        String message = node.has("message")
                ? node.get("message").asText()
                : "Bad request.";
        return badRequestResponse(logger, message);
    }

    public static Response badRequestResponse(Logger logger, String message) {
        logger.error(message);
        return Response.status(BAD_REQUEST)
                .entity(ImmutableMap.of("message", message))
                .build();
    }

    public static Response fieldsMissingResponse(Logger logger, List<String> missingFields) {
        String message = String.format("Field(s) missing: [%s]", COMMA_JOINER.join(missingFields));
        logger.error(message);
        return Response.status(BAD_REQUEST)
                .entity(ImmutableMap.of("message", message))
                .build();
    }
}
