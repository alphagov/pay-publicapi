package uk.gov.pay.api.model.directdebit.mandates;

import org.junit.jupiter.api.Test;
import uk.gov.pay.api.model.directdebit.mandates.MandateError.Code;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MandateErrorTest {

    @Test
    public void shouldGetExpectedValuesWhenToStringIsCalled() {

        MandateError mandateError = MandateError.aMandateError(Code.CREATE_MANDATE_ACCOUNT_ERROR);
        assertThat(mandateError.toString(), is("MandateError{field=null, code=P0199, name=CREATE_MANDATE_ACCOUNT_ERROR, description='There is an error with this account. Please contact support'}"));
    }
}
