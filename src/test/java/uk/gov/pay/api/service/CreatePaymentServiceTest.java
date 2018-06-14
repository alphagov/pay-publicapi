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
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.links.Link;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;
import uk.gov.pay.api.model.links.PostLink;
import uk.gov.pay.api.pact.PactProviderRule;
import uk.gov.pay.api.pact.Pacts;

import javax.ws.rs.client.Client;
import java.util.Collections;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
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

        when(configuration.getBaseUrl()).thenReturn("http://publicapi.test.localhost/"); // Has to match what is in the Pact

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
        PaymentWithAllLinks paymentResponse = createPaymentService.invoke(account, requestPayload);

        assertThat(paymentResponse.getPaymentId(), is("ch_ab2341da231434l"));
        assertThat(paymentResponse.getPayment().getAmount(), is(100L));
        assertThat(paymentResponse.getPayment().getReference(), is("a reference"));
        assertThat(paymentResponse.getPayment().getDescription(), is("a description"));
        assertThat(paymentResponse.getPayment().getEmail(), is(nullValue()));
        assertThat(paymentResponse.getPayment().getState(), is(new PaymentState("created", false)));
        assertThat(paymentResponse.getPayment().getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentResponse.getPayment().getPaymentProvider(), is("Sandbox"));
        assertThat(paymentResponse.getPayment().getCreatedDate(), is("2016-01-01T12:00:00Z"));
        assertThat(paymentResponse.getLinks().getSelf(), is(new Link("http://publicapi.test.localhost/v1/payments/ch_ab2341da231434l", "GET")));
        assertThat(paymentResponse.getLinks().getNextUrl(), is(new Link("http://frontend_connector/charge/token_1234567asdf", "GET")));
        PostLink expectedLink = new PostLink("http://frontend_connector/charge/", "POST", "application/x-www-form-urlencoded", Collections.singletonMap("chargeTokenId", "token_1234567asdf"));
        assertThat(paymentResponse.getLinks().getNextUrlPost(), is(expectedLink));
    }

}
