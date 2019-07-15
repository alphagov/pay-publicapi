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
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;

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
    void setUp() {
        when(configuration.getConnectorDDUrl()).thenReturn(connectorRule.getUrl());
        when(configuration.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");
        searchService = new DirectDebitMandateSearchService(
                RestClientFactory.buildClient(new RestClientConfig(false)),
                new DirectDebitConnectorUriGenerator(configuration));
    }
    
    @Test
    @PactVerification({"direct-debit-connector"})
    public void searchMandates() {
        Account account = new Account("TESTACCOUNTID", TokenPaymentType.DIRECT_DEBIT);
        var params = aDirectDebitSearchMandatesParams().build();
        searchService.searchMandates(account,params);
    }
}
