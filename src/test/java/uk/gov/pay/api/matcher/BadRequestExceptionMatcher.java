package uk.gov.pay.api.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.PaymentError;

public class BadRequestExceptionMatcher extends TypeSafeMatcher<BadRequestException> {

    private final String code;
    private final String description;

    private BadRequestExceptionMatcher(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static BadRequestExceptionMatcher aBadRequestExceptionWithError(String code, String description) {
        return new BadRequestExceptionMatcher(code, description);
    }

    @Override
    protected boolean matchesSafely(BadRequestException e) {
        PaymentError paymentError = e.getPaymentError();
        return code.equals(paymentError.getCode()) &&
                description.equals(paymentError.getDescription());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(BadRequestException.class.getCanonicalName())
                .appendText(" with ")
                .appendText(" PaymentError. { code = ")
                .appendValue(code)
                .appendText(", description = ")
                .appendValue(this.description)
                .appendText(" }");
    }
}
