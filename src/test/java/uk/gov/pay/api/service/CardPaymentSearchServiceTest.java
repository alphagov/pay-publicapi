package uk.gov.pay.api.service;

import au.com.dius.pact.consumer.PactVerification;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static com.jayway.jsonassert.JsonAssert.collectionWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CardPaymentSearchServiceTest {

    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("connector", this);

    private Client client;

    @Mock
    private PublicApiConfig configuration;
    private ConnectorUriGenerator connectorUriGenerator;
    private PaymentSearchService paymentSearchService;
    private PaymentUriGenerator paymentUriGenerator;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        when(configuration.getConnectorUrl()).thenReturn(connectorRule.getUrl());

        when(configuration.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");

        connectorUriGenerator = new ConnectorUriGenerator(configuration);
        paymentUriGenerator = new PaymentUriGenerator();
        client = RestClientFactory.buildClient(new RestClientConfig(false));
        objectMapper = new ObjectMapper();
        paymentSearchService = new PaymentSearchService(client, configuration, connectorUriGenerator, paymentUriGenerator, objectMapper);
    }
    
    @Ignore
    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-search-payment-by-last-digits-card-number"})
    public void searchShouldReturnAResponseWithOneTransaction_whenFilteringByLastDigitsCardNumber() {
        Account account = new Account("123456", TokenPaymentType.CARD);
        Response response =
                paymentSearchService.doSearch(account, null, null,
                        null, null, null,
                        null, null, null,
                        null, null, null, "1234");
        JsonAssert.with(response.getEntity().toString())
                .assertThat("count", is(1))
                .assertThat("total", is(1))
                .assertThat("page", is(1))
                .assertThat("results", is(collectionWithSize(equalTo(1))))
                .assertThat("results[0]", hasKey("amount"))
                .assertThat("results[0]", hasKey("state"))
                .assertThat("results[0]", hasKey("reference"))
                .assertThat("results[0]", hasKey("email"))
                .assertThat("results[0].card_details", hasKey("cardholder_name"))
                .assertThat("results[0].card_details", hasKey("first_digits_card_number"))
                .assertThat("results[0].card_details.last_digits_card_number", is("1234"))
                .assertThat("results[0].state", hasKey("status"))
                .assertThat("results[0].state", hasKey("finished"))
                .assertThat("results[0]", hasKey("_links"))
                .assertThat("_links", hasKey("self"))
                .assertThat("_links", hasKey("first_page"))
                .assertThat("_links", hasKey("last_page"))
                .assertNotDefined("_links.next_page")
                .assertNotDefined("_links.prev_page");
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-search-payment-by-first-digits-card-number"})
    public void searchShouldReturnAResponseWithOneTransaction_whenFilteringByFirstDigitsCardNumber() {
        Account account = new Account("123456", TokenPaymentType.CARD);
        Response response =
                paymentSearchService.doSearch(account, null, null,
                        null, null, null,
                        null, null, null,
                        null, null, "123456", null);
        JsonAssert.with(response.getEntity().toString())
                .assertThat("count", is(1))
                .assertThat("total", is(1))
                .assertThat("page", is(1))
                .assertThat("results", is(collectionWithSize(equalTo(1))))
                .assertThat("results[0]", hasKey("amount"))
                .assertThat("results[0]", hasKey("state"))
                .assertThat("results[0]", hasKey("reference"))
                .assertThat("results[0]", hasKey("email"))
                .assertThat("results[0].card_details", hasKey("cardholder_name"))
                .assertThat("results[0].card_details.first_digits_card_number", is("123456"))
                .assertThat("results[0].card_details", hasKey("last_digits_card_number"))
                .assertThat("results[0].state", hasKey("status"))
                .assertThat("results[0].state", hasKey("finished"))
                .assertThat("results[0]", hasKey("_links"))
                .assertThat("_links", hasKey("self"))
                .assertThat("_links", hasKey("first_page"))
                .assertThat("_links", hasKey("last_page"))
                .assertNotDefined("_links.next_page")
                .assertNotDefined("_links.prev_page");
    }
}
