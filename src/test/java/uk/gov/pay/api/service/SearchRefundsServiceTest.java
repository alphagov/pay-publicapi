package uk.gov.pay.api.service;

import au.com.dius.pact.consumer.PactVerification;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.app.RestClientFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.app.config.RestClientConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.RefundsValidationException;
import uk.gov.pay.api.ledger.service.LedgerUriGenerator;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.search.PaginationDecorator;
import uk.gov.pay.api.model.search.card.SearchRefundsResults;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;

import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.matcher.RefundValidationExceptionMatcher.aValidationExceptionContaining;

@RunWith(MockitoJUnitRunner.class)
public class SearchRefundsServiceTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();
    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("connector", this);
    @Rule
    public PactProviderRule ledgerRule = new PactProviderRule("ledger", this);
    @Mock
    private PublicApiConfig mockConfiguration;

    private SearchRefundsService searchRefundsService;
    private String ACCOUNT_ID = "888";

    @Before
    public void setUp() {
        when(mockConfiguration.getConnectorUrl()).thenReturn(connectorRule.getUrl());
        when(mockConfiguration.getLedgerUrl()).thenReturn(ledgerRule.getUrl());
        when(mockConfiguration.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");

        Client client = RestClientFactory.buildClient(new RestClientConfig(false));
        ConnectorUriGenerator connectorUriGenerator = new ConnectorUriGenerator(mockConfiguration);
        LedgerUriGenerator ledgerUriGenerator = new LedgerUriGenerator(mockConfiguration);

        searchRefundsService = new SearchRefundsService(
                new ConnectorService(client, connectorUriGenerator),
                new LedgerService(client, ledgerUriGenerator),
                new PublicApiUriGenerator(mockConfiguration),
                new PaginationDecorator(mockConfiguration));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-search-refunds-with-page-and-display"})
    public void getAllRefundsShouldReturnCorrectTotalAndPageAndDisplaySize() {
        RefundsParams params = new RefundsParams(null, null, "1", "2");
        String accountId = "777";
        String refundId1 = "111111";
        String refundId2 = "222222";
        String extChargeId = "someExternalId";
        Account account = new Account(accountId, TokenPaymentType.CARD);
        SearchRefundsResults results = searchRefundsService.searchConnectorRefunds(account, params);

        assertThat(results.getResults().size(), is(2));
        assertThat(results.getCount(), is(2));
        assertThat(results.getTotal(), is(2));
        assertThat(results.getPage(), is(1));
        assertThat(results.getResults().get(0).getStatus(), is("available"));
        assertThat(results.getResults().get(0).getCreatedDate(), is("2017-10-01T01:41:01Z"));
        assertThat(results.getResults().get(0).getRefundId(), is(refundId1));
        assertThat(results.getResults().get(0).getChargeId(), is(extChargeId));
        assertThat(results.getResults().get(0).getAmount(), is(98L));
        assertThat(results.getResults().get(0).getLinks().getSelf().getHref(), is(format("http://publicapi.test.localhost/v1/payments/%s/refunds/%s", extChargeId, refundId1)));
        assertThat(results.getResults().get(0).getLinks().getPayment().getHref(), is(format("http://publicapi.test.localhost/v1/payments/%s", extChargeId)));

        assertThat(results.getResults().get(1).getStatus(), is("available"));
        assertThat(results.getResults().get(1).getCreatedDate(), is("2017-09-02T02:42:02Z"));
        assertThat(results.getResults().get(1).getRefundId(), is(refundId2));
        assertThat(results.getResults().get(1).getChargeId(), is(extChargeId));
        assertThat(results.getResults().get(1).getAmount(), is(100L));
        assertThat(results.getResults().get(1).getLinks().getSelf().getHref(), is(format("http://publicapi.test.localhost/v1/payments/%s/refunds/%s", extChargeId, refundId2)));
        assertThat(results.getResults().get(1).getLinks().getPayment().getHref(), is(format("http://publicapi.test.localhost/v1/payments/%s", extChargeId)));

        assertThat(results.getLinks().getSelf().getHref(), is("http://publicapi.test.localhost/v1/refunds?display_size=2&page=1"));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-search-refunds-with-page-and-display-when-no-refunds-exist"})
    public void getAllRefundsShouldReturnNoRefundsWhenThereAreNone() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);
        RefundsParams params = new RefundsParams(null, null, "1", "1");
        SearchRefundsResults results = searchRefundsService.searchConnectorRefunds(account, params);
        assertThat(results.getCount(), is(0));
        assertThat(results.getTotal(), is(0));
        assertThat(results.getPage(), is(1));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-search-refunds-with-from-and-to-date"})
    public void getAllRefundsShouldReturnCorrectFromAndToDate() {
        RefundsParams params = new RefundsParams("2016-01-25T13:22:55Z", "2016-01-25T13:24:55Z", "1", "500");
        String accountId = "777";
        String refundId1 = "111111";
        String refundId2 = "222222";
        String extChargeId = "someExternalId";
        Account account = new Account(accountId, TokenPaymentType.CARD);
        SearchRefundsResults results = searchRefundsService.searchConnectorRefunds(account, params);

        assertThat(results.getResults().size(), is(2));
        assertThat(results.getCount(), is(2));
        assertThat(results.getTotal(), is(2));
        assertThat(results.getPage(), is(1));
        assertThat(results.getResults().get(0).getStatus(), is("available"));
        assertThat(results.getResults().get(0).getCreatedDate(), is("2016-01-25T13:23:55Z"));
        assertThat(results.getResults().get(0).getRefundId(), is(refundId1));
        assertThat(results.getResults().get(0).getChargeId(), is(extChargeId));
        assertThat(results.getResults().get(0).getAmount(), is(98L));
        assertThat(results.getResults().get(0).getLinks().getSelf().getHref(), is(format("http://publicapi.test.localhost/v1/payments/%s/refunds/%s", extChargeId, refundId1)));
        assertThat(results.getResults().get(0).getLinks().getPayment().getHref(), is(format("http://publicapi.test.localhost/v1/payments/%s", extChargeId)));

        assertThat(results.getResults().get(1).getStatus(), is("available"));
        assertThat(results.getResults().get(1).getCreatedDate(), is("2016-01-25T13:23:55Z"));
        assertThat(results.getResults().get(1).getRefundId(), is(refundId2));
        assertThat(results.getResults().get(1).getChargeId(), is(extChargeId));
        assertThat(results.getResults().get(1).getAmount(), is(100L));
        assertThat(results.getResults().get(1).getLinks().getSelf().getHref(), is(format("http://publicapi.test.localhost/v1/payments/%s/refunds/%s", extChargeId, refundId2)));
        assertThat(results.getResults().get(1).getLinks().getPayment().getHref(), is(format("http://publicapi.test.localhost/v1/payments/%s", extChargeId)));

        assertThat(results.getLinks().getSelf().getHref(), is("http://publicapi.test.localhost/v1/refunds?from_date=2016-01-25T13%3A22%3A55Z&to_date=2016-01-25T13%3A24%3A55Z&display_size=500&page=1"));
    }

    @Test
    public void getSearchResponse_shouldThrowRefundsValidationExceptionWhenParamsAreInvalid() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);
        String invalid = "invalid_param";
        RefundsParams params = new RefundsParams(null, null, invalid, invalid);

        expectedException.expect(RefundsValidationException.class);
        expectedException.expect(aValidationExceptionContaining(
                "P1101",
                format("Invalid parameters: %s. See Public API documentation for the correct data formats",
                        "page, display_size")));
        searchRefundsService.searchConnectorRefunds(account, params);
    }

    @Test
    @PactVerification({"ledger"})
    @Pacts(pacts = {"publicapi-ledger-search-refunds"})
    public void getAllRefundsShouldReturnCorrectFromAndToDateFromLedger() {
        RefundsParams params = new RefundsParams("2018-09-21T13:22:55Z", "2018-10-23T13:24:55Z", "1", "500");
        String accountId = "777";
        String refundId1 = "111111";
        String refundId2 = "222222";
        Account account = new Account(accountId, TokenPaymentType.CARD);
        SearchRefundsResults results = searchRefundsService.searchLedgerRefunds(account, params);

        assertThat(results.getResults().size(), is(2));
        assertThat(results.getCount(), is(2));
        assertThat(results.getTotal(), is(2));
        assertThat(results.getPage(), is(1));
        assertThat(results.getResults().get(0).getStatus(), is("success"));
        assertThat(results.getResults().get(0).getCreatedDate(), is("2018-09-22T10:14:16.067Z"));
        assertThat(results.getResults().get(0).getRefundId(), is(refundId1));
        assertThat(results.getResults().get(0).getChargeId(), is("someExternalId1"));
        assertThat(results.getResults().get(0).getAmount(), is(150L));
        assertThat(results.getResults().get(0).getLinks().getSelf().getHref(), is(format("http://publicapi.test.localhost/v1/payments/someExternalId1/refunds/%s", refundId1)));
        assertThat(results.getResults().get(0).getLinks().getPayment().getHref(), is("http://publicapi.test.localhost/v1/payments/someExternalId1"));

        assertThat(results.getResults().get(1).getStatus(), is("success"));
        assertThat(results.getResults().get(1).getCreatedDate(), is("2018-10-22T10:16:16.067Z"));
        assertThat(results.getResults().get(1).getRefundId(), is(refundId2));
        assertThat(results.getResults().get(1).getChargeId(), is("someExternalId2"));
        assertThat(results.getResults().get(1).getAmount(), is(250L));
        assertThat(results.getResults().get(1).getLinks().getSelf().getHref(), is(format("http://publicapi.test.localhost/v1/payments/someExternalId2/refunds/%s", refundId2)));
        assertThat(results.getResults().get(1).getLinks().getPayment().getHref(), is("http://publicapi.test.localhost/v1/payments/someExternalId2"));

        assertThat(results.getLinks().getSelf().getHref(), is("http://publicapi.test.localhost/v1/refunds?from_date=2018-09-21T13%3A22%3A55Z&to_date=2018-10-23T13%3A24%3A55Z&display_size=500&page=1"));
    }

    @Test
    public void getSearchResponseFromLedger_shouldThrowRefundsValidationExceptionWhenParamsAreInvalid() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);
        String invalid = "invalid_param";
        RefundsParams params = new RefundsParams(null, null, invalid, invalid);

        expectedException.expect(RefundsValidationException.class);
        expectedException.expect(aValidationExceptionContaining(
                "P1101",
                format("Invalid parameters: %s. See Public API documentation for the correct data formats",
                        "page, display_size")));
        searchRefundsService.searchLedgerRefunds(account, params);
    }
}
