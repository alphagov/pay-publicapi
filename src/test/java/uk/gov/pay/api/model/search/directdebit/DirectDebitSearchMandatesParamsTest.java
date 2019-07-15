package uk.gov.pay.api.model.search.directdebit;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Test;

import java.time.ZonedDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.pay.api.model.search.directdebit.DirectDebitSearchMandatesParams.DirectDebitSearchMandatesParamsBuilder.aDirectDebitSearchMandatesParams;

public class DirectDebitSearchMandatesParamsTest {

    @Test
    public void paramsAsMap() {
        
        final var yesterday = ZonedDateTime.now().minusDays(1);
        final var tomorrow = ZonedDateTime.now().plusDays(1);
        
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
        
        assertThat(paramsAsMap, IsMapContaining.hasEntry("bank_statement_reference", "a bank statement reference"));
        assertThat(paramsAsMap, IsMapContaining.hasEntry("display_size", String.valueOf(10)));
        assertThat(paramsAsMap, IsMapContaining.hasEntry("email", "test@test.test"));
        assertThat(paramsAsMap, IsMapContaining.hasEntry("from_date", yesterday.toString()));
        assertThat(paramsAsMap, IsMapContaining.hasEntry("to_date", tomorrow.toString()));
        assertThat(paramsAsMap, IsMapContaining.hasEntry("name", "test"));
        assertThat(paramsAsMap, IsMapContaining.hasEntry("page", String.valueOf(1)));
        assertThat(paramsAsMap, IsMapContaining.hasEntry("reference", "a reference"));
        assertThat(paramsAsMap, IsMapContaining.hasEntry("state", "pending"));
        assertThat(paramsAsMap, IsMapContaining.hasEntry("reference", "a reference"));
    }
}
