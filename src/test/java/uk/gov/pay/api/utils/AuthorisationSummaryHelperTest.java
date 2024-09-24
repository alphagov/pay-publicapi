package uk.gov.pay.api.utils;

import org.junit.jupiter.api.Test;
import uk.gov.pay.api.model.AuthorisationSummary;
import uk.gov.pay.api.model.ThreeDSecure;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.pay.api.utils.AuthorisationSummaryHelper.includeAuthorisationSummaryWhen3dsRequired;

class AuthorisationSummaryHelperTest {
    @Test
    void shouldReturnNullWhenAuthorisationSummaryIsNull() {
        assertThat(includeAuthorisationSummaryWhen3dsRequired(null), is(nullValue()));
    }

    @Test
    void shouldReturnNullWhenThreeDSecureRequiredIsNull() {
        AuthorisationSummary authorisationSummary = new AuthorisationSummary(null);
        assertThat(includeAuthorisationSummaryWhen3dsRequired(authorisationSummary), is(nullValue()));
    }

    @Test
    void shouldReturnNullWhenThreeDSecureRequiredIsFalse() {
        AuthorisationSummary authorisationSummary = new AuthorisationSummary(new ThreeDSecure(false));
        assertThat(includeAuthorisationSummaryWhen3dsRequired(authorisationSummary), is(nullValue()));
    }

    @Test
    void shouldReturnAuthorisationSummaryWhenThreeDSecureRequiredIsTrue() {
        AuthorisationSummary authorisationSummary = new AuthorisationSummary(new ThreeDSecure(true));
        assertThat(includeAuthorisationSummaryWhen3dsRequired(authorisationSummary), is(authorisationSummary));
    }
}