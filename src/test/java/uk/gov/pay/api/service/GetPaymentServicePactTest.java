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
import uk.gov.pay.api.ledger.service.LedgerUriGenerator;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.links.PostLink;
import uk.gov.service.payments.commons.model.AuthorisationMode;
import uk.gov.service.payments.commons.model.SupportedLanguage;
import uk.gov.service.payments.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.service.payments.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetPaymentServicePactTest {

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
        LedgerUriGenerator ledgerUriGenerator = new LedgerUriGenerator(mockConfiguration);
        Client client = RestClientFactory.buildClient(new RestClientConfig(false));
        getPaymentService = new GetPaymentService(publicApiUriGenerator,
                new ConnectorService(client, connectorUriGenerator),
                new LedgerService(client, ledgerUriGenerator));
    }
    
    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-get-created-payment"})
    public void testGetPayment() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD, "a-token-link");

        CardPayment payment = getPaymentService.getPayment(account, CHARGE_ID);

        assertThat(payment.getAmount(), is(100L));
        assertThat(payment.getState(), is(new PaymentState("created", false)));
        assertThat(payment.getDescription(), is("Test description"));
        assertThat(payment.getReference(), is("aReference"));
        assertThat(payment.getLanguage(), is(SupportedLanguage.ENGLISH));
        assertThat(payment.getPaymentId(), is(CHARGE_ID));
        assertThat(payment.getReturnUrl().get(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(payment.getPaymentProvider(), is("sandbox"));
        assertThat(payment.getCreatedDate(), is("2018-09-07T13:12:02.121Z"));
        assertThat(payment.getMoto(), is (false));
        assertThat(payment.getDelayedCapture(), is(true));
        assertThat(payment.getCorporateCardSurcharge(), is(Optional.empty()));
        assertThat(payment.getTotalAmount(), is(Optional.empty()));
        assertThat(payment.getAuthorisationMode(), is(AuthorisationMode.WEB));
        assertThat(payment.getLinks().getSelf().getHref(), containsString("v1/payments/" + CHARGE_ID));
        assertThat(payment.getLinks().getSelf().getMethod(), is("GET"));
        assertThat(payment.getLinks().getRefunds().getHref(), containsString("v1/payments/" + CHARGE_ID + "/refunds"));
        assertThat(payment.getLinks().getRefunds().getMethod(), is("GET"));
        assertThat(payment.getLinks().getNextUrl().getHref(), containsString("secure/ae749781-6562-4e0e-8f56-32d9639079dc"));
        assertThat(payment.getLinks().getNextUrl().getMethod(), is("GET"));
        assertThat(payment.getLinks().getNextUrlPost(), is(new PostLink(
                "https://card_frontend/secure",
                "POST",
                "application/x-www-form-urlencoded",
                Collections.singletonMap("chargeTokenId", "ae749781-6562-4e0e-8f56-32d9639079dc")
        )));
        assertThat(payment.getLinks().getCapture(), is(nullValue()));
    }
    
    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-get-capturable-payment"})
    public void testGetCapturablePaymentWithMetadataAndCorporateSurcharge() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD, "a-token-link");
        
        CardPayment payment = getPaymentService.getPayment(account, CHARGE_ID);

        assertThat(payment.getProviderId(), is("gateway-tx-123456"));
        assertThat(payment.getCorporateCardSurcharge().get(), is(250L));
        assertThat(payment.getTotalAmount().get(), is(350L));
        assertThat(payment.getLinks().getCapture().getHref(),
                containsString("v1/payments/" + CHARGE_ID + "/capture"));
        assertThat(payment.getLinks().getCapture().getMethod(), is("POST"));
        assertThat(payment.getMetadata(), is(notNullValue()));
        assertThat(payment.getMetadata().getMetadata().isEmpty(), is(false));
        assertThat(payment.getLinks().getNextUrl(), is(nullValue()));
        assertThat(payment.getLinks().getNextUrlPost(), is(nullValue()));
        assertThat(payment.getAuthorisationSummary().getThreeDSecure().isRequired(), is(true));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-get-payment-with-fee-and-net-amount"})
    public void testGetPaymentWithFeeAndNetAmount() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD, "a-token-link");
        CardPayment payment = getPaymentService.getPayment(account, CHARGE_ID);

        assertThat(payment.getProviderId(), is("gateway-tx-123456"));
        assertThat(payment.getFee().get(), is(5L));
        assertThat(payment.getNetAmount().get(), is(345L));
        assertThat(payment.getLinks().getNextUrl(), is(nullValue()));
        assertThat(payment.getLinks().getNextUrlPost(), is(nullValue()));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-get-rejected-rcp-payment-with-can-retry-true"})
    public void testGetRejectedRecurringPaymentWithCanRetryTrue() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD, "a-token-link");
        CardPayment payment = getPaymentService.getPayment(account, CHARGE_ID);

        assertThat(payment.getState().getCanRetry(), is(true));
        assertThat(payment.getAuthorisationMode(), is(AuthorisationMode.AGREEMENT));
        assertThat(payment.getLinks().getNextUrl(), is(nullValue()));
        assertThat(payment.getLinks().getNextUrlPost(), is(nullValue()));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-get-motoapi-created-payment"})
    public void testGetMotoApiPayment() {
        Account account = new Account(ACCOUNT_ID, TokenPaymentType.CARD, "a-token-link");
        CardPayment payment = getPaymentService.getPayment(account, CHARGE_ID);

        assertThat(payment.getAmount(), is(100L));
        assertThat(payment.getState(), is(new PaymentState("created", false)));
        assertThat(payment.getDescription(), is("a description"));
        assertThat(payment.getReference(), is("a reference"));
        assertThat(payment.getPaymentId(), is(CHARGE_ID));
        assertThat(payment.getMoto(), is (true));
        assertThat(payment.getAuthorisationMode(), is(AuthorisationMode.MOTO_API));
        assertThat(payment.getLinks().getSelf().getHref(), containsString("v1/payments/" + CHARGE_ID));
        assertThat(payment.getLinks().getSelf().getMethod(), is("GET"));
        assertThat(payment.getLinks().getRefunds().getHref(), containsString("v1/payments/" + CHARGE_ID + "/refunds"));
        assertThat(payment.getLinks().getRefunds().getMethod(), is("GET"));
        assertThat(payment.getLinks().getAuthUrlPost(), is(new PostLink("http://publicapi.test.localhost/v1/auth", "POST", "application/json", Collections.singletonMap("one_time_token", "token_1234567asdf"))));
    }
}
