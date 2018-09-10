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
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;
import uk.gov.pay.api.model.links.PostLink;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetPaymentServiceTest {

    private static final String ACCOUNT_ID = "123456";
    private static final String CHARGE_ID = "ch_123abc456def";

    private GetPaymentService getPaymentService;

    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("connector", this);

    @Mock
    private PublicApiConfig mockConfiguration;

    @Before
    public void setup() {
        when(mockConfiguration.getConnectorUrl()).thenReturn(connectorRule.getUrl());

        when(mockConfiguration.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");

        PublicApiUriGenerator publicApiUriGenerator = new PublicApiUriGenerator(mockConfiguration);
        ConnectorUriGenerator connectorUriGenerator = new ConnectorUriGenerator(mockConfiguration);
        Client client = RestClientFactory.buildClient(new RestClientConfig(false));
        getPaymentService = new GetPaymentService(client, publicApiUriGenerator, connectorUriGenerator);
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-get-payment-with-delayed-capture-true"}, publish = false)
    public void testGetPayment() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);

        PaymentWithAllLinks paymentResponse = getPaymentService.getPayment(account, CHARGE_ID);
        CardPayment payment = (CardPayment) paymentResponse.getPayment();

        assertThat(payment.getAmount(), is(100L));
        assertThat(payment.getState(), is(new PaymentState("created", false)));
        assertThat(payment.getDescription(), is("a description"));
        assertThat(payment.getReference(), is("a reference"));
        assertThat(payment.getLanguage(), is(SupportedLanguage.ENGLISH));
        assertThat(payment.getPaymentId(), is(CHARGE_ID));
        assertThat(payment.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(payment.getPaymentProvider(), is("sandbox"));
        assertThat(payment.getCreatedDate(), is("2018-09-07T13:12:02.121Z"));
        assertThat(payment.getDelayedCapture(), is(true));
        assertThat(paymentResponse.getLinks().getSelf().getHref(), containsString("v1/payments/" + CHARGE_ID));
        assertThat(paymentResponse.getLinks().getSelf().getMethod(), is("GET"));
        assertThat(paymentResponse.getLinks().getRefunds().getHref(), containsString("v1/payments/" + CHARGE_ID + "/refunds"));
        assertThat(paymentResponse.getLinks().getRefunds().getMethod(), is("GET"));
        assertThat(paymentResponse.getLinks().getNextUrl().getHref(), containsString("secure/ae749781-6562-4e0e-8f56-32d9639079dc"));
        assertThat(paymentResponse.getLinks().getNextUrl().getMethod(), is("GET"));
        assertThat(paymentResponse.getLinks().getNextUrlPost(), is(new PostLink(
                "https://card_frontend/secure",
                "POST",
                "application/x-www-form-urlencoded",
                Collections.singletonMap("chargeTokenId", "ae749781-6562-4e0e-8f56-32d9639079dc")
        )));
    }

}
