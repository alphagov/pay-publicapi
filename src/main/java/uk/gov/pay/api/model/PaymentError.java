package uk.gov.pay.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import static uk.gov.pay.api.model.PaymentError.Code.P0100;
import static uk.gov.pay.api.model.PaymentError.Code.P0103;

@ApiModel(value = "Payment Error", description = "A Payment Error response")
public class PaymentError {

    public enum Code {
        P0199,
        P0198,
        P0100,
        P0101,
        P0102,
        P0103
    }

    private final Code code;
    private final String description;

    private PaymentError(Code code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PaymentError aPaymentError(Code code, String description) {
        return new PaymentError(code, description);
    }

    public static PaymentError missingMandatoryAttribute(String fieldName) {
        return new PaymentError(P0103, "Missing mandatory attribute: " + fieldName);
    }

    public static PaymentError invalidURLFormatAttributeValue(String fieldName) {
        return invalidFormatAttributeValue(fieldName, "Must be a valid URL format");
    }

    public static PaymentError invalidStringFormatAttributeValue(String fieldName) {
        return invalidFormatAttributeValue(fieldName, "Must be a valid string format");
    }

    public static PaymentError invalidNumericFormatAttributeValue(String fieldName) {
        return invalidFormatAttributeValue(fieldName, "Must be a valid numeric format");
    }

    private static PaymentError invalidFormatAttributeValue(String fieldName, String formatDescription) {
        return new PaymentError(P0100, "Invalid attribute value: " + fieldName + ". " + formatDescription);
    }

    public static PaymentError invalidAttributeValue(Code code, String fieldName, String explanation) {
        return new PaymentError(code, "Invalid attribute value: " + fieldName + ". " + explanation);
    }

    @ApiModelProperty(example = "P0102")
    public String getCode() {
        return code.name();
    }

    @ApiModelProperty(example = "Invalid attribute value: amount. Must be less than or equal to 10000000")
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "PaymentError{" +
                "code=" + code +
                ", description='" + description + '\'' +
                '}';
    }
}
