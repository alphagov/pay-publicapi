package uk.gov.pay.api.model;

import org.junit.Test;
import uk.gov.pay.api.model.generated.PaymentError;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static uk.gov.pay.api.model.PaymentErrorBuilder.aPaymentError;

public class PaymentErrorTest {

    @Test
    public void shouldGetExpectedValuesWhenToStringIsCalled() {

        String paymentError = aPaymentError(PaymentErrorCodes.CREATE_PAYMENT_ACCOUNT_ERROR).toString();
        assertThat(paymentError, containsString("code: P0199"));
        assertThat(paymentError, containsString("description: There is an error with this account. Please contact support"));
    }
}
