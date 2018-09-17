package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.google.common.collect.ObjectArrays.concat;
import static java.lang.String.format;

@ApiModel(value = "PaymentError", description = "A Payment Error response")
@JsonInclude(NON_NULL)
public class PaymentError {

    public enum Code {

        CREATE_PAYMENT_ACCOUNT_ERROR("P0199", "There is an error with this account. Please contact support"),
        CREATE_PAYMENT_CONNECTOR_ERROR("P0198", "Downstream system error"),
        CREATE_PAYMENT_PARSING_ERROR("P0197", "Unable to parse JSON"),
        CREATE_PAYMENT_MISSING_FIELD_ERROR("P0101", "Missing mandatory attribute: %s"),
        CREATE_PAYMENT_VALIDATION_ERROR("P0102", "Invalid attribute value: %s. %s"),

        CREATE_PAYMENT_AGREEMENT_TYPE_ERROR("P0140", "Can't collect payment from this type of agreement"),

        GET_PAYMENT_NOT_FOUND_ERROR("P0200", "Not found"),
        GET_PAYMENT_CONNECTOR_ERROR("P0298", "Downstream system error"),

        GET_PAYMENT_EVENTS_NOT_FOUND_ERROR("P0300", "Not found"),
        GET_PAYMENT_EVENTS_CONNECTOR_ERROR("P0398", "Downstream system error"),

        SEARCH_PAYMENTS_VALIDATION_ERROR("P0401", "Invalid parameters: %s. See Public API documentation for the correct data formats"),
        SEARCH_PAYMENTS_NOT_FOUND("P0402", "Page not found"),
        SEARCH_PAYMENTS_CONNECTOR_ERROR("P0498", "Downstream system error"),

        CANCEL_PAYMENT_NOT_FOUND_ERROR("P0500", "Not found"),
        CANCEL_PAYMENT_CONNECTOR_BAD_REQUEST_ERROR("P0501", "Cancellation of payment failed"),
        CANCEL_PAYMENT_CONNECTOR_CONFLICT_ERROR("P0502", "Cancellation of payment failed"),
        CANCEL_PAYMENT_CONNECTOR_ERROR("P0598", "Downstream system error"),

        CAPTURE_PAYMENT_NOT_FOUND_ERROR("P1000", "Not found"),
        CAPTURE_PAYMENT_CONNECTOR_BAD_REQUEST_ERROR("P1001", "Capture of payment failed"),
        CAPTURE_PAYMENT_CONNECTOR_CONFLICT_ERROR("P1003", "Payment cannot be captured"),
        CAPTURE_PAYMENT_CONNECTOR_ERROR("P1098", "Downstream system error"),

        CREATE_PAYMENT_REFUND_CONNECTOR_ERROR("P0698", "Downstream system error"),
        CREATE_PAYMENT_REFUND_PARSING_ERROR("P0697", "Unable to parse JSON"),
        CREATE_PAYMENT_REFUND_NOT_FOUND_ERROR("P0600", "Not found"),
        CREATE_PAYMENT_REFUND_MISSING_FIELD_ERROR("P0601", "Missing mandatory attribute: %s"),
        CREATE_PAYMENT_REFUND_VALIDATION_ERROR("P0602", "Invalid attribute value: %s. %s"),
        CREATE_PAYMENT_REFUND_NOT_AVAILABLE("P0603", "The payment is not available for refund. Payment refund status: %s"),
        CREATE_PAYMENT_REFUND_AMOUNT_AVAILABLE_MISMATCH("P0604", "Refund amount available mismatch."),

        GET_PAYMENT_REFUND_NOT_FOUND_ERROR("P0700", "Not found"),
        GET_PAYMENT_REFUND_CONNECTOR_ERROR("P0798", "Downstream system error"),

        GET_PAYMENT_REFUNDS_NOT_FOUND_ERROR("P0800", "Not found"),
        GET_PAYMENT_REFUNDS_CONNECTOR_ERROR("P0898", "Downstream system error"),

        TOO_MANY_REQUESTS_ERROR("P0900", "Too many requests"),
        REQUEST_DENIED_ERROR("P0920", "Request blocked by security rules. Please consult API documentation for more information.");

        private String value;
        private String format;

        Code(String value, String format) {
            this.value = value;
            this.format = format;
        }

        public String value() {
            return value;
        }

        public String getFormat() {
            return format;
        }
    }

    private String field;
    private final Code code;
    private final String description;

    public static PaymentError aPaymentError(Code code, Object... parameters) {
        return new PaymentError(code, parameters);
    }

    public static PaymentError aPaymentError(String fieldName, Code code, Object... parameters) {
        return new PaymentError(fieldName, code, parameters);
    }

    private PaymentError(Code code, Object... parameters) {
        this.code = code;
        this.description = format(code.getFormat(), parameters);
    }

    private PaymentError(String fieldName, Code code, Object... parameters) {
        this.field = fieldName;
        this.code = code;
        this.description = format(code.getFormat(), concat(fieldName, parameters));
    }

    @ApiModelProperty(example = "amount")
    public String getField() {
        return field;
    }

    @ApiModelProperty(example = "P0102")
    public String getCode() {
        return code.value();
    }

    @ApiModelProperty(example = "Invalid attribute value: amount. Must be less than or equal to 10000000")
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "PaymentError{" +
                "field=" + field +
                ", code=" + code.value() +
                ", name=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
