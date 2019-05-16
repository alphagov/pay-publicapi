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
