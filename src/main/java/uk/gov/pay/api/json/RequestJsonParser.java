package uk.gov.pay.api.json;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.CreatePaymentRefundRequest;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.PaymentError;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.pay.api.model.CreatePaymentRefundRequest.REFUND_AMOUNT_AVAILABLE;
import static uk.gov.pay.api.model.CreatePaymentRequest.*;
import static uk.gov.pay.api.model.PaymentError.Code.*;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

class RequestJsonParser {

    static CreatePaymentRequest paymentRequestValueOf(JsonNode rootNode) {

        Integer amount = parseInteger(rootNode, AMOUNT_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, CREATE_PAYMENT_MISSING_FIELD_ERROR);
        String reference = parseString(rootNode, REFERENCE_FIELD_NAME);
        String description = parseString(rootNode, DESCRIPTION_FIELD_NAME);
        String returnUrl = parseString(rootNode, RETURN_URL_FIELD_NAME, aPaymentError(RETURN_URL_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Must be a valid URL format"));

        String dynamic3ds = parseOptionalBooleanAsString(rootNode, DYNAMIC_3D_SECURE_FIELD_NAME,
                aPaymentError(DYNAMIC_3D_SECURE_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Must be a valid string format")).orElse(null);

        return new CreatePaymentRequest(amount, returnUrl, reference, description, dynamic3ds);
    }

    static CreatePaymentRefundRequest paymentRefundRequestValueOf(JsonNode rootNode) {
        Integer amount = parseInteger(rootNode, AMOUNT_FIELD_NAME, CREATE_PAYMENT_REFUND_VALIDATION_ERROR, CREATE_PAYMENT_REFUND_MISSING_FIELD_ERROR);
        Integer refundAmountAvailable = rootNode.get(REFUND_AMOUNT_AVAILABLE) == null ? null : rootNode.get(REFUND_AMOUNT_AVAILABLE).asInt();
        return new CreatePaymentRefundRequest(amount, refundAmountAvailable);
    }

    private static Optional<String> parseOptionalBooleanAsString(JsonNode node, String fieldName, PaymentError formatError) {
        JsonNode fieldNode = node.get(fieldName);
        String fieldValue = getBooleanValueAsString(fieldNode, formatError);
        return Optional.ofNullable(fieldValue);
    }

    private static String parseString(JsonNode node, String fieldName) {
        return parseString(node, fieldName, aPaymentError(fieldName, CREATE_PAYMENT_VALIDATION_ERROR, "Must be a valid string format"));
    }

    private static String parseString(JsonNode node, String fieldName, PaymentError formatError) {
        JsonNode fieldNode = node.get(fieldName);
        String fieldValue = getStringValue(fieldNode, formatError);
        check(isNotBlank(fieldValue), aPaymentError(fieldName, CREATE_PAYMENT_MISSING_FIELD_ERROR));
        return fieldValue;
    }

    private static Integer parseInteger(JsonNode node, String fieldName, PaymentError.Code validationErrorCode, PaymentError.Code missingErrorCode) {
        JsonNode fieldNode = node.get(fieldName);
        Integer fieldValue = getIntegerValue(fieldNode, aPaymentError(fieldName, validationErrorCode, "Must be a valid numeric format"));
        check(fieldValue != null, aPaymentError(fieldName, missingErrorCode));
        return fieldValue;
    }

    private static String getStringValue(JsonNode fieldNode, PaymentError formatError) {
        String fieldValue = null;
        if (notNull(fieldNode)) {
            check(fieldNode.isTextual(), formatError);
            fieldValue = fieldNode.asText();
        }
        return fieldValue;
    }

    private static Integer getIntegerValue(JsonNode fieldNode, PaymentError formatError) {
        Integer fieldValue = null;
        if (notNull(fieldNode)) {
            check(fieldNode.isInt(), formatError);
            fieldValue = fieldNode.intValue();
        }
        return fieldValue;
    }

    private static String getBooleanValueAsString(JsonNode fieldNode, PaymentError formatError) {
        String fieldValue = null;
        if (notNull(fieldNode)) {
            check(fieldNode.isBoolean(), formatError);
            fieldValue = fieldNode.asText();
        }
        return fieldValue;
    }

    private static boolean notNull(JsonNode fieldNode) {
        return fieldNode != null && !fieldNode.isNull();
    }

    private static void check(boolean condition, PaymentError error) {
        if (!condition) {
            throw new BadRequestException(error);
        }
    }
}
