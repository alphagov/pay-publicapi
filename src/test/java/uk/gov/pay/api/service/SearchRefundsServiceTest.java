package uk.gov.pay.api.service;

import au.com.dius.pact.consumer.PactVerification;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonassert.JsonAssert;
import org.hamcrest.Matchers;
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
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SearchRefundsServiceTest {

    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("connector", this);

    private Client client;

    @Mock
    private PublicApiConfig publicApiConfig;

    private RefundsUriGenerator refundsUriGenerator;
    private SearchRefundsService searchRefundsService;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() { 
        when(publicApiConfig.getConnectorUrl()).thenReturn(connectorRule.getUrl());
        when(publicApiConfig.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");

        refundsUriGenerator = new RefundsUriGenerator(publicApiConfig);
        
        client = RestClientFactory.buildClient(new RestClientConfig(false));
        objectMapper = new ObjectMapper();
        searchRefundsService = new SearchRefundsService(client, publicApiConfig, refundsUriGenerator, objectMapper);
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-search-refunds-with-page-and-display"}, publish = false)
    public void getAllRefundsShouldReturnCorrectTotalAndPageAndDisplaySize() {
        Account account = new Account("123456", TokenPaymentType.CARD);
        Response response = searchRefundsService.getAllRefunds(account, "1", "1");
        JsonAssert.with(response.getEntity().toString())
                .assertThat("count", Matchers.is(1))
                .assertThat("total", Matchers.is(1))
                .assertThat("page", Matchers.is(1));
    }
}
