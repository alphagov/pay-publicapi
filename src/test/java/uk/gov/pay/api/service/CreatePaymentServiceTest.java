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
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.pact.PactProviderRule;
import uk.gov.pay.api.pact.Pacts;

import javax.ws.rs.client.Client;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreatePaymentServiceTest {
    private CreatePaymentService createPaymentService;

    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("connector", this);

    private Client client;

    @Mock
    private PublicApiConfig configuration;

    private PublicApiUriGenerator publicApiUriGenerator;
    private ConnectorUriGenerator connectorUriGenerator;
    
    @Before
    public void setup() {
        when(configuration.getConnectorUrl()).thenReturn(connectorRule.getUrl()); // We will actually send real requests here, which will be intercepted by pact

        when(configuration.getBaseUrl()).thenReturn("http://localhost/"); // Has to match what is in the Pact

        // These can be concrete implementations, because they're simple
        publicApiUriGenerator = new PublicApiUriGenerator();
        connectorUriGenerator = new ConnectorUriGenerator(configuration);
        client = RestClientFactory.buildClient(new RestClientConfig(false));

        createPaymentService = new CreatePaymentService(client, configuration, publicApiUriGenerator, connectorUriGenerator);
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector"})
    public void testCreatePayment() {
        Account account = new Account("GATEWAY_ACCOUNT_ID", TokenPaymentType.CARD);
        CreatePaymentRequest requestPayload = new CreatePaymentRequest(100, "https://somewhere.gov.uk/rainbow/1", "a reference", "a description");
        createPaymentService.invoke(account, requestPayload);
    }

}
