package uk.gov.pay.api.model.directdebit.mandates;

import org.junit.Test;
import uk.gov.pay.api.model.directdebit.mandates.AgreementError.Code;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AgreementErrorTest {

    @Test
    public void shouldGetExpectedValuesWhenToStringIsCalled() {

        AgreementError agreementError = AgreementError.anAgreementError(Code.CREATE_MANDATE_ACCOUNT_ERROR);
        assertThat(agreementError.toString(), is("AgreementError{field=null, code=P0199, name=CREATE_MANDATE_ACCOUNT_ERROR, description='There is an error with this account. Please contact support'}"));
    }
}
