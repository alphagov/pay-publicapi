package uk.gov.pay.api.model;

import uk.gov.pay.api.model.generated.PaymentError;

import static com.google.common.collect.ObjectArrays.concat;
import static java.lang.String.format;

public class PaymentErrorBuilder {

    public static PaymentError aPaymentError(PaymentErrorCodes code, Object... parameters) {
        return new PaymentError().code(code.value()).description(format(code.getFormat(), parameters));
    }

    public static PaymentError aPaymentError(String fieldName, PaymentErrorCodes code, Object... parameters) {
        return new PaymentError().field(fieldName).code(code.value()).description(format(code.getFormat(), concat(fieldName, parameters)));
    }
}
