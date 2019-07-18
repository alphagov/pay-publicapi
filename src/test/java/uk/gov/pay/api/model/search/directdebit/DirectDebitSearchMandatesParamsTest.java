package uk.gov.pay.api.model.search.directdebit;

import org.junit.Test;

import java.time.ZonedDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static uk.gov.pay.api.model.search.directdebit.DirectDebitSearchMandatesParams.DirectDebitSearchMandatesParamsBuilder.aDirectDebitSearchMandatesParams;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class DirectDebitSearchMandatesParamsTest {

    @Test
    public void shouldCreateMapWithAllValues() {

        final var yesterday = ISO_INSTANT_MILLISECOND_PRECISION.format(ZonedDateTime.now().minusDays(1));
        final var tomorrow = ISO_INSTANT_MILLISECOND_PRECISION.format(ZonedDateTime.now().plusDays(1));

        var params = aDirectDebitSearchMandatesParams()
                .withBankStatementReference("a bank statement reference")
                .withDisplaySize(10)
                .withEmail("test@test.test")
                .withFromDate(yesterday)
                .withToDate(tomorrow)
                .withName("test")
                .withPage(1)
                .withReference("a reference")
                .withState("pending")
                .build();

        var paramsAsMap = params.paramsAsMap();

        assertThat(paramsAsMap, hasEntry("bank_statement_reference", "a bank statement reference"));
        assertThat(paramsAsMap, hasEntry("display_size", String.valueOf(10)));
        assertThat(paramsAsMap, hasEntry("email", "test@test.test"));
        assertThat(paramsAsMap, hasEntry("from_date", yesterday.toString()));
        assertThat(paramsAsMap, hasEntry("to_date", tomorrow.toString()));
        assertThat(paramsAsMap, hasEntry("name", "test"));
        assertThat(paramsAsMap, hasEntry("page", String.valueOf(1)));
        assertThat(paramsAsMap, hasEntry("reference", "a reference"));
        assertThat(paramsAsMap, hasEntry("state", "pending"));
        assertThat(paramsAsMap, hasEntry("reference", "a reference"));
    }
}
