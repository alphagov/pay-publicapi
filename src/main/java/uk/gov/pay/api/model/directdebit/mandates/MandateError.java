package uk.gov.pay.api.model.directdebit.mandates;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.google.common.collect.ObjectArrays.concat;
import static java.lang.String.format;

@Schema(name = "MandateError", description = "A Mandate Error response")
@JsonInclude(NON_NULL)
public class MandateError {

    public enum Code {

        CREATE_MANDATE_ACCOUNT_ERROR("P0199", "There is an error with this account. Please contact support"),
        CREATE_MANDATE_CONNECTOR_ERROR("P0198", "Downstream system error"),
        CREATE_MANDATE_TYPE_ERROR("P0197", "It is not possible to create a mandate of this type"),
        
        GET_MANDATE_NOT_FOUND_ERROR("P0200", "Not found"),
        GET_MANDATE_CONNECTOR_ERROR("P0298", "Downstream system error"),

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

    public static MandateError aMandateError(Code code, Object... parameters) {
        return new MandateError(code, parameters);
    }

    public static MandateError aMandateError(String fieldName, Code code, Object... parameters) {
        return new MandateError(fieldName, code, parameters);
    }

    private MandateError(Code code, Object... parameters) {
        this.code = code;
        this.description = format(code.getFormat(), parameters);
    }

    private MandateError(String fieldName, Code code, Object... parameters) {
        this.field = fieldName;
        this.code = code;
        this.description = format(code.getFormat(), concat(fieldName, parameters));
    }

    @Schema(example = "return_url")
    public String getField() {
        return field;
    }

    @Schema(example = "P0102")
    public String getCode() {
        return code.value();
    }

    @Schema(example = "Invalid attribute value: return_url. Must be a valid url.")
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "MandateError{" +
                "field=" + field +
                ", code=" + code.value() +
                ", name=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
