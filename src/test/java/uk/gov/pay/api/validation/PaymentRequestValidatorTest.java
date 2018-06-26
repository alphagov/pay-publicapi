package uk.gov.pay.api.validation;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import uk.gov.pay.api.exception.ValidationException;
import uk.gov.pay.api.model.CreatePaymentRequest;

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
        CreatePaymentRequest createPaymentRequest = createPaymentRequestWithReturnUrl(VALID_AMOUNT, VALID_RETURN_URL, VALID_REFERENCE, VALID_DESCRIPTION);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validParameters_withAgreementId_shouldSuccessValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestWithAgreementId(VALID_AMOUNT, VALID_REFERENCE, VALID_DESCRIPTION, VALID_AGREEMENT_ID);
        paymentRequestValidator.validate(createPaymentRequest);
    }
    
    @Test
    public void validateMinimumAmount_shouldSuccessValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestWithReturnUrl(PaymentRequestValidator.AMOUNT_MIN_VALUE, VALID_RETURN_URL, VALID_REFERENCE, VALID_DESCRIPTION);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateMinimumAmount_shouldFailValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestWithReturnUrl(PaymentRequestValidator.AMOUNT_MIN_VALUE - 1, VALID_RETURN_URL, VALID_REFERENCE, VALID_DESCRIPTION);
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateMaximumAmount_shouldSuccessValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequestWithReturnUrl(PaymentRequestValidator.AMOUNT_MAX_VALUE, VALID_RETURN_URL, VALID_REFERENCE, VALID_DESCRIPTION);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateMaximumAmount_shouldFailValue() {
        CreatePaymentRequest paymentRequest = createPaymentRequestWithReturnUrl(PaymentRequestValidator.AMOUNT_MAX_VALUE + 1, VALID_RETURN_URL, VALID_REFERENCE, VALID_DESCRIPTION);
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(paymentRequest);
    }

    @Test
    public void validateReturnUrlMaxLength_shouldFailValue() {
        String invalidMaxLengthReturnUrl = "https://" + randomAlphanumeric(PaymentRequestValidator.URL_MAX_LENGTH) + ".com/";
        CreatePaymentRequest paymentRequest = createPaymentRequestWithReturnUrl(VALID_AMOUNT, invalidMaxLengthReturnUrl, VALID_REFERENCE, VALID_DESCRIPTION);
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(paymentRequest);
    }

    @Test
    public void validateReturnUrlNotHttps_shouldFailValue() {
        String validHttpOnlyUrl = "http://www.example.com/";
        CreatePaymentRequest paymentRequest = createPaymentRequestWithReturnUrl(VALID_AMOUNT, validHttpOnlyUrl, VALID_REFERENCE, VALID_DESCRIPTION);
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(paymentRequest);
    }

    @Test
    public void validateReferenceMaxLength_shouldFailValue() {
        String invalidMaxLengthReference = randomAlphanumeric(PaymentRequestValidator.REFERENCE_MAX_LENGTH + 1);
        CreatePaymentRequest paymentRequest = createPaymentRequestWithReturnUrl(VALID_AMOUNT, VALID_RETURN_URL, invalidMaxLengthReference, VALID_DESCRIPTION);
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(paymentRequest);
    }

    @Test
    public void validateDescriptionMaxLength_shouldFailValue() {
        String invalidMaxLengthDescription = randomAlphanumeric(PaymentRequestValidator.DESCRIPTION_MAX_LENGTH + 1);
        CreatePaymentRequest paymentRequest = createPaymentRequestWithReturnUrl(VALID_AMOUNT, VALID_RETURN_URL, VALID_REFERENCE, invalidMaxLengthDescription);
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(paymentRequest);
    }

    @Test
    public void validateAgreementIdMaxLength_shouldFailValue() {
        String invalidMaxLengthAgreementId = randomAlphanumeric(PaymentRequestValidator.AGREEMENT_ID_MAX_LENGTH + 1);
        CreatePaymentRequest paymentRequest = createPaymentRequestWithAgreementId(VALID_AMOUNT, VALID_REFERENCE, VALID_DESCRIPTION, invalidMaxLengthAgreementId);
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(paymentRequest);
    }
    
    private static CreatePaymentRequest createPaymentRequestWithReturnUrl(int amount, String returnUrl, String reference, String description) {
        return new CreatePaymentRequest(amount, returnUrl, reference, description);
    }

    private static CreatePaymentRequest createPaymentRequestWithAgreementId(int amount, String reference, String description, String agreementId) {
        return new CreatePaymentRequest(amount, null, reference, description, agreementId);
    }

}
