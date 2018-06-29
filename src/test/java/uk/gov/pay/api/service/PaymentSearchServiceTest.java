package uk.gov.pay.api.service;

import au.com.dius.pact.consumer.PactVerification;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static com.jayway.jsonassert.JsonAssert.collectionWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentSearchServiceTest {

    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("direct-debit-connector", this);

    private Client client;

    @Mock
    private PublicApiConfig configuration;
    private ConnectorUriGenerator connectorUriGenerator;
    private PaymentSearchService paymentSearchService;
    private PaymentUriGenerator paymentUriGenerator;
    private ObjectMapper objectMapper;
    
    @Before
    public void setUp() {
        when(configuration.getConnectorDDUrl()).thenReturn(connectorRule.getUrl());

        when(configuration.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");

        connectorUriGenerator = new ConnectorUriGenerator(configuration);
        paymentUriGenerator = new PaymentUriGenerator();
        client = RestClientFactory.buildClient(new RestClientConfig(false));
        objectMapper = new ObjectMapper();
        paymentSearchService = new PaymentSearchService(client, configuration, connectorUriGenerator, paymentUriGenerator, objectMapper);
    }
    
    @Test
    public void doSearchShouldThrowBadRequestException_whenAccountIsNotDD_andAgreementIsASearchParam() {
        Account account = new Account("an account", TokenPaymentType.CARD);
        String agreementId = "an-agreement-id";
        try {
            paymentSearchService.doSearch(account, null, null, null, null, null,
                    null, null, null, agreementId);
        } catch (uk.gov.pay.api.exception.BadRequestException ex) {
            assertThat(ex.getPaymentError().getCode(), is("P0401"));
            assertThat(ex.getPaymentError().getDescription().contains("Invalid parameters: agreement_id."), is(true));
        }
    }
    
    @Test
    @PactVerification({"direct-debit-connector"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-search-by-mandate-three-results"})
    public void doSearchShouldReturnADirectDebitSearchResponseWithThreeTransactions() {
        Account account = new Account("2po9ycynwq8yxdgg2qwq9e9qpyrtre", TokenPaymentType.DIRECT_DEBIT);
        String agreementId = "jkdjsvd8f78ffkwfek2q";
        Response response = 
                paymentSearchService.doSearch(account, null, null, 
                        null, null, null, 
                        null, null, null,
                        agreementId);
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
                .assertThat("_links", hasKey("self"))
                .assertThat("_links", hasKey("first_page"))
                .assertThat("_links", hasKey("last_page"))
                .assertThat("_links.self.href", containsString(agreementId))
                .assertNotDefined("_links.next_page")
                .assertNotDefined("_links.prev_page");
    }
}
