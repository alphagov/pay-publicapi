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
import uk.gov.pay.api.model.PaymentEventsResponse;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetPaymentEventsServiceTest {

    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("connector", this);

    @Mock
    private PublicApiConfig mockConfiguration;

    private GetPaymentEventsService getPaymentEventsService;
    private static final String ACCOUNT_ID = "42";

    @Before
    public void setUp() throws Exception {
        when(mockConfiguration.getConnectorUrl()).thenReturn(connectorRule.getUrl());
        when(mockConfiguration.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");

        PublicApiUriGenerator publicApiUriGenerator = new PublicApiUriGenerator(mockConfiguration);
        ConnectorUriGenerator connectorUriGenerator = new ConnectorUriGenerator(mockConfiguration);
        Client client = RestClientFactory.buildClient(new RestClientConfig(false));

        getPaymentEventsService = new GetPaymentEventsService(client, connectorUriGenerator, publicApiUriGenerator);
    }

    @Test
    @PactVerification("connector")
    @Pacts(pacts = {"publicapi-connector-get-payment-events"})
    public void shouldReturnPaymentEvents() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);
        PaymentEventsResponse paymentEventsResponse = getPaymentEventsService.getPaymentEvent(account, "abc123");
        assertThat(paymentEventsResponse.getPaymentId(), is("abc123"));
        assertThat(paymentEventsResponse.getLinks().getSelf().getHref(), is("http://publicapi.test.localhost/v1/payments/abc123/events"));
        assertThat(paymentEventsResponse.getEvents().size(), is(2));
        assertThat(paymentEventsResponse.getEvents().get(0).getPaymentId(), is("abc123"));
        assertThat(paymentEventsResponse.getEvents().get(0).getPaymentLink().getPaymentLink().getHref(), is("http://publicapi.test.localhost/v1/payments/abc123"));
        assertThat(paymentEventsResponse.getEvents().get(0).getState().getStatus(), is("created"));
        assertThat(paymentEventsResponse.getEvents().get(0).getState().isFinished(), is(false));
        assertThat(paymentEventsResponse.getEvents().get(0).getUpdated(), is("2019-08-06T10:34:43.487Z"));
        assertThat(paymentEventsResponse.getEvents().get(1).getPaymentId(), is("abc123"));
        assertThat(paymentEventsResponse.getEvents().get(1).getPaymentLink().getPaymentLink().getHref(), is("http://publicapi.test.localhost/v1/payments/abc123"));
        assertThat(paymentEventsResponse.getEvents().get(1).getState().getStatus(), is("failed"));
        assertThat(paymentEventsResponse.getEvents().get(1).getState().isFinished(), is(true));
        assertThat(paymentEventsResponse.getEvents().get(1).getState().getCode(), is("P0010"));
        assertThat(paymentEventsResponse.getEvents().get(1).getState().getMessage(), is("Payment method rejected"));
        assertThat(paymentEventsResponse.getEvents().get(1).getUpdated(), is("2019-08-06T10:34:48.487Z"));
    }
}
