package uk.gov.pay.api.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import uk.gov.pay.api.exception.ValidationException;
import uk.gov.pay.api.model.generated.PaymentError;

public class ValidationExceptionMatcher extends TypeSafeMatcher<ValidationException> {

    private final String code;
    private final String description;

    private ValidationExceptionMatcher(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ValidationExceptionMatcher aValidationExceptionContaining(String code, String description) {
        return new ValidationExceptionMatcher(code, description);
    }

    @Override
    protected boolean matchesSafely(ValidationException e) {
        PaymentError paymentError = e.getPaymentError();
        return code.equals(paymentError.getCode()) &&
                description.equals(paymentError.getDescription());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(ValidationException.class.getCanonicalName())
                .appendText(" with ")
                .appendText(" PaymentError. { code = ")
                .appendValue(code)
                .appendText(", description = ")
                .appendValue(this.description)
                .appendText(" }");
    }
}
