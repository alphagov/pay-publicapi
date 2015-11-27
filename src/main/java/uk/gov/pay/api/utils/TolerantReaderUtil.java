package uk.gov.pay.api.utils;

import com.fasterxml.jackson.databind.JsonNode;

public class TolerantReaderUtil {
    public static String tolerantGet(JsonNode node, String key) {
        JsonNode value = node.get(key);
        if (value != null) {
            return value.asText();
        } else {
            return "null";
        }
    }
}
