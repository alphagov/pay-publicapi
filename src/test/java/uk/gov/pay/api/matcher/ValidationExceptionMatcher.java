package uk.gov.pay.api.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import uk.gov.pay.api.exception.ValidationException;
import uk.gov.pay.api.model.PaymentError;

public class ValidationExceptionMatcher extends TypeSafeMatcher<ValidationException> {

    private PaymentError expectedError;

    private ValidationExceptionMatcher(PaymentError expectedError) {
        this.expectedError = expectedError;
    }

    public static ValidationExceptionMatcher aValidationExceptionContaining(PaymentError expectedError) {
        return new ValidationExceptionMatcher(expectedError);
    }

    @Override
    protected boolean matchesSafely(ValidationException e) {
        PaymentError paymentError = e.getPaymentError();
        return expectedError.getCode().equals(paymentError.getCode()) &&
                expectedError.getDescription().equals(paymentError.getDescription());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(ValidationException.class.getCanonicalName())
                .appendText(" with ")
                .appendText(expectedError.toString());
    }
}
