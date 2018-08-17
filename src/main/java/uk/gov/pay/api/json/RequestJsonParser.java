package uk.gov.pay.api.json;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.CreatePaymentRefundRequest;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.validation.LanguageValidator;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.pay.api.model.CreatePaymentRefundRequest.REFUND_AMOUNT_AVAILABLE;
import static uk.gov.pay.api.model.CreatePaymentRequest.AGREEMENT_ID_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.AMOUNT_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.DESCRIPTION_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.LANGUAGE_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.REFERENCE_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.RETURN_URL_FIELD_NAME;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_MISSING_FIELD_ERROR;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_REFUND_MISSING_FIELD_ERROR;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_REFUND_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

class RequestJsonParser {

    static CreatePaymentRequest parsePaymentRequest(JsonNode paymentRequest) {
        Integer amount = parseInteger(paymentRequest, AMOUNT_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, CREATE_PAYMENT_MISSING_FIELD_ERROR);
        String reference = parseString(paymentRequest, REFERENCE_FIELD_NAME, true);
        String description = parseString(paymentRequest, DESCRIPTION_FIELD_NAME, true);

        CreatePaymentRequest.CreatePaymentRequestBuilder createPaymentRequestBuilder = CreatePaymentRequest.builder()
                .amount(amount)
                .reference(reference)
                .description(description);

        if (paymentRequest.has(LANGUAGE_FIELD_NAME)) {
            String language = parseString(paymentRequest, LANGUAGE_FIELD_NAME, false,
                    aPaymentError(LANGUAGE_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, LanguageValidator.ERROR_MESSAGE));
            createPaymentRequestBuilder.language(language);
        }

        if (paymentRequest.has(AGREEMENT_ID_FIELD_NAME)) {
            String agreementId = parseString(paymentRequest, AGREEMENT_ID_FIELD_NAME, true,
                    aPaymentError(AGREEMENT_ID_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Must be a valid agreement ID"));
            createPaymentRequestBuilder.agreementId(agreementId);
        } else {
            String returnUrl = parseString(paymentRequest, RETURN_URL_FIELD_NAME, true,
                    aPaymentError(RETURN_URL_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Must be a valid URL format"));
            createPaymentRequestBuilder.returnUrl(returnUrl);
        }

        return createPaymentRequestBuilder.build();
    }

    static CreatePaymentRefundRequest parseRefundRequest(JsonNode rootNode) {
        Integer amount = parseInteger(rootNode, AMOUNT_FIELD_NAME, CREATE_PAYMENT_REFUND_VALIDATION_ERROR, CREATE_PAYMENT_REFUND_MISSING_FIELD_ERROR);
        Integer refundAmountAvailable = rootNode.get(REFUND_AMOUNT_AVAILABLE) == null ? null : rootNode.get(REFUND_AMOUNT_AVAILABLE).asInt();
        return new CreatePaymentRefundRequest(amount, refundAmountAvailable);
    }

    private static String parseString(JsonNode node, String fieldName, boolean fieldIsMandatory) {
        return parseString(node, fieldName, fieldIsMandatory, aPaymentError(fieldName, CREATE_PAYMENT_VALIDATION_ERROR,
                "Must be a valid string format"));
    }

    private static String parseString(JsonNode node, String fieldName, boolean fieldIsMandatory, PaymentError formatError) {
        JsonNode fieldNode = node.get(fieldName);
        String fieldValue = getStringValue(fieldNode, formatError);
        if (fieldIsMandatory) {
            check(isNotBlank(fieldValue), aPaymentError(fieldName, CREATE_PAYMENT_MISSING_FIELD_ERROR));
        } else {
            check(isNotBlank(fieldValue), formatError);
        }
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

    private static boolean notNull(JsonNode fieldNode) {
        return fieldNode != null && !fieldNode.isNull();
    }

    private static void check(boolean condition, PaymentError error) {
        if (!condition) {
            throw new BadRequestException(error);
        }
    }
}
