package uk.gov.pay.api.service;

import au.com.dius.pact.consumer.PactVerification;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.agreement.model.AgreementSearchResults;
import uk.gov.pay.api.app.RestClientFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.app.config.RestClientConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.ledger.model.AgreementSearchParams;
import uk.gov.pay.api.ledger.service.LedgerUriGenerator;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.search.PaginationDecorator;
import uk.gov.service.payments.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.service.payments.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchAgreementsServicePactTest {
    @Rule
    public PactProviderRule ledgerRule = new PactProviderRule("ledger", this);
    @Mock
    private PublicApiConfig mockConfiguration;

    private SearchAgreementsService searchAgreementsService;

    private static final String PUBLIC_API_URL = "http://publicapi.test.localhost/";

    @Before
    public void setUp() {
        when(mockConfiguration.getLedgerUrl()).thenReturn(ledgerRule.getUrl());
        when(mockConfiguration.getBaseUrl()).thenReturn(PUBLIC_API_URL);

        Client client = RestClientFactory.buildClient(new RestClientConfig(false));
        LedgerUriGenerator ledgerUriGenerator = new LedgerUriGenerator(mockConfiguration);

        searchAgreementsService = new SearchAgreementsService(
                new LedgerService(client, ledgerUriGenerator),
                new PaginationDecorator(mockConfiguration));
    }

    @Test
    @PactVerification({"ledger"})
    @Pacts(pacts = {"publicapi-ledger-search-agreements-with-page-and-display-size"})
    public void searchWithPageAndDisplaySize_shouldReturnCorrectPageAndPaginationLinks() {
        AgreementSearchParams params = new AgreementSearchParams(null, null, "2", "1");
        Account account = new Account("777", TokenPaymentType.CARD, "a-token-link");
        AgreementSearchResults results = searchAgreementsService.searchLedgerAgreements(account, params);

        assertThat(results.getResults().size(), is(1));
        assertThat(results.getCount(), is(1));
        assertThat(results.getTotal(), is(3));
        assertThat(results.getPage(), is(2));
        assertThat(results.getLinks().getSelf().getHref(), is(PUBLIC_API_URL + "v1/agreements?display_size=1&page=2"));
        assertThat(results.getLinks().getFirstPage().getHref(), is(PUBLIC_API_URL + "v1/agreements?display_size=1&page=1"));
        assertThat(results.getLinks().getLastPage().getHref(), is(PUBLIC_API_URL + "v1/agreements?display_size=1&page=3"));
        assertThat(results.getLinks().getPrevPage().getHref(), is(PUBLIC_API_URL + "v1/agreements?display_size=1&page=1"));
        assertThat(results.getLinks().getNextPage().getHref(), is(PUBLIC_API_URL + "v1/agreements?display_size=1&page=3"));
    }
}
