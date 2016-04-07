package uk.gov.pay.api.json;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.pay.api.exception.PaymentError;
import uk.gov.pay.api.exception.ValidationException;

import javax.annotation.Nonnull;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.pay.api.exception.PaymentError.missingMandatoryAttribute;
import static uk.gov.pay.api.exception.PaymentError.unrecognisedAttributeValue;

class PaymentParser {

    @Nonnull
    static String parseString(JsonNode node, String fieldName) {
        String fieldValue = getStringValue(node, fieldName);
        check(isNotBlank(fieldValue), missingMandatoryAttribute(fieldName));
        return fieldValue;
    }

    @Nonnull
    static int parseInteger(JsonNode node, String fieldName) {
        Integer fieldValue = getIntegerValue(node, fieldName);
        check(fieldValue != null, missingMandatoryAttribute(fieldName));
        return fieldValue;
    }

    static void check(boolean condition, PaymentError error) {
        if (!condition) {
            throw new ValidationException(error);
        }
    }

    private static Integer getIntegerValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = getJsonNode(node, fieldName);
        Integer value = null;
        if (fieldNode != null) {
            if (!fieldNode.isInt()) {
                throw new ValidationException(unrecognisedAttributeValue(fieldName));
            }
            value = fieldNode.intValue();
        }
        return value;
    }

    private static String getStringValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = getJsonNode(node, fieldName);
        String value = null;
        if (fieldNode != null) {
            if (!fieldNode.isTextual()) {
                throw new ValidationException(unrecognisedAttributeValue(fieldName));
            }
            value = fieldNode.asText();
        }
        return value;
    }

    private static JsonNode getJsonNode(JsonNode node, String fieldName) {
        if (!node.has(fieldName)) {
            return null;
        }
        JsonNode fieldNode = node.get(fieldName);
        if (fieldNode.isNull()) {
            return null;
        }
        return fieldNode;
    }
}
