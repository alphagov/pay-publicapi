package uk.gov.pay.api.validation;

import org.junit.BeforeClass;
import uk.gov.pay.api.model.telephone.CreateTelephonePaymentRequest;
import uk.gov.pay.api.model.telephone.PaymentOutcome;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

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
    
    
}
