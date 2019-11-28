package uk.gov.pay.api.service;

import au.com.dius.pact.consumer.PactVerification;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.app.RestClientFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.app.config.RestClientConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.clients.ExternalServiceClient;
import uk.gov.pay.api.ledger.service.LedgerUriGenerator;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;
import uk.gov.pay.api.utils.mocks.ConnectorMockClient;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.pay.commons.testing.port.PortFactory.findFreePort;

@RunWith(MockitoJUnitRunner.class)
public class GetPaymentServiceLedgerTest {

    private static final String ACCOUNT_ID = "123456";
    private static final String CHARGE_ID_NON_EXISTENT_IN_CONNECTOR = "ch_123abc456xyz";
    private static final int CONNECTOR_PORT = findFreePort();

    private GetPaymentService getPaymentService;

    @ClassRule
    public static WireMockClassRule connectorRule = new WireMockClassRule(CONNECTOR_PORT);

    @Rule
    public PactProviderRule ledgerRule = new PactProviderRule("ledger", this);

    @Mock
    private PublicApiConfig mockConfiguration;

    @Before
    public void setup() {
        connectorRule.resetAll();
        when(mockConfiguration.getConnectorUrl()).thenReturn("http://localhost:" + CONNECTOR_PORT);
        when(mockConfiguration.getLedgerUrl()).thenReturn(ledgerRule.getUrl());
        when(mockConfiguration.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");

        ConnectorMockClient connectorMockClient = new ConnectorMockClient(connectorRule);
        connectorMockClient.respondChargeNotFound(ACCOUNT_ID, CHARGE_ID_NON_EXISTENT_IN_CONNECTOR, "not found");

        PublicApiUriGenerator publicApiUriGenerator = new PublicApiUriGenerator(mockConfiguration);
        ConnectorUriGenerator connectorUriGenerator = new ConnectorUriGenerator(mockConfiguration);
        LedgerUriGenerator ledgerUriGenerator = new LedgerUriGenerator(mockConfiguration);
        Client client = RestClientFactory.buildClient(new RestClientConfig(false));
        getPaymentService = new GetPaymentService(publicApiUriGenerator,
                new ConnectorService(new ExternalServiceClient(client), connectorUriGenerator),
                new LedgerService(client, ledgerUriGenerator));
    }

    @Test
    @PactVerification({"ledger"})
    @Pacts(pacts = {"publicapi-ledger-get-payment-with-metadata"})
    public void testGetPaymentWithMetadataFromLedger() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);
        PaymentWithAllLinks paymentResponse = getPaymentService.getPayment(account, CHARGE_ID_NON_EXISTENT_IN_CONNECTOR);
        CardPayment payment = (CardPayment) paymentResponse.getPayment();
        assertThat(payment.getMetadata(), is(notNullValue()));
        assertThat(payment.getMetadata().getMetadata().isEmpty(), is(false));
    }

    @Test
    @PactVerification({"ledger"})
    @Pacts(pacts = {"publicapi-ledger-get-payment-with-gateway-transaction-id"})
    public void providerIdIsAvailableWhenPaymentIsSubmitted_Ledger() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);
        PaymentWithAllLinks paymentResponse = getPaymentService.getPayment(account, CHARGE_ID_NON_EXISTENT_IN_CONNECTOR);
        CardPayment payment = (CardPayment) paymentResponse.getPayment();
        assertThat(payment.getProviderId(), is("gateway-tx-123456"));
    }

    @Test
    @PactVerification({"ledger"})
    @Pacts(pacts = {"publicapi-ledger-get-payment-with-delayed-capture-true"})
    public void testGetPaymentFromLedger() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);

        PaymentWithAllLinks paymentResponse = getPaymentService.getPayment(account, CHARGE_ID_NON_EXISTENT_IN_CONNECTOR);
        CardPayment payment = (CardPayment) paymentResponse.getPayment();

        assertThat(payment.getAmount(), is(100L));
        assertThat(payment.getState(), is(new PaymentState("created", false)));
        assertThat(payment.getDescription(), is("Test description"));
        assertThat(payment.getReference(), is("aReference"));
        assertThat(payment.getLanguage(), is(SupportedLanguage.ENGLISH));
        assertThat(payment.getPaymentId(), is(CHARGE_ID_NON_EXISTENT_IN_CONNECTOR));
        assertThat(payment.getReturnUrl().get(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(payment.getPaymentProvider(), is("sandbox"));
        assertThat(payment.getCreatedDate(), is("2018-09-07T13:12:02.121Z"));
        assertThat(payment.getDelayedCapture(), is(true));
        assertThat(payment.getCorporateCardSurcharge(), is(Optional.empty()));
        assertThat(payment.getTotalAmount(), is(Optional.empty()));
        assertThat(paymentResponse.getLinks().getSelf().getHref(), containsString("v1/payments/" + CHARGE_ID_NON_EXISTENT_IN_CONNECTOR));
        assertThat(paymentResponse.getLinks().getSelf().getMethod(), is("GET"));
        assertThat(paymentResponse.getLinks().getRefunds().getHref(), containsString("v1/payments/" + CHARGE_ID_NON_EXISTENT_IN_CONNECTOR + "/refunds"));
        assertThat(paymentResponse.getLinks().getRefunds().getMethod(), is("GET"));
        assertThat(paymentResponse.getLinks().getNextUrl(), is(nullValue()));
        assertThat(paymentResponse.getLinks().getNextUrlPost(), is(nullValue()));
        assertThat(paymentResponse.getLinks().getCapture(), is(nullValue()));
    }

    @Test
    @PactVerification({"ledger"})
    @Pacts(pacts = {"publicapi-ledger-get-payment-with-corporate-surcharge"})
    public void testGetPaymentWithCorporateCardSurchargeFromLedger() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);

        PaymentWithAllLinks paymentResponse = getPaymentService.getPayment(account, CHARGE_ID_NON_EXISTENT_IN_CONNECTOR);
        CardPayment payment = (CardPayment) paymentResponse.getPayment();
        assertThat(payment.getCorporateCardSurcharge().get(), is(250L));
        assertThat(payment.getTotalAmount().get(), is(2250L));
        assertThat(paymentResponse.getLinks().getCapture(), is(nullValue()));
    }

    @Test
    @PactVerification({"ledger"})
    @Pacts(pacts = {"publicapi-ledger-get-payment-with-fee-and-net-amount"})
    public void testGetPaymentWithFeeAndNetAmountFromLedger() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD);

        PaymentWithAllLinks paymentResponse = getPaymentService.getPayment(account, CHARGE_ID_NON_EXISTENT_IN_CONNECTOR);
        CardPayment payment = (CardPayment) paymentResponse.getPayment();

        assertThat(payment.getPaymentId(), is(CHARGE_ID_NON_EXISTENT_IN_CONNECTOR));
        assertThat(payment.getPaymentProvider(), is("sandbox"));
        assertThat(payment.getAmount(), is(100L));
        assertThat(payment.getFee().get(), is(5L));
        assertThat(payment.getNetAmount().get(), is(95L));
    }
}
