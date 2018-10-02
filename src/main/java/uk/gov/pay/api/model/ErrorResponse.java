package uk.gov.pay.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.lang.String.format;

@ApiModel(value = "ErrorResponse", description = "An error response")
@JsonInclude(NON_NULL)
public class ErrorResponse {

    public enum Code {

        TOO_MANY_REQUESTS_ERROR("P0900", "Too many requests");

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

    private final Code code;
    private final String description;

    public static ErrorResponse anApiErrorResponse(Code code, Object... parameters) {
        return new ErrorResponse(code, parameters);
    }

    private ErrorResponse(Code code, Object... parameters) {
        this.code = code;
        this.description = format(code.getFormat(), parameters);
    }

    @ApiModelProperty(example = "P0900")
    public String getCode() {
        return code.value();
    }

    @ApiModelProperty(example = "Too many requests")
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "ApiErrorResponse{" +
                "code=" + code.value() +
                ", name=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
