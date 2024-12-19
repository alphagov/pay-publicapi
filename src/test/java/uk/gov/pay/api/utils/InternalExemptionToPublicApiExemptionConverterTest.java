package uk.gov.pay.api.utils;

import org.junit.jupiter.api.Test;
import uk.gov.pay.api.model.Exemption;
import uk.gov.pay.api.model.ExemptionOutcome;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class InternalExemptionToPublicApiExemptionConverterTest {

    @Test
    void shouldReturnNull_whenExemptionIsNull() {
        assertThat(InternalExemptionToPublicApiExemptionConverter.convertExemption(null), is(nullValue()));
    }

    @Test
    void shouldReturnNull_whenExemptionRequestedButOutcomeNotYetKnown() {
        Exemption exemption = new Exemption(true, null, null);
        assertThat(InternalExemptionToPublicApiExemptionConverter.convertExemption(exemption), is(nullValue()));
    }

    @Test
    void shouldReturnNull_whenNonCorporateExemptionIsHonoured() {
        Exemption exemption = new Exemption(true, null, new ExemptionOutcome("honoured"));
        assertThat(InternalExemptionToPublicApiExemptionConverter.convertExemption(exemption), is(nullValue()));
    }

    @Test
    void shouldReturnNull_whenNonCorporateExemptionIsRejected() {
        Exemption exemption = new Exemption(true, null, new ExemptionOutcome("rejected"));
        assertThat(InternalExemptionToPublicApiExemptionConverter.convertExemption(exemption), is(nullValue()));
    }

    @Test
    void shouldReturnNull_whenNonCorporateExemptionIsOutOfScope() {
        Exemption exemption = new Exemption(true, null, new ExemptionOutcome("out of scope"));
        assertThat(InternalExemptionToPublicApiExemptionConverter.convertExemption(exemption), is(nullValue()));
    }

    @Test
    void shouldReturnNull_whenOutcomeIsNotKnown() {
        Exemption exemption = new Exemption(true, "corporate", null);
        assertThat(InternalExemptionToPublicApiExemptionConverter.convertExemption(exemption), is(nullValue()));
    }

    @Test
    void shouldReturnNull_whenTypeIsCorporateAndOutcomeIsOutOfScope() {
        Exemption exemption = new Exemption(true, "corporate", new ExemptionOutcome("out of scope"));
        assertThat(InternalExemptionToPublicApiExemptionConverter.convertExemption(exemption), is(exemption));
    }

    @Test
    void shouldReturnNull_whenTypeIsCorporateAndOutcomeIsRejected() {
        Exemption exemption = new Exemption(true, "corporate", new ExemptionOutcome("rejected"));
        assertThat(InternalExemptionToPublicApiExemptionConverter.convertExemption(exemption), is(exemption));
    }

    @Test
    void shouldReturnNull_whenRequestedIsFalse() {
        Exemption exemption = new Exemption(false, null, null);
        assertThat(InternalExemptionToPublicApiExemptionConverter.convertExemption(exemption), is(nullValue()));
    }
}
