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

public class CardLastFourDigitsValidatorTest {

    private static CreateTelephonePaymentRequest.TelephoneRequestBuilder telephoneRequestBuilder = new CreateTelephonePaymentRequest.TelephoneRequestBuilder();

    private static Validator validator;

    @BeforeClass
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        telephoneRequestBuilder
                .amount(1200)
                .description("Some description")
                .reference("Some reference")
                .createdDate("2018-02-21T16:04:25Z")
                .authorisedDate("2018-02-21T16:05:33Z")
                .authCode("666")
                .processorId("1PROC")
                .providerId("1PROV")
                .cardExpiry("01/99")
                .cardType("visa")
                .firstSixDigits("123456")
                .nameOnCard("Jane Doe")
                .emailAddress("jane_doe@example.com")
                .telephoneNumber("+447700900796")
                .paymentOutcome(new PaymentOutcome("success"));
    }
    
    @Test
    public void failsValidationForThreeDigits() {

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .lastFourDigits("123")
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.size(), is(1));
        assertThat(constraintViolations.iterator().next().getMessage(), is("Field [last_four_digits] must be exactly 4 digits"));
    }

    @Test
    public void failsValidationForFiveDigits() {

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .lastFourDigits("12345")
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.size(), is(1));
        assertThat(constraintViolations.iterator().next().getMessage(), is("Field [last_four_digits] must be exactly 4 digits"));
    }

    @Test
    public void failsValidationForFourDigits() {

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .lastFourDigits("1234")
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.size(), is(1));
        assertThat(constraintViolations.iterator().next().getMessage(), is("Field [last_four_digits] must be exactly 4 digits"));
    }

    @Test
    public void failsValidationForNullDigits() {

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .lastFourDigits(null)
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.size(), is(1));
        assertThat(constraintViolations.iterator().next().getMessage(), is("Field [last_four_digits] must be exactly 4 digits"));
    }

}
