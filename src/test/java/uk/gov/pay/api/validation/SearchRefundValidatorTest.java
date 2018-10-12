package uk.gov.pay.api.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import uk.gov.pay.api.service.RefundsParams;

import static uk.gov.pay.api.matcher.RefundValidationExceptionMatcher.aValidationExceptionContaining;

public class SearchRefundValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private RefundsParams params;


    @Test
    public void validateSearchParameters_shouldSuccessValidation() {
        params = new RefundsParams("1", "1");
        SearchRefundsValidator.validateSearchParameters(params);
    }

    @Test
    public void validateSearchParameters_shouldGiveAValidationError_ForNonNumericPageAndSize() {
        String NON_NUMERIC_STRING = "non-numeric-string";
        params = new RefundsParams(NON_NUMERIC_STRING, NON_NUMERIC_STRING);

        expectedException.expect(aValidationExceptionContaining(
                "P1101",
                "Invalid parameters: page, display_size. " +
                        "See Public API documentation for the correct data formats"));
        SearchRefundsValidator.validateSearchParameters(params);
    }
}
