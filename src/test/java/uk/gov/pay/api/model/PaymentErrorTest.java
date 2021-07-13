package uk.gov.pay.api.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class PaymentErrorTest {

    @Test
    void shouldGetExpectedValuesWhenToStringIsCalled() {
        PaymentError paymentError = PaymentError.aPaymentError(PaymentError.Code.CREATE_PAYMENT_ACCOUNT_ERROR);
        assertThat(paymentError.toString(), is("PaymentError{field=null, code=P0199, name=CREATE_PAYMENT_ACCOUNT_ERROR, description='There is an error with this account. Please contact support'}"));
    }
}
