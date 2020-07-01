package uk.gov.pay.api.validation;

import org.junit.jupiter.api.Test;
import uk.gov.pay.api.exception.PaymentValidationException;

import java.time.ZonedDateTime;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class DirectDebitEventSearchValidatorTest {

    @Test
    public void shouldPassValidation() {
        DirectDebitEventSearchValidator.validateSearchParameters(ZonedDateTime.now().toString(), ZonedDateTime.now().toString(), 1);
    }

    @Test
    public void shouldFailWithInvalidDisplaySize() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> DirectDebitEventSearchValidator.validateSearchParameters(ZonedDateTime.now().toString(),
                        ZonedDateTime.now().toString(), 501));
        assertTrue(paymentValidationException.getPaymentError().getDescription().contains("display_size"));
    }

    @Test
    public void shouldFailWithInvalidToDate() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> DirectDebitEventSearchValidator.validateSearchParameters("Invalid date string",
                        ZonedDateTime.now().toString(), 1));
        assertTrue(paymentValidationException.getPaymentError().getDescription().contains("to_date"));
    }

    @Test
    public void shouldFailWithInvalidFromDate() {
        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class,
                () -> DirectDebitEventSearchValidator.validateSearchParameters(ZonedDateTime.now().toString(),
                        "Invalid date string", 1));
        assertTrue(paymentValidationException.getPaymentError().getDescription().contains("from_date"));
    }
}
