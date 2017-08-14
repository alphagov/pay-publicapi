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

    private final PaymentRequestValidator paymentRequestValidator = new PaymentRequestValidator(URLValidator.SECURITY_ENABLED);

    @Test
    public void validParameters_shouldSuccessValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequest(VALID_AMOUNT, VALID_RETURN_URL, VALID_REFERENCE, VALID_DESCRIPTION);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateMinimumAmount_shouldSuccessValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequest(PaymentRequestValidator.AMOUNT_MIN_VALUE, VALID_RETURN_URL, VALID_REFERENCE, VALID_DESCRIPTION);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateMinimumAmount_shouldFailValue() throws Exception {
        CreatePaymentRequest createPaymentRequest = createPaymentRequest(PaymentRequestValidator.AMOUNT_MIN_VALUE - 1, VALID_RETURN_URL, VALID_REFERENCE, VALID_DESCRIPTION);
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateMaximumAmount_shouldSuccessValue() {
        CreatePaymentRequest createPaymentRequest = createPaymentRequest(PaymentRequestValidator.AMOUNT_MAX_VALUE, VALID_RETURN_URL, VALID_REFERENCE, VALID_DESCRIPTION);
        paymentRequestValidator.validate(createPaymentRequest);
    }

    @Test
    public void validateMaximumAmount_shouldFailValue() throws Exception {
        CreatePaymentRequest paymentRequest = createPaymentRequest(PaymentRequestValidator.AMOUNT_MAX_VALUE + 1, VALID_RETURN_URL, VALID_REFERENCE, VALID_DESCRIPTION);
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(paymentRequest);
    }

    @Test
    public void validateReturnUrlMaxLength_shouldFailValue() throws Exception {
        String invalidMaxLengthReturnUrl = "https://" + randomAlphanumeric(PaymentRequestValidator.URL_MAX_LENGTH) + ".com/";
        CreatePaymentRequest paymentRequest = createPaymentRequest(VALID_AMOUNT, invalidMaxLengthReturnUrl, VALID_REFERENCE, VALID_DESCRIPTION);
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(paymentRequest);
    }

    @Test
    public void validateReturnUrlNotHttps_shouldFailValue() throws Exception {
        String validHttpOnlyUrl = "http://www.example.com/";
        CreatePaymentRequest paymentRequest = createPaymentRequest(VALID_AMOUNT, validHttpOnlyUrl, VALID_REFERENCE, VALID_DESCRIPTION);
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(paymentRequest);
    }

    @Test
    public void validateReferenceMaxLength_shouldFailValue() throws Exception {
        String invalidMaxLengthReference = randomAlphanumeric(PaymentRequestValidator.REFERENCE_MAX_LENGTH + 1);
        CreatePaymentRequest paymentRequest = createPaymentRequest(VALID_AMOUNT, VALID_RETURN_URL, invalidMaxLengthReference, VALID_DESCRIPTION);
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(paymentRequest);
    }

    @Test
    public void validateDescriptionMaxLength_shouldFailValue() throws Exception {
        String invalidMaxLengthDescription = randomAlphanumeric(PaymentRequestValidator.DESCRIPTION_MAX_LENGTH + 1);
        CreatePaymentRequest paymentRequest = createPaymentRequest(VALID_AMOUNT, VALID_RETURN_URL, VALID_REFERENCE, invalidMaxLengthDescription);
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(paymentRequest);
    }

    private CreatePaymentRequest createPaymentRequest(int amount, String returnUrl, String reference, String description) {
        return new CreatePaymentRequest(amount, returnUrl, reference, description);
    }

}
