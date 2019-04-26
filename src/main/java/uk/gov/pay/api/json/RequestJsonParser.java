package uk.gov.pay.api.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.CreatePaymentRefundRequest;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.PaymentError.Code;
import uk.gov.pay.api.validation.LanguageValidator;
import uk.gov.pay.commons.model.charge.ExternalMetadata;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.http.HttpStatus.SC_UNPROCESSABLE_ENTITY;
import static uk.gov.pay.api.model.CreatePaymentRefundRequest.REFUND_AMOUNT_AVAILABLE;
import static uk.gov.pay.api.model.CreatePaymentRequest.AGREEMENT_ID_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.AMOUNT_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.DELAYED_CAPTURE_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.DESCRIPTION_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.EMAIL_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.LANGUAGE_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.METADATA;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_CITY_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_COUNTRY_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_LINE1_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_LINE2_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_POSTCODE_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_BILLING_ADDRESS_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_CARDHOLDER_DETAILS_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_CARDHOLDER_NAME_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.REFERENCE_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.RETURN_URL_FIELD_NAME;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_METADATA_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_MISSING_FIELD_ERROR;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_REFUND_MISSING_FIELD_ERROR;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_REFUND_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

class RequestJsonParser {

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    static CreatePaymentRefundRequest parseRefundRequest(JsonNode rootNode) {
        Integer amount = validateAndGetAmount(rootNode, CREATE_PAYMENT_REFUND_VALIDATION_ERROR, CREATE_PAYMENT_REFUND_MISSING_FIELD_ERROR);
        Integer refundAmountAvailable = rootNode.get(REFUND_AMOUNT_AVAILABLE) == null ? null : rootNode.get(REFUND_AMOUNT_AVAILABLE).asInt();
        return new CreatePaymentRefundRequest(amount, refundAmountAvailable);
    }

