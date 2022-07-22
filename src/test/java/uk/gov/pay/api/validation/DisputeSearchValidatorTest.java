package uk.gov.pay.api.validation;

import org.junit.jupiter.api.Test;
import uk.gov.pay.api.exception.DisputesValidationException;
import uk.gov.pay.api.service.DisputesParams;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.pay.api.validation.DisputeSearchValidator.validateDisputeParameters;

class DisputeSearchValidatorTest {
    private static String VALIDATION_EXCEPTION_MESSAGE = "Invalid parameters: %s. See Public API documentation for the correct data formats";
    @Test
    void validateSearchParameters_shouldSuccessValidation() {
        DisputesParams params = new DisputesParams.Builder()
                .withFromDate("2022-07-22T11:23:55Z")
                .withToDate("2022-07-22T12:23:55Z")
                .withDisplaySize("1")
                .withFromSettledDate("2022-07-20")
                .withToSettledDate("2022-07-21")
                .withPage("1")
                .withState("won")
                .build();

        validateDisputeParameters(params);
    }

    @Test
    void validateParams_shouldNotGiveAnErrorValidation_ForMissingPageDisplaySize() {
        DisputesParams params = new DisputesParams.Builder()
                .withFromDate("2022-07-22T11:23:55Z")
                .withToDate("2022-07-22T12:23:55Z")
                .withDisplaySize(null)
                .withFromSettledDate("2022-07-20")
                .withToSettledDate("2022-07-21")
                .withPage(null)
                .withState("lost")
                .build();
        validateDisputeParameters(params);
    }

    @Test
    void validateSearchParameters_shouldGiveAValidationError_ForNonValidToDate() {
        DisputesParams params = new DisputesParams.Builder()
                .withToDate("alpha")
                .build();
        DisputesValidationException validationException = assertThrows(DisputesValidationException.class,
                () -> validateDisputeParameters(params));
        assertThat(validationException.getRequestError().getCode(), is("P0401"));
        assertThat(validationException.getRequestError().getDescription(), is(format(VALIDATION_EXCEPTION_MESSAGE, "to_date")));
    }

    @Test
    void validateSearchParameters_shouldGiveAValidationError_ForNonValidFromDate() {
        DisputesParams params = new DisputesParams.Builder()
                .withFromDate("bravo")
                .build();
        DisputesValidationException validationException = assertThrows(DisputesValidationException.class,
                () -> validateDisputeParameters(params));
        assertThat(validationException.getRequestError().getCode(), is("P0401"));
        assertThat(validationException.getRequestError().getDescription(), is(format(VALIDATION_EXCEPTION_MESSAGE, "from_date")));
    }

    @Test
    void validateSearchParameters_shouldGiveAValidationError_ForNonNumericPageAndSize() {
        DisputesParams params = new DisputesParams.Builder()
                .withDisplaySize("charlie")
                .withPage("delta")
                .build();
        DisputesValidationException validationException = assertThrows(DisputesValidationException.class,
                () -> validateDisputeParameters(params));
        assertThat(validationException.getRequestError().getCode(), is("P0401"));
        assertThat(validationException.getRequestError().getDescription(), is(format(VALIDATION_EXCEPTION_MESSAGE, "page, display_size")));
    }

    @Test
    void validateParams_shouldGiveAnErrorValidation_forZeroPageDisplay() {
        DisputesParams params = new DisputesParams.Builder()
                .withDisplaySize("0")
                .withPage("0")
                .build();
        DisputesValidationException validationException = assertThrows(DisputesValidationException.class,
                () -> validateDisputeParameters(params));
        assertThat(validationException.getRequestError().getCode(), is("P0401"));
        assertThat(validationException.getRequestError().getDescription(), is(format(VALIDATION_EXCEPTION_MESSAGE, "page, display_size")));
    }

    @Test
    void validateParams_shouldGiveAnErrorValidation_forMaxedOutValuesPageDisplaySize() {
        DisputesParams params = new DisputesParams.Builder()
                .withDisplaySize(String.valueOf(Integer.MAX_VALUE + 1))
                .withPage(String.valueOf(Integer.MAX_VALUE + 1))
                .build();
        DisputesValidationException validationException = assertThrows(DisputesValidationException.class,
                () -> validateDisputeParameters(params));
        assertThat(validationException.getRequestError().getCode(), is("P0401"));
        assertThat(validationException.getRequestError().getDescription(), is(format(VALIDATION_EXCEPTION_MESSAGE, "page, display_size")));
    }

    @Test
    void validateParams_shouldGiveAnErrorValidation_forTooLargeDisplaySize() {
        DisputesParams params = new DisputesParams.Builder()
                .withDisplaySize("501")
                .build();
        DisputesValidationException validationException = assertThrows(DisputesValidationException.class,
                () -> validateDisputeParameters(params));
        assertThat(validationException.getRequestError().getCode(), is("P0401"));
        assertThat(validationException.getRequestError().getDescription(), is(format(VALIDATION_EXCEPTION_MESSAGE, "display_size")));
    }

    @Test
    void validateSearchParameters_shouldGiveAValidationError_ForNonValidToSettledDate() {
        DisputesParams params = new DisputesParams.Builder()
                .withToSettledDate("January 7")
                .build();
        DisputesValidationException validationException = assertThrows(DisputesValidationException.class,
                () -> validateDisputeParameters(params));
        assertThat(validationException.getRequestError().getCode(), is("P0401"));
        assertThat(validationException.getRequestError().getDescription(), is(format(VALIDATION_EXCEPTION_MESSAGE, "to_settled_date")));
    }

    @Test
    void validateSearchParameters_shouldGiveAValidationError_ForNonValidFromSettledDate() {
        DisputesParams params = new DisputesParams.Builder()
                .withFromSettledDate("19th September")
                .build();
        DisputesValidationException validationException = assertThrows(DisputesValidationException.class,
                () -> validateDisputeParameters(params));
        assertThat(validationException.getRequestError().getCode(), is("P0401"));
        assertThat(validationException.getRequestError().getDescription(), is(format(VALIDATION_EXCEPTION_MESSAGE, "from_settled_date")));
    }

    @Test
    void validateSearchParameters_shouldGiveAValidationError_ForNonExistentStatus() {
        DisputesParams params = new DisputesParams.Builder()
                .withState("blew it")
                .build();
        DisputesValidationException validationException = assertThrows(DisputesValidationException.class,
                () -> validateDisputeParameters(params));
        assertThat(validationException.getRequestError().getCode(), is("P0401"));
        assertThat(validationException.getRequestError().getDescription(), is(format(VALIDATION_EXCEPTION_MESSAGE, "state")));
    }
}