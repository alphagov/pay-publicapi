package uk.gov.pay.api.service;

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
import uk.gov.pay.api.exception.DisputesValidationException;
import uk.gov.pay.api.ledger.service.LedgerUriGenerator;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.search.PaginationDecorator;
import uk.gov.pay.api.model.search.dispute.DisputeForSearchResult;
import uk.gov.pay.api.model.search.dispute.DisputeSearchResults;
import uk.gov.service.payments.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.service.payments.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchDisputesServiceTest {
    @Rule
    public PactProviderRule ledgerRule = new PactProviderRule("ledger", this);

    @Mock
    private PublicApiConfig mockConfiguration;

    private SearchDisputesService service;

    private static final String accountId = "123456";
    private static final String tokenLink = "a-token-link";

    @Before
    public void setUp() {
        when(mockConfiguration.getLedgerUrl()).thenReturn(ledgerRule.getUrl());
        when(mockConfiguration.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");

        Client client = RestClientFactory.buildClient(new RestClientConfig(false));
        LedgerUriGenerator ledgerUriGenerator = new LedgerUriGenerator(mockConfiguration);

        service = new SearchDisputesService(new LedgerService(client, ledgerUriGenerator),
                new PublicApiUriGenerator(mockConfiguration),
                new PaginationDecorator(mockConfiguration));
    }

    @Test
    @PactVerification({"ledger"})
    @Pacts(pacts = {"publicapi-ledger-search-disputes_by_settled_dates"})
    public void searchDisputesBySettledDatesShouldReturnFromLedger() {
        DisputesParams.Builder paramsBuilder = new DisputesParams.Builder();
        DisputesParams params = paramsBuilder
                .withFromSettledDate("2022-05-27")
                .withToSettledDate("2022-05-27")
                .build();
        Account account = new Account(accountId, TokenPaymentType.CARD, tokenLink);
        DisputeSearchResults results = service.validateAndSearchDisputes(account, params);
        assertThat(results.getResults().size(), is(1));
        assertThat(results.getCount(), is(1));
        assertThat(results.getPage(), is(1));
        assertThat(results.getTotal(), is(1));
        assertThat(results.getLinks().getSelf().getHref(), is("http://publicapi.test.localhost/v1/disputes/?from_settled_date=2022-05-27&to_settled_date=2022-05-27&display_size=500&page=1"));
        assertThat(results.getLinks().getFirstPage().getHref(), is("http://publicapi.test.localhost/v1/disputes/?from_settled_date=2022-05-27&to_settled_date=2022-05-27&display_size=500&page=1"));
        assertThat(results.getLinks().getLastPage().getHref(), is("http://publicapi.test.localhost/v1/disputes/?from_settled_date=2022-05-27&to_settled_date=2022-05-27&display_size=500&page=1"));

        DisputeForSearchResult dispute = results.getResults().get(0);
        assertThat(dispute.getDisputeId(), is("dispute97837509646393e3C"));
        assertThat(dispute.getReason(), is("fraudulent"));
        assertThat(dispute.getAmount(), is(1000L));
        assertThat(dispute.getFee(), is(1500L));
        assertThat(dispute.getNetAmount(), is(-2500L));
        assertThat(dispute.getPaymentId(), is("parent-abcde-12345"));
        assertThat(dispute.getCreatedDate(), is("2022-05-20T19:05:00.000Z"));
        assertThat(dispute.getEvidenceDueDate(), is("2022-05-27T19:05:00.000Z"));
        assertThat(dispute.getSettlementSummary().getSettledDate(), is("2022-05-27"));
        assertThat(dispute.getStatus(), is("lost"));
        assertThat(dispute.getLinks().getPayment().getHref(), is("http://publicapi.test.localhost/v1/payments/parent-abcde-12345"));
    }

    @Test
    public void getSearchResponse_shouldThrowDisputesValidationExceptionWhenParamsAreInvalid() {
        Account account = new Account(accountId, TokenPaymentType.CARD, tokenLink);
        String invalid = "invalid_param";
        DisputesParams.Builder builder = new DisputesParams.Builder();
        DisputesParams params = builder
                .withDisplaySize(invalid)
                .withPage(invalid)
                .withToSettledDate(invalid)
                .build();

        DisputesValidationException disputesValidationException = assertThrows(DisputesValidationException.class,
                () -> service.validateAndSearchDisputes(account, builder.build()));

        assertThat(disputesValidationException.getRequestError().getCode(), is("P0401"));
        assertThat(disputesValidationException.getRequestError().getDescription(), is("Invalid parameters: to_settled_date, page, display_size. See Public API documentation for the correct data formats"));
    }
}