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
    private static final String SUCCESSFUL_TEST_EMAIL = "alice.111@mail.fake";
    private static final String UNSUCCESSFUL_TEST_EMAIL = randomAlphanumeric(255) + "@mail.fake";

    @Test
    public void validateMinimumAmount_shouldSuccessValue(){
        PaymentRequestValidator paymentRequestValidator = new PaymentRequestValidator(URLValidator.SECURITY_ENABLED);
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(1, "https://return.to/return_url", "reference", "description");
    }

    @Test
    public void validateMinimumAmount_shouldFailValue() throws Exception{
        PaymentRequestValidator paymentRequestValidator = new PaymentRequestValidator(URLValidator.SECURITY_ENABLED);
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(PaymentRequestValidator.AMOUNT_MAX_VALUE + 1, "https://return.to/return_url", "reference", "description");
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(paymentRequest);
    }

    @Test
    public void validateMaximumAmount_shouldSuccessValue(){
        PaymentRequestValidator paymentRequestValidator = new PaymentRequestValidator(URLValidator.SECURITY_ENABLED);
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(100, "https://return.to/return_url", "reference", "description");
    }

    @Test
    public void validateMaximumAmount_shouldFailValue() throws Exception{
        PaymentRequestValidator paymentRequestValidator = new PaymentRequestValidator(URLValidator.SECURITY_ENABLED);
        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(PaymentRequestValidator.AMOUNT_MAX_VALUE + 1, "https://return.to/return_url", "reference", "description");
        expectedException.expect(ValidationException.class);
        paymentRequestValidator.validate(paymentRequest);
    }
}
