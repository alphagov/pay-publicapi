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

    @Test
    void shouldGetExpectedValuesForAgreementNotFoundError() {
        PaymentError paymentError = PaymentError.aPaymentError("set_up_agreement", PaymentError.Code.CREATE_PAYMENT_AGREEMENT_ID_ERROR);
        assertThat(paymentError.toString(), is("PaymentError{field=set_up_agreement, code=P0103, name=CREATE_PAYMENT_AGREEMENT_ID_ERROR, description='Invalid attribute value: set_up_agreement. Agreement ID does not exist'}"));
    }

}
