package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.google.common.collect.ObjectArrays.concat;
import static java.lang.String.format;

@Schema(name = "RefundError", description = "A Refund Error response")
@JsonInclude(NON_NULL)
public class RefundError {

    public enum Code {

        SEARCH_REFUNDS_VALIDATION_ERROR("P1101", "Invalid parameters: %s. See Public API documentation for the correct data formats"),
        SEARCH_REFUNDS_NOT_FOUND("P1100", "Page not found"),
        SEARCH_REFUNDS_CONNECTOR_ERROR("P1898", "Downstream system error");

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

    public static RefundError aRefundError(Code code, Object... parameters) {
        return new RefundError(code, parameters);
    }

    public static RefundError aRefundError(String fieldName, Code code, Object... parameters) {
        return new RefundError(fieldName, code, parameters);
    }

    private RefundError(Code code, Object... parameters) {
        this.code = code;
        this.description = format(code.getFormat(), parameters);
    }

    private RefundError(String fieldName, Code code, Object... parameters) {
        this.field = fieldName;
        this.code = code;
        this.description = format(code.getFormat(), concat(fieldName, parameters));
    }

    @Schema(example = "amount_submitted")
    public String getField() {
        return field;
    }

    @Schema(example = "P0102")
    public String getCode() {
        return code.value();
    }

    @Schema(example = "Invalid attribute value: amountSubmitted. Must be less than or equal to 10000000")
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "RefundError{" +
                "field=" + field +
                ", code=" + code.value() +
                ", name=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
