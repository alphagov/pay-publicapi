package uk.gov.pay.api.model.directdebit.agreement;

import org.junit.Test;
import uk.gov.pay.api.model.directdebit.agreement.AgreementError.Code;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class AgreementErrorTest {

    @Test
    public void shouldGetExpectedValuesWhenToStringIsCalled() {

        AgreementError agreementError = AgreementError.anAgreementError(Code.CREATE_AGREEMENT_ACCOUNT_ERROR);
        assertThat(agreementError.toString(), is("AgreementError{field=null, code=P0199, name=CREATE_AGREEMENT_ACCOUNT_ERROR, description='There is an error with this account. Please contact support'}"));
    }
}
