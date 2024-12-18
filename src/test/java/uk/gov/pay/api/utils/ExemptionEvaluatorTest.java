package uk.gov.pay.api.utils;

import org.junit.jupiter.api.Test;
import uk.gov.pay.api.model.Exemption;
import uk.gov.pay.api.model.ExemptionOutcome;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class ExemptionEvaluatorTest {

    @Test
    void shouldReturnNull_whenExemptionIsNull() {
        assertThat(ExemptionEvaluator.evaluateExemption(null), is(nullValue()));
    }

    @Test
    void shouldReturnNull_whenOutcomeIsNotKnown() {
        Exemption exemption = new Exemption(true, "corporate", null);
        assertThat(ExemptionEvaluator.evaluateExemption(exemption), is(nullValue()));
    }

    @Test
    void shouldReturnNull_whenTypeIsNull() {
        Exemption exemption = new Exemption(true, null, new ExemptionOutcome("honoured"));
        assertThat(ExemptionEvaluator.evaluateExemption(exemption), is(nullValue()));
    }

    @Test
    void shouldReturnNull_whenRequestedIsNull() {
        Exemption exemption = new Exemption(false, null, null);
        assertThat(ExemptionEvaluator.evaluateExemption(exemption), is(nullValue()));
    }

    @Test
    void shouldReturnExemption_whenTypeIsCorporateAndOutcomeIsKnown() {
        Exemption exemption = new Exemption(true, "corporate", new ExemptionOutcome("rejected"));
        assertThat(ExemptionEvaluator.evaluateExemption(exemption), is(exemption));
    }
}