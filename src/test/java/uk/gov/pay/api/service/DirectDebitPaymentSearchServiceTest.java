package uk.gov.pay.api.service;

import au.com.dius.pact.consumer.PactVerification;
import com.jayway.jsonassert.JsonAssert;
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
import uk.gov.pay.api.model.search.PaginationDecorator;
import uk.gov.pay.api.service.directdebit.DirectDebitConnectorUriGenerator;
import uk.gov.pay.api.service.directdebit.DirectDebitPaymentSearchService;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.core.Response;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.model.search.directdebit.DirectDebitSearchPaymentsParams.DirectDebitSearchPaymentsParamsBuilder.aDirectDebitSearchPaymentsParams;

@RunWith(MockitoJUnitRunner.class)
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

        directDebitPaymentSearchService = new DirectDebitPaymentSearchService(
                RestClientFactory.buildClient(new RestClientConfig(false)),
                new DirectDebitConnectorUriGenerator(configuration),
                new PublicApiUriGenerator(configuration),
                new PaginationDecorator(configuration));
    }

    @Test
    @PactVerification({"direct-debit-connector"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-search-by-mandate-three-results"})
    public void doSearchShouldReturnADirectDebitSearchResponseWithThreePayments() {
        Account account = new Account("2po9ycynwq8yxdgg2qwq9e9qpyrtre", TokenPaymentType.DIRECT_DEBIT);
        String mandateId = "jkdjsvd8f78ffkwfek2q";
        
        var searchParams =  aDirectDebitSearchPaymentsParams()
                .withMandateId(mandateId)
                .build();
        Response response = directDebitPaymentSearchService.doSearch(
                account,
                searchParams);
        
        JsonAssert.with(response.getEntity().toString())
                .assertThat("count", is(3))
                .assertThat("total", is(3))
                .assertThat("page", is(1))
                .assertThat("$.results.*", hasSize(3))
                .assertThat("$.results[0]", hasKey("amount"))
                .assertThat("$.results[1]", hasKey("amount"))
                .assertThat("$.results[2]", hasKey("amount"))
                .assertThat("$.results[0].state", hasKey("status"))
                .assertThat("$.results[0].state", hasKey("details"))
                .assertThat("$.results[0].state", hasKey("finished"))
                .assertThat("$.results[1].state", hasKey("status"))
                .assertThat("$.results[1].state", hasKey("details"))
                .assertThat("$.results[1].state", hasKey("finished"))
                .assertThat("$.results[2].state", hasKey("status"))
                .assertThat("$.results[2].state", hasKey("details"))
                .assertThat("$.results[2].state", hasKey("finished"))
                .assertThat("$.results.[0]", hasKey("mandate_id"))
                .assertThat("$.results.[1]", hasKey("mandate_id"))
                .assertThat("$.results.[2]", hasKey("mandate_id"))
                .assertThat("$.results.[0]", hasKey("description"))
                .assertThat("$.results.[1]", hasKey("description"))
                .assertThat("$.results.[2]", hasKey("description"))
                .assertThat("$.results.[0]", hasKey("reference"))
                .assertThat("$.results.[1]", hasKey("reference"))
                .assertThat("$.results.[2]", hasKey("reference"))
                .assertThat("$.results.[0]", hasKey("payment_id"))
                .assertThat("$.results.[1]", hasKey("payment_id"))
                .assertThat("$.results.[2]", hasKey("payment_id"))
                .assertThat("$.results.[0]", hasKey("payment_provider"))
                .assertThat("$.results.[1]", hasKey("payment_provider"))
                .assertThat("$.results.[2]", hasKey("payment_provider"))
                .assertThat("$.results.[0]", hasKey("provider_id"))
                .assertThat("$.results.[1]", hasKey("provider_id"))
                .assertThat("$.results.[2]", hasKey("provider_id"))
                .assertThat("$.results.[0]", hasKey("created_date"))
                .assertThat("$.results.[1]", hasKey("created_date"))
                .assertThat("$.results.[2]", hasKey("created_date"))
                .assertThat("_links", hasKey("self"))
                .assertThat("_links", hasKey("first_page"))
                .assertThat("_links", hasKey("last_page"))
                .assertNotDefined("_links.next_page")
                .assertNotDefined("_links.prev_page");
    }
}
