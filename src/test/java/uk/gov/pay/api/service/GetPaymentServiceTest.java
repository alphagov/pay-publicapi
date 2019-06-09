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
import uk.gov.pay.api.model.response.CardPayment;
import uk.gov.pay.api.model.response.PaymentState;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.response.PaymentWithAllLinks;
import uk.gov.pay.api.model.response.PostLink;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
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
    @Pacts(pacts = {"publicapi-connector-get-payment-with-metadata"})
    public void testGetPaymentWithMetadata() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);
        PaymentWithAllLinks paymentResponse = getPaymentService.getPayment(account, "ch_999abc456def");
        CardPayment payment = (CardPayment) paymentResponse.getPayment();
        assertThat(payment.getMetadata(), is(notNullValue()));
        assertThat(payment.getMetadata().getMetadata().isEmpty(), is(false));
    }
    
    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-get-payment-with-gateway-transaction-id"})
    public void providerIdIsAvailableWhenPaymentIsSubmitted() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);
        PaymentWithAllLinks paymentResponse = getPaymentService.getPayment(account, "ch_999abc456def");
        CardPayment payment = (CardPayment) paymentResponse.getPayment();
        assertThat(payment.getProviderId(), is("gateway-tx-123456"));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-get-payment-with-delayed-capture-true"})
    public void testGetPayment() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);

        PaymentWithAllLinks paymentResponse = getPaymentService.getPayment(account, CHARGE_ID);
        CardPayment payment = (CardPayment) paymentResponse.getPayment();

        assertThat(payment.getAmount(), is(100L));
        assertThat(payment.getState(), is(new PaymentState("created", false)));
        assertThat(payment.getDescription(), is("Test description"));
        assertThat(payment.getReference(), is("aReference"));
        assertThat(payment.getLanguage(), is(SupportedLanguage.ENGLISH));
        assertThat(payment.getPaymentId(), is(CHARGE_ID));
        assertThat(payment.getReturnUrl().get(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(payment.getPaymentProvider(), is("sandbox"));
        assertThat(payment.getCreatedDate(), is("2018-09-07T13:12:02.121Z"));
        assertThat(payment.getDelayedCapture(), is(true));
        assertThat(payment.getCorporateCardSurcharge(), is(Optional.empty()));
        assertThat(payment.getTotalAmount(), is(Optional.empty()));
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
        assertThat(paymentResponse.getLinks().getCapture(), is(nullValue()));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-get-payment-with-corporate-surcharge"}) 
    public void testGetPaymentWithCorporateCardSurcharge() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);

        PaymentWithAllLinks paymentResponse = getPaymentService.getPayment(account, CHARGE_ID);
        CardPayment payment = (CardPayment) paymentResponse.getPayment();
        assertThat(payment.getCorporateCardSurcharge().get(), is(250L));
        assertThat(payment.getTotalAmount().get(), is(2250L));
        assertThat(paymentResponse.getLinks().getCapture(), is(nullValue()));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-get-payment-with-awaiting-capture-request-state"})
    public void testGetPaymentWithChargeInAwaitingCaptureRequest() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);
        PaymentWithAllLinks paymentResponse = getPaymentService.getPayment(account, CHARGE_ID);
        assertThat(paymentResponse.getLinks().getCapture().getHref(), 
                containsString("v1/payments/" + CHARGE_ID + "/capture"));
        assertThat(paymentResponse.getLinks().getCapture().getMethod(), is("POST"));
        assertThat(paymentResponse.getLinks().getNextUrl(), is(nullValue()));
        assertThat(paymentResponse.getLinks().getNextUrlPost(), is(nullValue()));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-get-payment-with-fee-and-net-amount"})
    public void testGetPaymentWithFeeAndNetAmount() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);

        PaymentWithAllLinks paymentResponse = getPaymentService.getPayment(account, CHARGE_ID);
        CardPayment payment = (CardPayment) paymentResponse.getPayment();

        assertThat(payment.getPaymentId(), is(CHARGE_ID));
        assertThat(payment.getPaymentProvider(), is("sandbox"));
        assertThat(payment.getAmount(), is(100L));
        assertThat(payment.getFee().get(), is(5L));
        assertThat(payment.getNetAmount().get(), is(95L));
    }
}
