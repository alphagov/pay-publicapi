package uk.gov.pay.api.validation;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.pay.api.model.telephone.CreateTelephonePaymentRequest;
import uk.gov.pay.api.model.telephone.PaymentOutcome;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CardExpiryValidatorTest {

    private static CreateTelephonePaymentRequest.TelephoneRequestBuilder telephoneRequestBuilder = new CreateTelephonePaymentRequest.TelephoneRequestBuilder();

    private static Validator validator;

    @BeforeClass
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        telephoneRequestBuilder
                .withAmount(1200)
                .withDescription("Some description")
                .withReference("Some reference")
                .withProcessorId("1PROC")
                .withProviderId("1PROV")
                .withLastFourDigits("1234")
                .withFirstSixDigits("123456")
                .withCardType("visa")
                .withPaymentOutcome(new PaymentOutcome("success"));
    }

    @Test
    public void failsValidationForInvalidMonth00() {

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .withCardExpiry("00/99")
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.size(), is(1));
        assertThat(constraintViolations.iterator().next().getMessage(), is("Field [card_expiry] must have valid MM/YY"));
    }
    
    @Test
    public void failsValidationForInvalidMonth99() {

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .withCardExpiry("99/99")
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.size(), is(1));
        assertThat(constraintViolations.iterator().next().getMessage(), is("Field [card_expiry] must have valid MM/YY"));
    }

    @Test
    public void passesValidationForValidCardExpiry() {

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .withCardExpiry("01/99")
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.isEmpty(), is(true));
    }

    @Test
    public void passesValidationForNullCardExpiry() {

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .withCardExpiry(null)
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.size(), is(1));
        assertThat(constraintViolations.iterator().next().getMessage().equals("Field [card_expiry] cannot be null"), is(false));
    }
}
