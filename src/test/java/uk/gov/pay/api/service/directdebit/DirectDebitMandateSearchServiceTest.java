package uk.gov.pay.api.service.directdebit;

import au.com.dius.pact.consumer.PactVerification;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.app.RestClientFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.app.config.RestClientConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.search.directdebit.SearchMandateConnectorResponse;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.model.search.directdebit.DirectDebitSearchMandatesParams.DirectDebitSearchMandatesParamsBuilder.aDirectDebitSearchMandatesParams;

@RunWith(MockitoJUnitRunner.class)
public class DirectDebitMandateSearchServiceTest {

    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("direct-debit-connector", this);

    @Mock
    private PublicApiConfig configuration;
    
    DirectDebitMandateSearchService searchService;
    
    @Before
    public void setUp() {
        when(configuration.getConnectorDDUrl()).thenReturn(connectorRule.getUrl());
        Client client = RestClientFactory.buildClient(new RestClientConfig(false));
        searchService = new DirectDebitMandateSearchService(client, new DirectDebitConnectorUriGenerator(configuration));
    }

    @Test
    @PactVerification({"direct-debit-connector"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-search-mandates"})
    public void shouldGetMandateSearchResponseFromDirectDebitConnector() {
        Account account = new Account("9ddfcc27-acf5-43f9-92d5-52247540714c", TokenPaymentType.DIRECT_DEBIT);
        String expectedBankStatementReference = "410104";

        var searchParams = aDirectDebitSearchMandatesParams()
                .withBankStatementReference(expectedBankStatementReference)
                .build();

        SearchMandateConnectorResponse searchResponse = searchService.getMandatesFromDDConnector(account, searchParams);

        assertThat(searchResponse.getCount(), is(1));
        assertThat(searchResponse.getMandates().get(0).getMandateReference(), is(expectedBankStatementReference));
    }
}
