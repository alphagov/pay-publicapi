package uk.gov.pay.api.validation;

import org.junit.jupiter.api.Test;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.PaymentValidationException;
import uk.gov.pay.api.model.TokenPaymentType;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.pay.api.matcher.PaymentValidationExceptionMatcher.aValidationExceptionContaining;

public class PaymentSearchValidatorTest {

    private static final String SUCCESSFUL_TEST_EMAIL = "alice.111@mail.fake";
    private static final String UNSUCCESSFUL_TEST_EMAIL = randomAlphanumeric(255) + "@mail.fake";
    private static final String UNSUCCESSFUL_TEST_CARD_BRAND = "123456789012345678901";
    private static final String INVALID_LENGTH_AGREEMENT = randomAlphanumeric(27);
    private Account account = new Account("an account", TokenPaymentType.CARD, "a-token-link");

    @Test
    public void validateParams_shouldSuccessValidation() {
        PaymentSearchValidator.validateSearchParameters(account, "success", "ref", SUCCESSFUL_TEST_EMAIL,
                "", "2016-01-25T13:23:55Z", "2016-01-25T13:23:55Z",
                "1", "1", "", "424242", "4242",
                "2020-09-25", "2020-09-25");
    }

    @Test
    public void validateParams_reference_shouldGiveAnErrorValidation() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account, "success", randomAlphanumeric(500),
                        SUCCESSFUL_TEST_EMAIL, "", "2016-01-25T13:23:55Z", "2016-01-25T13:23:55Z",
                        "1", "1", "", "424242", "4242",
                        "2020-09-25", "2020-09-25"));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401", "Invalid parameters: reference. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_email_shouldGiveAnErrorValidation() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account, "success", "ref", UNSUCCESSFUL_TEST_EMAIL,
                        "", "2016-01-25T13:23:55Z", "2016-01-25T13:23:55Z",
                        "1", "1", "", "424242", "4242",
                        "2020-09-25", "2020-09-25"));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401", "Invalid parameters: email. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_state_shouldGiveAnErrorValidation() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account, "invalid", "ref", SUCCESSFUL_TEST_EMAIL,
                        "", "2016-01-25T13:23:55Z", "2016-01-25T13:23:55Z", "1",
                        "1", "", "424242", "4242",
                        "2020-09-25", "2020-09-25"));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401", "Invalid parameters: state. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_toDate_shouldGiveAnErrorValidation() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account, "success", "ref", SUCCESSFUL_TEST_EMAIL,
                        "", "2016-01-25T13:23:55Z", "2016-01-25T13-23:55Z",
                        "1", "1", "", "424242", "4242",
                        "2020-09-25", "2020-09-25"));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401", "Invalid parameters: to_date. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_fromDate_shouldGiveAnErrorValidation() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account, "success", "ref", SUCCESSFUL_TEST_EMAIL,
                        "", "2016-01-25T13-23:55Z", "2016-01-25T13:23:55Z",
                        "1", "1", "", "424242", "4242",
                        "2020-09-25", "2020-09-25"));
        assertThat(paymentValidationException,
                aValidationExceptionContaining("P0401", "Invalid parameters: from_date. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnErrorValidation_forAllParams() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account, "invalid", randomAlphanumeric(500), UNSUCCESSFUL_TEST_EMAIL,
                        "", "2016-01-25T13-23:55Z", "2016-01-25T13-23:55Z",
                        "-1", "-1", INVALID_LENGTH_AGREEMENT,
                        "424242", "4242", "2020-09-25", "2020-09-25"));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401", "Invalid parameters: state, reference, email, from_date, to_date, page, display_size, mandate_id. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnErrorValidation_forZeroPageDisplay() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account, "invalid", randomAlphanumeric(500), UNSUCCESSFUL_TEST_EMAIL,
                        "", "2016-01-25T13-23:55Z", "2016-01-25T13-23:55Z",
                        "0", "0", "", "424242", "4242",
                        "2020-09-25", "2020-09-25"));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401", "Invalid parameters: state, reference, email, from_date, to_date, page, display_size. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnErrorValidation_forMaxedOutValuesPageDisplaySize() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account, "invalid", randomAlphanumeric(500), UNSUCCESSFUL_TEST_EMAIL,
                        "", "2016-01-25T13-23:55Z", "2016-01-25T13-23:55Z",
                        String.valueOf(Integer.MAX_VALUE + 1), String.valueOf(Integer.MAX_VALUE + 1), "", "424242", "4242",
                        "2020-09-25", "2020-09-25"));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401",
                "Invalid parameters: state, reference, email, from_date, to_date, page, display_size. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldNotGiveAnErrorValidation_ForMissingPageDisplaySize() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account, "invalid", randomAlphanumeric(500), UNSUCCESSFUL_TEST_EMAIL,
                        "", "2016-01-25T13-23:55Z", "2016-01-25T13-23:55Z",
                        null, null, "", "424242", "4242", "2020-09-25", "2020-09-25"));
        assertThat(paymentValidationException,
                aValidationExceptionContaining("P0401", "Invalid parameters: state, reference, email, from_date, to_date. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnErrorValidation_forTooLargePageDisplay() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account, "invalid", randomAlphanumeric(500), UNSUCCESSFUL_TEST_EMAIL,
                        "", "2016-01-25T13-23:55Z", "2016-01-25T13-23:55Z",
                        "0", "501", "", "424242", "4242",
                        "2020-09-25", "2020-09-25"));
        assertThat(paymentValidationException,
                aValidationExceptionContaining("P0401", "Invalid parameters: state, reference, email, from_date, to_date, page, display_size. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldNotGiveAnErrorValidation_ForNonNumberPageAndSize() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account, "invalid", randomAlphanumeric(500), UNSUCCESSFUL_TEST_EMAIL,
                        "", "2016-01-25T13-23:55Z", "2016-01-25T13-23:55Z",
                        "non-numeric-page", "non-numeric-size", "", "424242", "4242",
                        "2020-09-25", "2020-09-25"));
        assertThat(paymentValidationException,
                aValidationExceptionContaining("P0401", "Invalid parameters: state, reference, email, from_date, to_date, page, display_size. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_card_brand_shouldGiveAnErrorValidation() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account, "success", "ref", SUCCESSFUL_TEST_EMAIL,
                        UNSUCCESSFUL_TEST_CARD_BRAND, "2016-01-25T13:23:55Z", "2016-01-25T13:23:55Z",
                        "1", "1", "", "424242", "4242",
                        "2020-09-25", "2020-09-25"));
        assertThat(paymentValidationException,
                aValidationExceptionContaining("P0401", "Invalid parameters: card_brand. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnError_forTooLongMandate() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account, "", "", "",
                        "", "", "",
                        "", "", INVALID_LENGTH_AGREEMENT, "424242", "4242", null, null));
        assertThat(paymentValidationException,
                aValidationExceptionContaining("P0401", "Invalid parameters: mandate_id. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnError_forInvalidCardPaymentState() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account, "pending", "", "",
                        "", "", "",
                        "", "", "", "424242",
                        "4242", "2020-09-25", "2020-09-25"));
        assertThat(paymentValidationException,
                aValidationExceptionContaining("P0401", "Invalid parameters: state. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnError_forInvalidDirectDebitState() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(new Account("an account", TokenPaymentType.DIRECT_DEBIT, "a-token-link"),
                        "created", "", "",
                        "", "", "",
                        "", "", "", "", "", "", ""));
        assertThat(paymentValidationException,
                aValidationExceptionContaining("P0401", "Invalid parameters: state. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnError_forWrongLengthFirstDigits() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account,
                        "created", "", "",
                        "", "", "",
                        "", "", "", "424", "", "", ""));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401",
                "Invalid parameters: first_digits_card_number. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnError_forInvalidFirstDigits() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account,
                        "", "", "",
                        "", "", "",
                        "", "", "", "42424b", "", "", ""));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401",
                "Invalid parameters: first_digits_card_number. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnError_forNegativeFirstDigits() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account,
                        "", "", "",
                        "", "", "",
                        "", "", "", "-42422", "", "", ""));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401",
                "Invalid parameters: first_digits_card_number. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnError_forNonArabicFirstDigits() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account,
                        "", "", "",
                        "", "", "",
                        "", "", "", "१२३१२३", "", "", ""));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401",
                "Invalid parameters: first_digits_card_number. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnError_forWrongLengthLastDigits() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account,
                        "", "", "",
                        "", "", "",
                        "", "", "", "", "422", "", ""));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401",
                "Invalid parameters: last_digits_card_number. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnError_forInvalidLastDigits() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account,
                        "", "", "",
                        "", "", "",
                        "", "", "",
                        "", "422a",
                        "", ""));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401",
                "Invalid parameters: last_digits_card_number. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnError_forNegativeLastDigits() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account,
                        "", "", "",
                        "", "", "",
                        "", "", "",
                        "", "-433",
                        "", ""));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401",
                "Invalid parameters: last_digits_card_number. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateParams_shouldGiveAnError_forNonArabicLastDigits() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account,
                        "", "", "",
                        "", "", "",
                        "", "", "",
                        "", "१२३२",
                        "", ""));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401", "Invalid parameters: last_digits_card_number. See Public API documentation for the correct data formats"));
    }

    @Test
    public void validateDateParams_shouldGiveAnError_forNonISO_8601Dates() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> PaymentSearchValidator.validateSearchParameters(account,
                        "", "", "",
                        "", "", "",
                        "", "", "", "", "",
                        "2020.09.25", "2020-09-25T10:35:00"));
        assertThat(paymentValidationException, aValidationExceptionContaining("P0401", "Invalid parameters: from_settled_date, to_settled_date. See Public API documentation for the correct data formats"));
    }
}