    static CreatePaymentRequest parsePaymentRequest(JsonNode paymentRequest) {

        var builder = CreatePaymentRequest.builder()
                .amount(validateAndGetAmount(paymentRequest, CREATE_PAYMENT_VALIDATION_ERROR, CREATE_PAYMENT_MISSING_FIELD_ERROR))
                .reference(validateAndGetReference(paymentRequest))
                .description(validateAndGetDescription(paymentRequest));

        if (paymentRequest.has(LANGUAGE_FIELD_NAME)) {
            builder.language(validateAndGetLanguage(paymentRequest));
        }

        if (paymentRequest.has(DELAYED_CAPTURE_FIELD_NAME)) {
            builder.delayedCapture(validateAndGetDelayedCapture(paymentRequest));
        }

        if (paymentRequest.has(AGREEMENT_ID_FIELD_NAME)) {
            builder.agreementId(validateAndGetAgreementId(paymentRequest));
        } else {
            builder.returnUrl(validateAndGetReturnUrl(paymentRequest));
        }
        
        if (paymentRequest.has(EMAIL_FIELD_NAME)) {
            String email = getStringValue(paymentRequest.get(EMAIL_FIELD_NAME),
                    aPaymentError(EMAIL_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Field must be a string"));
            builder.email(email);
        }
        
        if (paymentRequest.has(PREFILLED_CARDHOLDER_DETAILS_FIELD_NAME)) {
            JsonNode prefilledNode = paymentRequest.get(PREFILLED_CARDHOLDER_DETAILS_FIELD_NAME);
            if (prefilledNode.has(PREFILLED_CARDHOLDER_NAME_FIELD_NAME)) {
                String cardHolderName = getStringValue(prefilledNode.get(PREFILLED_CARDHOLDER_NAME_FIELD_NAME),
                        aPaymentError(PREFILLED_CARDHOLDER_NAME_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Field must be a string"));
                builder.cardholderName(cardHolderName);
            }
            if (prefilledNode.has(PREFILLED_BILLING_ADDRESS_FIELD_NAME)) {
                JsonNode addressNode = prefilledNode.get(PREFILLED_BILLING_ADDRESS_FIELD_NAME);
                if (addressNode.has(PREFILLED_ADDRESS_LINE1_FIELD_NAME)) {
                    String addressLine1 = getStringValue(addressNode.get(PREFILLED_ADDRESS_LINE1_FIELD_NAME), 
                            aPaymentError(PREFILLED_ADDRESS_LINE1_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Field must be a string"));
                    builder.addressLine1(addressLine1);
                }
                if (addressNode.has(PREFILLED_ADDRESS_LINE2_FIELD_NAME)) {
                    String addressLine1 = getStringValue(addressNode.get(PREFILLED_ADDRESS_LINE2_FIELD_NAME),
                            aPaymentError(PREFILLED_ADDRESS_LINE2_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Field must be a string"));
                    builder.addressLine2(addressLine1);
                }
                if (addressNode.has(PREFILLED_ADDRESS_CITY_FIELD_NAME)) {
                    String addressCity = getStringValue(addressNode.get(PREFILLED_ADDRESS_CITY_FIELD_NAME),
                            aPaymentError(PREFILLED_ADDRESS_CITY_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Field must be a string"));
                    builder.city(addressCity);
                }
                if (addressNode.has(PREFILLED_ADDRESS_POSTCODE_FIELD_NAME)) {
                    String addressPostcode = getStringValue(addressNode.get(PREFILLED_ADDRESS_POSTCODE_FIELD_NAME),
                            aPaymentError(PREFILLED_ADDRESS_POSTCODE_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Field must be a string"));
                    builder.postcode(addressPostcode);
                }
                if (addressNode.has(PREFILLED_ADDRESS_COUNTRY_FIELD_NAME)) {
                    String countryCode = getStringValue(addressNode.get(PREFILLED_ADDRESS_COUNTRY_FIELD_NAME),
                            aPaymentError(PREFILLED_ADDRESS_COUNTRY_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Field must be a string"));
                    builder.country(countryCode);
                }
            }
        }

        if (paymentRequest.has(METADATA))
            builder.metadata(validateAndGetMetadata(paymentRequest));

        return builder.build();
    }

    private static String validateAndGetReturnUrl(JsonNode paymentRequest) {
        return validateAndGetString(
                paymentRequest.get(RETURN_URL_FIELD_NAME),
                aPaymentError(RETURN_URL_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Must be a valid URL format"),
                aPaymentError(RETURN_URL_FIELD_NAME, CREATE_PAYMENT_MISSING_FIELD_ERROR));
    }

    private static String validateAndGetAgreementId(JsonNode paymentRequest) {
        return validateAndGetString(
                paymentRequest.get(AGREEMENT_ID_FIELD_NAME),
                aPaymentError(AGREEMENT_ID_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Must be a valid agreement ID"),
                aPaymentError(AGREEMENT_ID_FIELD_NAME, CREATE_PAYMENT_MISSING_FIELD_ERROR));
    }

    private static String validateAndGetLanguage(JsonNode paymentRequest) {
        return validateAndGetString(
                paymentRequest.get(LANGUAGE_FIELD_NAME),
                aPaymentError(LANGUAGE_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, LanguageValidator.ERROR_MESSAGE),
                aPaymentError(LANGUAGE_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, LanguageValidator.ERROR_MESSAGE));
    }

    private static String validateAndGetDescription(JsonNode paymentRequest) {
        return validateAndGetString(
                paymentRequest.get(DESCRIPTION_FIELD_NAME),
                aPaymentError(DESCRIPTION_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Must be a valid string format"),
                aPaymentError(DESCRIPTION_FIELD_NAME, CREATE_PAYMENT_MISSING_FIELD_ERROR));
    }

    private static String validateAndGetReference(JsonNode paymentRequest) {
        return validateAndGetString(
                paymentRequest.get(REFERENCE_FIELD_NAME),
                aPaymentError(REFERENCE_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Must be a valid string format"),
                aPaymentError(REFERENCE_FIELD_NAME, CREATE_PAYMENT_MISSING_FIELD_ERROR));
    }

    private static String validateAndGetString(JsonNode jsonNode, PaymentError validationError, PaymentError missingError) {
        String value = validateAndGetValue(jsonNode, validationError, missingError, JsonNode::isTextual, JsonNode::asText);
        check(isNotBlank(value), missingError);
        return value;
    }

    private static Boolean validateAndGetDelayedCapture(JsonNode paymentRequest) {
        return validateAndGetValue(
                paymentRequest.get(DELAYED_CAPTURE_FIELD_NAME),
                aPaymentError(DELAYED_CAPTURE_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Must be true or false"),
                aPaymentError(DELAYED_CAPTURE_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, "Must be true or false"),
                JsonNode::isBoolean,
                JsonNode::booleanValue);
    }

    private static Integer validateAndGetAmount(JsonNode paymentRequest, Code validationError, Code missingError) {
        return validateAndGetValue(
                paymentRequest.get(AMOUNT_FIELD_NAME),
                aPaymentError(AMOUNT_FIELD_NAME, validationError, "Must be a valid numeric format"),
                aPaymentError(AMOUNT_FIELD_NAME, missingError),
                JsonNode::isInt,
                JsonNode::intValue);
    }

    private static ExternalMetadata validateAndGetMetadata(JsonNode paymentRequest) {
        Map<String, Object> metadataMap = null;
        try {
            metadataMap = objectMapper.convertValue(paymentRequest.get("metadata"), Map.class);
        } catch (IllegalArgumentException e) {
            PaymentError paymentError = new PaymentError(METADATA, CREATE_PAYMENT_METADATA_VALIDATION_ERROR,
                    "Field [metadata] must be an object of JSON key-value pairs");
            throw new WebApplicationException(Response.status(SC_UNPROCESSABLE_ENTITY).entity(paymentError).build());
        }
        ExternalMetadata metadata = new ExternalMetadata(metadataMap);
        Set<ConstraintViolation<ExternalMetadata>> violations = validator.validate(metadata);
        if (violations.size() > 0) {
            String message = violations.stream().map(v -> v.getMessage()).collect(Collectors.joining(";\n"));
            PaymentError paymentError = new PaymentError(METADATA, CREATE_PAYMENT_METADATA_VALIDATION_ERROR, message);
            throw new WebApplicationException(Response.status(SC_UNPROCESSABLE_ENTITY).entity(paymentError).build());
        }
        return metadata;
    }

    private static <T> T validateAndGetValue(JsonNode jsonNode,
                                             PaymentError validationError,
                                             PaymentError missingError,
                                             Function<JsonNode, Boolean> isExpectedType,
                                             Function<JsonNode, T> valueFromJsonNode) {
        if (jsonNode != null && !jsonNode.isNull()) {
            check(isExpectedType.apply(jsonNode), validationError);
            return valueFromJsonNode.apply(jsonNode);
        }
        throw new BadRequestException(missingError);
    }

    private static void check(boolean condition, PaymentError error) {
        if (!condition) {
            throw new BadRequestException(error);
        }
    }

    private static String getStringValue(JsonNode fieldNode, PaymentError formatError) {
        String fieldValue = null;
        if (notNull(fieldNode)) {
            check(fieldNode.isTextual(), formatError);
            fieldValue = fieldNode.asText();
        }
        return fieldValue;
    }

    private static boolean notNull(JsonNode fieldNode) {
        return fieldNode != null && !fieldNode.isNull();
    }
}
