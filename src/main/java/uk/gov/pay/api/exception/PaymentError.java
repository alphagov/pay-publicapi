package uk.gov.pay.api.exception;

import javax.ws.rs.core.Response;

import static uk.gov.pay.api.exception.PaymentError.ErrorCode.P0100;
import static uk.gov.pay.api.exception.PaymentError.ErrorCode.P0102;
import static uk.gov.pay.api.exception.PaymentError.ErrorCode.P0103;

public class PaymentError {


    public enum ErrorCode {
        P0199(500),
        P0198(500),
        P0100(400),
        P0101(422),
        P0102(422),
        P0103(400);
        private final int status;

        ErrorCode(int status) {
            this.status = status;
        }
    }

    private final ErrorCode errorCode;
    private final String description;

    private PaymentError(ErrorCode errorCode, String description) {
        this.errorCode = errorCode;
        this.description = description;
    }

    public static PaymentError serverError(ErrorCode errorCode, String description) {
        return new PaymentError(errorCode, description);
    }

    public static PaymentError unparseableJSON() {
        return new PaymentError(P0100, "Unable to parse JSON");
    }

    public static PaymentError missingMandatoryAttribute(String fieldName) {
        return new PaymentError(P0103, "Missing mandatory attribute: " + fieldName);
    }

    public static PaymentError unrecognisedAttributeValue(String fieldName) {
        return new PaymentError(P0100, "Invalid attribute value: " + fieldName + ". Unrecognised format");
    }

    public static PaymentError invalidAttributeValue(ErrorCode errorCode, String fieldName, String explanation) {
        return new PaymentError(errorCode, "Invalid attribute value: " + fieldName + ". " + explanation);
    }
    
    public Response asResponse() {
        return Response.status(errorCode.status)
                .entity(new uk.gov.pay.api.model.PaymentErrorResponse(errorCode.name(), description))
                .build();
    }

    @Override
    public String toString() {
        return "PaymentError{" +
                "errorCode=" + errorCode +
                ", description='" + description + '\'' +
                '}';
    }
}
