package uk.gov.pay.api.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.api.exception.PaymentValidationException;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.commons.model.SupportedLanguage;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

public class PaymentRequestValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final int VALID_AMOUNT = 100;
    private static final String VALID_RETURN_URL = "https://www.example.com/return_url";
    private static final String VALID_REFERENCE = "reference";
    private static final String VALID_DESCRIPTION = "description";
    private static final String VALID_AGREEMENT_ID = "abcdef1234567890abcedf1234";

    private final PaymentRequestValidator paymentRequestValidator = new PaymentRequestValidator(URLValidator.SECURITY_ENABLED);

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
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().language(SupportedLanguage.ENGLISH.toString()).build();
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validParameters_withWelshLanguage_shouldSuccessValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().language(SupportedLanguage.WELSH.toString()).build();
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateUnsupportedLanguage_shouldFailValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().language("unsupported language").build();
        expectedException.expect(PaymentValidationException.class);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateMinimumAmount_shouldSuccessValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().amount(PaymentRequestValidator.AMOUNT_MIN_VALUE).build();
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateMinimumAmount_shouldFailValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().amount(PaymentRequestValidator.AMOUNT_MIN_VALUE - 1).build();
        expectedException.expect(PaymentValidationException.class);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateMaximumAmount_shouldSuccessValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().amount(PaymentRequestValidator.AMOUNT_MAX_VALUE).build();
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateMaximumAmount_shouldFailValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().amount(PaymentRequestValidator.AMOUNT_MAX_VALUE + 1).build();
        expectedException.expect(PaymentValidationException.class);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateReturnUrlMaxLength_shouldFailValue() {
        String invalidMaxLengthReturnUrl = "https://" + randomAlphanumeric(PaymentRequestValidator.URL_MAX_LENGTH) + ".com/";
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().returnUrl(invalidMaxLengthReturnUrl).build();
        expectedException.expect(PaymentValidationException.class);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateReturnUrlNotHttps_shouldFailValue() {
        String validHttpOnlyUrl = "http://www.example.com/";
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().returnUrl(validHttpOnlyUrl).build();
        expectedException.expect(PaymentValidationException.class);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateReferenceMaxLength_shouldFailValue() {
        String invalidMaxLengthReference = randomAlphanumeric(PaymentRequestValidator.REFERENCE_MAX_LENGTH + 1);
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().reference(invalidMaxLengthReference).build();
        expectedException.expect(PaymentValidationException.class);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateDescriptionMaxLength_shouldFailValue() {
        String invalidMaxLengthDescription = randomAlphanumeric(PaymentRequestValidator.DESCRIPTION_MAX_LENGTH + 1);
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithReturnUrl().description(invalidMaxLengthDescription).build();
        expectedException.expect(PaymentValidationException.class);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateAgreementIdMaxLength_shouldFailValue() {
        String invalidMaxLengthAgreementId = randomAlphanumeric(PaymentRequestValidator.AGREEMENT_ID_MAX_LENGTH + 1);
        CreatePaymentRequest createPaymentRequest = createPaymentRequestBuilderWithAgreementId().agreementId(invalidMaxLengthAgreementId).build();
        expectedException.expect(PaymentValidationException.class);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    private static CreatePaymentRequest.CreatePaymentRequestBuilder createPaymentRequestBuilderWithReturnUrl() {
        return CreatePaymentRequest.builder().amount(VALID_AMOUNT).returnUrl(VALID_RETURN_URL).reference(VALID_REFERENCE).description(VALID_DESCRIPTION);
    }

    private static CreatePaymentRequest.CreatePaymentRequestBuilder createPaymentRequestBuilderWithAgreementId() {
        return CreatePaymentRequest.builder().amount(VALID_AMOUNT).agreementId(VALID_AGREEMENT_ID).reference(VALID_REFERENCE).description(VALID_DESCRIPTION);
    }

}
