package uk.gov.pay.api.validation;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.pay.api.model.telephone.CreateTelephonePaymentRequest;
import uk.gov.pay.api.model.telephone.PaymentOutcome;
import uk.gov.pay.api.model.telephone.Supplemental;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PaymentOutcomeValidatorTest {

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
                .lastFourDigits("1234")
                .firstSixDigits("123456")
                .cardExpiry("01/13")
                .cardType("visa")
                .nameOnCard("Jane Doe")
                .emailAddress("jane_doe@example.com")
                .telephoneNumber("+447700900796");
    }

    @Test
    public void failsValidationForInvalidPaymentOutcomeStatus() {

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .paymentOutcome(new PaymentOutcome("invalid"))
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.size(), is(1));
        assertThat(constraintViolations.iterator().next().getMessage(), is("Field [payment_outcome] must include a valid status and error code"));
    }

    @Test
    public void failsValidationForPaymentOutcomeStatusSuccessAndErrorCodeGiven() {

        PaymentOutcome paymentOutcome = new PaymentOutcome(
                "success",
                "error",
                new Supplemental(
                        "ECKOH01234",
                        "textual message describing error code"
                )
        );

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .paymentOutcome(paymentOutcome)
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.size(), is(1));
        assertThat(constraintViolations.iterator().next().getMessage(), is("Field [payment_outcome] must include a valid status and error code"));
    }

    @Test
    public void failsValidationForInvalidErrorCode() {

        PaymentOutcome paymentOutcome = new PaymentOutcome(
                "failed",
                "error",
                new Supplemental(
                        "ECKOH01234",
                        "textual message describing error code"
                )
        );

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .paymentOutcome(paymentOutcome)
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.size(), is(1));
        assertThat(constraintViolations.iterator().next().getMessage(), is("Field [payment_outcome] must include a valid status and error code"));
    }

    @Test
    public void passesValidationForCorrectErrorCode() {

        PaymentOutcome paymentOutcome = new PaymentOutcome(
                "failed",
                "P0010",
                new Supplemental(
                        "ECKOH01234",
                        "textual message describing error code"
                )
        );

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .paymentOutcome(paymentOutcome)
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.isEmpty(), is(true));
    }

    @Test
    public void passesValidationForPaymentOutcomeStatusOfSuccess() {

        CreateTelephonePaymentRequest telephonePaymentRequest = telephoneRequestBuilder
                .paymentOutcome(new PaymentOutcome("success"))
                .build();

        Set<ConstraintViolation<CreateTelephonePaymentRequest>> constraintViolations = validator.validate(telephonePaymentRequest);

        assertThat(constraintViolations.isEmpty(), is(true));
    }
    
    
    
    
}
