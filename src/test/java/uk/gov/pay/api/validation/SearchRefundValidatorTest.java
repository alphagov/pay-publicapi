package uk.gov.pay.api.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static uk.gov.pay.api.matcher.RefundValidationExceptionMatcher.aValidationExceptionContaining;

public class SearchRefundValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private final static String NON_NUMERIC_STRING = "non-numeric-string";

    @Test
    public void validateSearchParameters_shouldSuccessValidation() {
        SearchRefundsValidator.validateSearchParameters(
                "1",
                "1"
        );
    }

    @Test
    public void validateSearchParameters_shouldGiveAValidationError_ForNonNumericPageAndSize() {
        expectedException.expect(aValidationExceptionContaining(
                "P1101", 
                "Invalid parameters: page, display_size. " +
                        "See Public API documentation for the correct data formats"));
        SearchRefundsValidator.validateSearchParameters(
                NON_NUMERIC_STRING, NON_NUMERIC_STRING);
    }
}
