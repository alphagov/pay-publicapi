package uk.gov.pay.api.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.PaymentError;

public class BadRequestExceptionMatcher extends TypeSafeMatcher<BadRequestException> {

    private PaymentError expectedError;

    private BadRequestExceptionMatcher(PaymentError expectedError) {
        this.expectedError = expectedError;
    }

    public static BadRequestExceptionMatcher aBadRequestExceptionContaining(PaymentError expectedError) {
        return new BadRequestExceptionMatcher(expectedError);
    }

    @Override
    protected boolean matchesSafely(BadRequestException e) {
        PaymentError paymentError = e.getPaymentError();
        return expectedError.getCode().equals(paymentError.getCode()) &&
                expectedError.getDescription().equals(paymentError.getDescription());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(BadRequestException.class.getCanonicalName())
                .appendText(" with ")
                .appendText(expectedError.toString());
    }
}
