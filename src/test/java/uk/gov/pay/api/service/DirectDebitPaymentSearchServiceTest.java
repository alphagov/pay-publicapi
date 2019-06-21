package uk.gov.pay.api.service;

import au.com.dius.pact.consumer.PactVerification;
import com.jayway.jsonassert.JsonAssert;
import org.junit.Before;
import org.junit.Ignore;
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
import uk.gov.pay.api.model.search.directdebit.SearchDirectDebitPayments;
import uk.gov.pay.api.service.directdebit.DirectDebitConnectorUriGenerator;
import uk.gov.pay.api.service.directdebit.DirectDebitPaymentSearchService;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.core.Response;

import static com.jayway.jsonassert.JsonAssert.collectionWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class DirectDebitPaymentSearchServiceTest {

    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("direct-debit-connector", this);

    @Mock
    private PublicApiConfig configuration;
    private DirectDebitPaymentSearchService directDebitPaymentSearchService;

    @Before
    public void setUp() {
        when(configuration.getConnectorDDUrl()).thenReturn(connectorRule.getUrl());

        when(configuration.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");

        SearchDirectDebitPayments searchDirectDebitPayments = new SearchDirectDebitPayments(
                RestClientFactory.buildClient(new RestClientConfig(false)),
                configuration,
                new ConnectorUriGenerator(configuration),
                new DirectDebitConnectorUriGenerator(configuration),
                new PaymentUriGenerator());
        directDebitPaymentSearchService = new DirectDebitPaymentSearchService(searchDirectDebitPayments);
    }

    @Test
    @PactVerification({"direct-debit-connector"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-search-by-mandate-three-results"}, publish = false)
    public void doSearchShouldReturnADirectDebitSearchResponseWithThreePayments() {
        Account account = new Account("2po9ycynwq8yxdgg2qwq9e9qpyrtre", TokenPaymentType.DIRECT_DEBIT);
        String mandateId = "jkdjsvd8f78ffkwfek2q";
        Response response = directDebitPaymentSearchService.doSearch(
                account,
                null,
                null,
                mandateId,
                null,
                null,
                null,
                null);
        JsonAssert.with(response.getEntity().toString())
                .assertThat("count", is(3))
                .assertThat("total", is(3))
                .assertThat("page", is(1))
                .assertThat("results", is(collectionWithSize(equalTo(3))))
                .assertThat("results[0]", hasKey("amount"))
                .assertThat("results[1]", hasKey("state"))
                .assertThat("results[2]", hasKey("reference"))
                .assertThat("results[2]", hasKey("name"))
                .assertThat("results[1]", hasKey("email"))
                .assertThat("results[0].state", hasKey("status"))
                .assertThat("results[2].state", hasKey("finished"))
                .assertThat("results[0]", hasKey("links"))
                .assertThat("results[1]", hasKey("agreement_id"))
                .assertThat("results[2].agreement_id", is(mandateId))
                .assertThat("_links", hasKey("self"))
                .assertThat("_links", hasKey("first_page"))
                .assertThat("_links", hasKey("last_page"))
                .assertThat("_links.self.href", containsString(mandateId))
                .assertNotDefined("_links.next_page")
                .assertNotDefined("_links.prev_page");
    }
}
