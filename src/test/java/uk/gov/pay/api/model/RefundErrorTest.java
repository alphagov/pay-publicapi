package uk.gov.pay.api.model;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RefundErrorTest {

    @Test
    public void shouldGetExpectedValuesWhenToStringIsCalled() {

        RefundError refundError = RefundError.aRefundError(RefundError.Code.SEARCH_REFUNDS_CONNECTOR_ERROR);
        assertThat(refundError.toString(), is("RefundError{field=null, code=P1898, name=SEARCH_REFUNDS_CONNECTOR_ERROR, description='Downstream system error'}"));
    }
}
