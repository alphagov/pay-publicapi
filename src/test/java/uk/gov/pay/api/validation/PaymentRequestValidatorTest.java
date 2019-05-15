package uk.gov.pay.api.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.commons.model.SupportedLanguage;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static uk.gov.pay.api.matcher.PaymentValidationExceptionMatcher.aValidationExceptionContaining;

public class PaymentRequestValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final int VALID_AMOUNT = 100;
    private static final String VALID_RETURN_URL = "https://www.example.com/return_url";
    private static final String VALID_REFERENCE = "reference";
    private static final String VALID_DESCRIPTION = "description";
    private static final String VALID_AGREEMENT_ID = "abcdef1234567890abcedf1234";

    private final PaymentRequestValidator paymentRequestValidator = new PaymentRequestValidator();

    @Test
    public void validParameters_withReturnUrl_shouldSuccessValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().build();
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validParameters_withAgreementId_shouldSuccessValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithAgreementId().build();
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validParameters_withEnglishLanguage_shouldSuccessValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().language(SupportedLanguage.ENGLISH).build();
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validParameters_withWelshLanguage_shouldSuccessValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().language(SupportedLanguage.WELSH).build();
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateMinimumAmount_shouldSuccessValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().amount(CreatePaymentRequest.AMOUNT_MIN_VALUE).build();
        paymentRequestValidator.validate(createPaymentRequest);
    }
    
    @Test
    public void validateCardHolderNameMaxLength_shouldFailValue() {
        String invalidMaxLengthEmail = randomAlphanumeric(PaymentRequestValidator.CARDHOLDER_NAME_MAX_LENGTH + 1);
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithAgreementId().cardholderName(invalidMaxLengthEmail).build();

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: cardholder_name. Must be less than or equal to 255 characters length"));

        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateLine1MaxLength_shouldFailValue() {
        String invalidMaxLengthEmail = randomAlphanumeric(PaymentRequestValidator.ADDRESS_LINE1_MAX_LENGTH+ 1);
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithAgreementId().addressLine1(invalidMaxLengthEmail).build();

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: line1. Must be less than or equal to 255 characters length"));

        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateLine2MaxLength_shouldFailValue() {
        String invalidMaxLengthEmail = randomAlphanumeric(PaymentRequestValidator.ADDRESS_LINE2_MAX_LENGTH+ 1);
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithAgreementId().addressLine2(invalidMaxLengthEmail).build();

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: line2. Must be less than or equal to 255 characters length"));

        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validatePostCodeMaxLength_shouldFailValue() {
        String invalidMaxLengthEmail = randomAlphanumeric(PaymentRequestValidator.POSTCODE_MAX_LENGTH+ 1);
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithAgreementId().postcode(invalidMaxLengthEmail).build();

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: postcode. Must be less than or equal to 25 characters length"));

        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateCityMaxLength_shouldFailValue() {
        String invalidMaxLengthEmail = randomAlphanumeric(PaymentRequestValidator.CITY_MAX_LENGTH+ 1);
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithAgreementId().city(invalidMaxLengthEmail).build();

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: city. Must be less than or equal to 255 characters length"));

        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateCountryMaxLength_shouldFailValue() {
        String invalidMaxLengthEmail = randomAlphanumeric(PaymentRequestValidator.COUNTRY_EXACT_LENGTH+ 1);
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithAgreementId().country(invalidMaxLengthEmail).build();

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: country. Must be exactly 2 characters length"));

        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateCountryMinLength_shouldFailValue() {
        String invalidMaxLengthEmail = randomAlphanumeric(1);
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithAgreementId().country(invalidMaxLengthEmail).build();

        expectedException.expect(aValidationExceptionContaining("P0102", "Invalid attribute value: country. Must be exactly 2 characters length"));

        paymentRequestValidator.validate(createPaymentRequest);
    }
    
    @Test
    public void validateCountryEmpty_shouldPass() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithAgreementId().country("").build();

        paymentRequestValidator.validate(createPaymentRequest);
    }

    private static CreatePaymentRequest.CreatePaymentRequestBuilder createPaymentRequestBuilderWithReturnUrl() {
        return CreatePaymentRequest.builder().amount(VALID_AMOUNT).returnUrl(VALID_RETURN_URL).reference(VALID_REFERENCE).description(VALID_DESCRIPTION);
    }

    private static CreatePaymentRequest.CreatePaymentRequestBuilder createPaymentRequestBuilderWithAgreementId() {
        return CreatePaymentRequest.builder().amount(VALID_AMOUNT).agreementId(VALID_AGREEMENT_ID).reference(VALID_REFERENCE).description(VALID_DESCRIPTION);
    }

}
