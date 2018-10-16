package uk.gov.pay.api.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import uk.gov.pay.api.exception.PaymentValidationException;
import uk.gov.pay.api.model.PaymentError;

public class PaymentValidationExceptionMatcher extends TypeSafeMatcher<PaymentValidationException> {

    private final String code;
    private final String description;

    private PaymentValidationExceptionMatcher(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static PaymentValidationExceptionMatcher aValidationExceptionContaining(String code, String description) {
        return new PaymentValidationExceptionMatcher(code, description);
    }

    @Override
    protected boolean matchesSafely(PaymentValidationException e) {
        PaymentError paymentError = e.getPaymentError();
        return code.equals(paymentError.getCode()) &&
                description.equals(paymentError.getDescription());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(PaymentValidationException.class.getCanonicalName())
                .appendText(" with ")
                .appendText(" PaymentError. { code = ")
                .appendValue(code)
                .appendText(", description = ")
                .appendValue(this.description)
                .appendText(" }");
    }
}
