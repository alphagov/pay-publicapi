package uk.gov.pay.api.json;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.PaymentError;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.pay.api.model.PaymentError.*;

class JsonParser {

    static String parseString(JsonNode node, String fieldName) {
        String fieldValue = getStringValue(node, fieldName, invalidStringFormatAttributeValue(fieldName));
        check(isNotBlank(fieldValue), missingMandatoryAttribute(fieldName));
        return fieldValue;
    }

    static String parseString(JsonNode node, String fieldName, PaymentError parsingError) {
        String fieldValue = getStringValue(node, fieldName, parsingError);
        check(isNotBlank(fieldValue), missingMandatoryAttribute(fieldName));
        return fieldValue;
    }

    static Integer parseInteger(JsonNode node, String fieldName) {
        Integer fieldValue = getIntegerValue(node, fieldName);
        check(fieldValue != null, missingMandatoryAttribute(fieldName));
        return fieldValue;
    }

    private static Integer getIntegerValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        Integer value = null;
        if (isNotNull(fieldNode)) {
            check(fieldNode.isInt(), invalidNumericFormatAttributeValue(fieldName));
            value = fieldNode.intValue();
        }
        return value;
    }

    private static String getStringValue(JsonNode node, String fieldName, PaymentError overrideError) {
        JsonNode fieldNode = node.get(fieldName);
        String value = null;
        if (isNotNull(fieldNode)) {
            check(fieldNode.isTextual(), overrideError);
            value = fieldNode.asText();
        }
        return value;
    }

    private static boolean isNotNull(JsonNode fieldNode) {
        return fieldNode != null && !fieldNode.isNull();
    }

    private static void check(boolean condition, PaymentError error) {
        if (!condition) {
            throw new BadRequestException(error);
        }
    }
}
