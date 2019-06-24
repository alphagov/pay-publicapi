package uk.gov.pay.api.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.TokenPaymentType;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static uk.gov.pay.api.matcher.PaymentValidationExceptionMatcher.aValidationExceptionContaining;

public class PaymentSearchValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private static final String SUCCESSFUL_TEST_EMAIL = "alice.111@mail.fake";
    private static final String UNSUCCESSFUL_TEST_EMAIL = randomAlphanumeric(255) + "@mail.fake";
    private static final String UNSUCCESSFUL_TEST_CARD_BRAND = "123456789012345678901";
    private static final String INVALID_LENGTH_AGREEMENT = randomAlphanumeric(27);
    private Account account = new Account("an account", TokenPaymentType.CARD);

    @Test
    public void validateParams_shouldSuccessValidation() {
        PaymentSearchValidator.validateSearchParameters(account,"success", "ref", SUCCESSFUL_TEST_EMAIL, 
                "", "2016-01-25T13:23:55Z", "2016-01-25T13:23:55Z", 
                "1", "1", "", "424242", "4242");
    }

    @Test
    public void validateParams_reference_shouldGiveAnErrorValidation() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: reference. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,"success", randomAlphanumeric(500), 
                SUCCESSFUL_TEST_EMAIL, "", "2016-01-25T13:23:55Z", "2016-01-25T13:23:55Z", 
                "1", "1", "", "424242", "4242");
    }

    @Test
    public void validateParams_email_shouldGiveAnErrorValidation() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: email. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,"success", "ref", UNSUCCESSFUL_TEST_EMAIL, 
                "", "2016-01-25T13:23:55Z", "2016-01-25T13:23:55Z", 
                "1", "1", "", "424242", "4242");
    }

    @Test
    public void validateParams_state_shouldGiveAnErrorValidation() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: state. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,"invalid", "ref", SUCCESSFUL_TEST_EMAIL, 
                "", "2016-01-25T13:23:55Z", "2016-01-25T13:23:55Z", "1", 
                "1", "", "424242", "4242");
    }

    @Test
    public void validateParams_toDate_shouldGiveAnErrorValidation() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: to_date. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,"success", "ref", SUCCESSFUL_TEST_EMAIL, 
                "", "2016-01-25T13:23:55Z", "2016-01-25T13-23:55Z", 
                "1", "1", "", "424242", "4242");
    }

    @Test
    public void validateParams_fromDate_shouldGiveAnErrorValidation() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: from_date. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,"success", "ref", SUCCESSFUL_TEST_EMAIL, 
                "", "2016-01-25T13-23:55Z", "2016-01-25T13:23:55Z", 
                "1", "1", "", "424242", "4242");
    }

    @Test
    public void validateParams_shouldGiveAnErrorValidation_forAllParams() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: state, reference, email, from_date, to_date, page, display_size, mandate_id. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,"invalid", randomAlphanumeric(500), UNSUCCESSFUL_TEST_EMAIL, 
                "", "2016-01-25T13-23:55Z", "2016-01-25T13-23:55Z", 
                "-1", "-1", INVALID_LENGTH_AGREEMENT, "424242", "4242");
    }

    @Test
    public void validateParams_shouldGiveAnErrorValidation_forZeroPageDisplay() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: state, reference, email, from_date, to_date, page, display_size. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,"invalid", randomAlphanumeric(500), UNSUCCESSFUL_TEST_EMAIL, 
                "", "2016-01-25T13-23:55Z", "2016-01-25T13-23:55Z", 
                "0", "0", "", "424242", "4242");
    }

    @Test
    public void validateParams_shouldGiveAnErrorValidation_forMaxedOutValuesPageDisplaySize() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: state, reference, email, from_date, to_date, page, display_size. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,"invalid", randomAlphanumeric(500), UNSUCCESSFUL_TEST_EMAIL, 
                "", "2016-01-25T13-23:55Z", "2016-01-25T13-23:55Z", 
                String.valueOf(Integer.MAX_VALUE+1), String.valueOf(Integer.MAX_VALUE+1), "", "424242", "4242");
    }

    @Test
    public void validateParams_shouldNotGiveAnErrorValidation_ForMissingPageDisplaySize() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: state, reference, email, from_date, to_date. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,"invalid", randomAlphanumeric(500), UNSUCCESSFUL_TEST_EMAIL, 
                "", "2016-01-25T13-23:55Z", "2016-01-25T13-23:55Z", 
                null, null, "", "424242", "4242");
    }

    @Test
    public void validateParams_shouldGiveAnErrorValidation_forTooLargePageDisplay() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: state, reference, email, from_date, to_date, page, display_size. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,"invalid", randomAlphanumeric(500), UNSUCCESSFUL_TEST_EMAIL, 
                "", "2016-01-25T13-23:55Z", "2016-01-25T13-23:55Z", 
                "0", "501", "", "424242", "4242");
    }

    @Test
    public void validateParams_shouldNotGiveAnErrorValidation_ForNonNumberPageAndSize() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: state, reference, email, from_date, to_date, page, display_size. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,"invalid", randomAlphanumeric(500), UNSUCCESSFUL_TEST_EMAIL, 
                "", "2016-01-25T13-23:55Z", "2016-01-25T13-23:55Z", 
                "non-numeric-page", "non-numeric-size", "", "424242", "4242");
    }

    @Test
    public void validateParams_card_brand_shouldGiveAnErrorValidation() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: card_brand. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,"success", "ref", SUCCESSFUL_TEST_EMAIL, 
                UNSUCCESSFUL_TEST_CARD_BRAND, "2016-01-25T13:23:55Z", "2016-01-25T13:23:55Z", 
                "1", "1", "", "424242", "4242");
    }
    
    @Test
    public void validateParams_shouldGiveAnError_forTooLongMandate() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: mandate_id. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,"", "", "", 
                "", "", "", 
                "","", INVALID_LENGTH_AGREEMENT, "424242", "4242");
    }
    
    @Test
    public void validateParams_shouldGiveAnError_forInvalidCardPaymentState() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: state. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,"pending", "", "",
                "", "", "",
                "","", "", "424242", "4242");
    }

    @Test
    public void validateParams_shouldGiveAnError_forInvalidDirectDebitState() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: state. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(new Account("an account", TokenPaymentType.DIRECT_DEBIT),
                "created", "", "",
                "", "", "",
                "","", "", "", "");
    }

    @Test
    public void validateParams_shouldGiveAnError_forWrongLengthFirstDigits() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: first_digits_card_number. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,
                "created", "", "",
                "", "", "",
                "","", "", "424", "");
    }
    
    @Test
    public void validateParams_shouldGiveAnError_forInvalidFirstDigits() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: first_digits_card_number. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,
                "", "", "",
                "", "", "",
                "","", "", "42424b", "");
    }

    @Test
    public void validateParams_shouldGiveAnError_forNegativeFirstDigits() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: first_digits_card_number. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,
                "", "", "",
                "", "", "",
                "","", "", "-42422", "");
    }

    @Test
    public void validateParams_shouldGiveAnError_forNonArabicFirstDigits() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: first_digits_card_number. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,
                "", "", "",
                "", "", "",
                "","", "", "१२३१२३", "");
    }

    @Test
    public void validateParams_shouldGiveAnError_forWrongLengthLastDigits() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: last_digits_card_number. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,
                "", "", "",
                "", "", "",
                "","", "", "", "422");
    }

    @Test
    public void validateParams_shouldGiveAnError_forInvalidLastDigits() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: last_digits_card_number. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,
                "", "", "",
                "", "", "",
                "","", "", "", "422a");
    }

    @Test
    public void validateParams_shouldGiveAnError_forNegativeLastDigits() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: last_digits_card_number. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,
                "", "", "",
                "", "", "",
                "","", "", "", "-433");
    }

    @Test
    public void validateParams_shouldGiveAnError_forNonArabicLastDigits() {
        expectedException.expect(aValidationExceptionContaining("P0401", "Invalid parameters: last_digits_card_number. See Public API documentation for the correct data formats"));
        PaymentSearchValidator.validateSearchParameters(account,
                "", "", "",
                "", "", "",
                "","", "", "", "१२३२");
    }
}
