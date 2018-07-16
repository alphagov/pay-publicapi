package uk.gov.pay.api.model.directdebit.agreement;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.google.common.collect.ObjectArrays.concat;
import static java.lang.String.format;

@ApiModel(value = "AgreementError", description = "An Agreement Error response")
@JsonInclude(NON_NULL)
public class AgreementError {

    public enum Code {

        CREATE_AGREEMENT_ACCOUNT_ERROR("P0199", "There is an error with this account. Please contact support"),
        CREATE_AGREEMENT_CONNECTOR_ERROR("P0198", "Downstream system error"),
        CREATE_AGREEMENT_TYPE_ERROR("P0197", "It is not possible to create an agreement of this type"),
        
        GET_AGREEMENT_NOT_FOUND_ERROR("P0200", "Not found"),
        GET_AGREEMENT_CONNECTOR_ERROR("P0298", "Downstream system error"),

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

    public static AgreementError anAgreementError(Code code, Object... parameters) {
        return new AgreementError(code, parameters);
    }

    public static AgreementError anAgreementError(String fieldName, Code code, Object... parameters) {
        return new AgreementError(fieldName, code, parameters);
    }

    private AgreementError(Code code, Object... parameters) {
        this.code = code;
        this.description = format(code.getFormat(), parameters);
    }

    private AgreementError(String fieldName, Code code, Object... parameters) {
        this.field = fieldName;
        this.code = code;
        this.description = format(code.getFormat(), concat(fieldName, parameters));
    }

    @ApiModelProperty(example = "return_url")
    public String getField() {
        return field;
    }

    @ApiModelProperty(example = "P0102")
    public String getCode() {
        return code.value();
    }

    @ApiModelProperty(example = "Invalid attribute value: return_url. Must be a valid url.")
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "AgreementError{" +
                "field=" + field +
                ", code=" + code.value() +
                ", name=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
