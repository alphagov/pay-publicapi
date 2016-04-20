package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.google.common.collect.ObjectArrays.concat;
import static java.lang.String.format;

@ApiModel(value = "Payment Error", description = "A Payment Error response")
@JsonInclude(NON_NULL)
public class PaymentError {

    public enum Code {

        CREATE_PAYMENT_ACCOUNT_ERROR("P0199", "There is an error with this account. Please contact support"),
        CREATE_PAYMENT_CONNECTOR_ERROR("P0198", "Downstream system error"),
        CREATE_PAYMENT_PARSING_ERROR("P0100", "Unable to parse JSON"),
        CREATE_PAYMENT_MISSING_FIELD_ERROR("P0101", "Missing mandatory attribute: %s"),
        CREATE_PAYMENT_VALIDATION_ERROR("P0102", "Invalid attribute value: %s. %s"),

        GET_PAYMENT_NOT_FOUND_ERROR("P0200", "Not found"),
        GET_PAYMENT_CONNECTOR_ERROR("P0298", "Downstream system error"),

        GET_PAYMENT_EVENTS_NOT_FOUND_ERROR("P0300", "Not found"),
        GET_PAYMENT_EVENTS_CONNECTOR_ERROR("P0398", "Downstream system error"),

        SEARCH_PAYMENTS_VALIDATION_ERROR("P0401", "Invalid parameters: %s. See Public API documentation for the correct data formats"),
        SEARCH_PAYMENTS_CONNECTOR_ERROR("P0498", "Downstream system error"),

        CANCEL_PAYMENT_NOT_FOUND_ERROR("P0500", "Not found"),
        CANCEL_PAYMENT_CONNECTOR_BAD_REQUEST_ERROR("P0501", "Cancellation of charge failed"),
        CANCEL_PAYMENT_CONNECTOR_ERROR("P0598", "Downstream system error");

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
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
