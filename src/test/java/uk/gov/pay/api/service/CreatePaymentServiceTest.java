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
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.ValidCreatePaymentRequest;
import uk.gov.pay.api.model.links.Link;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;
import uk.gov.pay.api.model.links.PostLink;
import uk.gov.pay.commons.model.SupportedLanguage;
import uk.gov.pay.commons.model.charge.ExternalMetadata;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreatePaymentServiceTest {

    private CreatePaymentService createPaymentService;

    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("connector", this);

    @Mock
    private PublicApiConfig configuration;

    @Before
    public void setup() {
        when(configuration.getConnectorUrl()).thenReturn(connectorRule.getUrl()); // We will actually send real requests here, which will be intercepted by pact        

        when(configuration.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");

        PublicApiUriGenerator publicApiUriGenerator = new PublicApiUriGenerator(configuration);
        ConnectorUriGenerator connectorUriGenerator = new ConnectorUriGenerator(configuration);
        Client client = RestClientFactory.buildClient(new RestClientConfig(false));

        createPaymentService = new CreatePaymentService(client,
                publicApiUriGenerator, connectorUriGenerator);
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-with-metadata"})
    public void testCreatePaymentWithMetadata() {
        Account account = new Account("123456", TokenPaymentType.CARD);
        Map<String, Object> metadata = Map.of(
                "ledger_code", 123, 
                "fund_code", "ISIN122038", 
                "cancellable", false);
        ValidCreatePaymentRequest requestPayload = new ValidCreatePaymentRequest(CreatePaymentRequest.builder()
                .amount(100)
                .returnUrl("https://somewhere.gov.uk/rainbow/1")
                .reference("a reference")
                .description("a description")
                .metadata(new ExternalMetadata(metadata))
                .build());

        PaymentWithAllLinks paymentResponse = createPaymentService.create(account, requestPayload);
        CardPayment payment = (CardPayment) paymentResponse.getPayment();
        assertThat(payment.getMetadata().getMetadata(), is(metadata));
    }
    
    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment"})
    public void testCreatePayment() {
        Account account = new Account("123456", TokenPaymentType.CARD);
        ValidCreatePaymentRequest requestPayload = new ValidCreatePaymentRequest(CreatePaymentRequest.builder()
                .amount(100)
                .returnUrl("https://somewhere.gov.uk/rainbow/1")
                .reference("a reference")
                .description("a description")
                .build());

        PaymentWithAllLinks paymentResponse = createPaymentService.create(account, requestPayload);
        CardPayment payment = (CardPayment) paymentResponse.getPayment();

        assertThat(payment.getPaymentId(), is("ch_ab2341da231434l"));
        assertThat(payment.getAmount(), is(100L));
        assertThat(payment.getReference(), is("a reference"));
        assertThat(payment.getDescription(), is("a description"));
        assertThat(payment.getEmail(), is(Optional.empty()));
        assertThat(payment.getState(), is(new PaymentState("created", false)));
        assertThat(payment.getReturnUrl().get(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(payment.getPaymentProvider(), is("Sandbox"));
        assertThat(payment.getCreatedDate(), is("2016-01-01T12:00:00Z"));
        assertThat(payment.getLanguage(), is(SupportedLanguage.ENGLISH));
        assertThat(payment.getDelayedCapture(), is(false));
        assertThat(paymentResponse.getLinks().getSelf(), is(new Link("http://publicapi.test.localhost/v1/payments/ch_ab2341da231434l", "GET")));
        assertThat(paymentResponse.getLinks().getNextUrl(), is(new Link("http://frontend_connector/charge/token_1234567asdf", "GET")));
        PostLink expectedLink = new PostLink("http://frontend_connector/charge/", "POST", "application/x-www-form-urlencoded", Collections.singletonMap("chargeTokenId", "token_1234567asdf"));
        assertThat(paymentResponse.getLinks().getNextUrlPost(), is(expectedLink));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-with-delayed-capture-true"})
    public void testCreatePaymentWithDelayedCaptureEqualsTrue() {
        Account account = new Account("123456", TokenPaymentType.CARD);
        ValidCreatePaymentRequest requestPayload = new ValidCreatePaymentRequest(CreatePaymentRequest.builder()
                .amount(100)
                .returnUrl("https://somewhere.gov.uk/rainbow/1")
                .reference("a reference")
                .description("a description")
                .delayedCapture(Boolean.TRUE)
                .build());

        PaymentWithAllLinks paymentResponse = createPaymentService.create(account, requestPayload);
        CardPayment payment = (CardPayment) paymentResponse.getPayment();

        assertThat(payment.getPaymentId(), is("ch_ab2341da231434l"));
        assertThat(payment.getAmount(), is(100L));
        assertThat(payment.getReference(), is("a reference"));
        assertThat(payment.getDescription(), is("a description"));
        assertThat(payment.getEmail(), is(Optional.empty()));
        assertThat(payment.getState(), is(new PaymentState("created", false)));
        assertThat(payment.getReturnUrl().get(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(payment.getPaymentProvider(), is("Sandbox"));
        assertThat(payment.getCreatedDate(), is("2016-01-01T12:00:00Z"));
        assertThat(payment.getLanguage(), is(SupportedLanguage.ENGLISH));
        assertThat(payment.getDelayedCapture(), is(true));
        assertThat(paymentResponse.getLinks().getSelf(), is(new Link("http://publicapi.test.localhost/v1/payments/ch_ab2341da231434l", "GET")));
        assertThat(paymentResponse.getLinks().getNextUrl(), is(new Link("http://frontend_connector/charge/token_1234567asdf", "GET")));
        PostLink expectedLink = new PostLink("http://frontend_connector/charge/", "POST", "application/x-www-form-urlencoded", Collections.singletonMap("chargeTokenId", "token_1234567asdf"));
        assertThat(paymentResponse.getLinks().getNextUrlPost(), is(expectedLink));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-with-language-welsh"})
    public void testCreatePaymentWithWelshLanguage() {
        Account account = new Account("123456", TokenPaymentType.CARD);
        ValidCreatePaymentRequest requestPayload = new ValidCreatePaymentRequest(CreatePaymentRequest.builder()
                .amount(100)
                .returnUrl("https://somewhere.gov.uk/rainbow/1")
                .reference("a reference")
                .description("a description")
                .language("cy")
                .build());

        PaymentWithAllLinks paymentResponse = createPaymentService.create(account, requestPayload);
        CardPayment payment = (CardPayment) paymentResponse.getPayment();

        assertThat(payment.getPaymentId(), is("ch_ab2341da231434l"));
        assertThat(payment.getAmount(), is(100L));
        assertThat(payment.getReference(), is("a reference"));
        assertThat(payment.getDescription(), is("a description"));
        assertThat(payment.getEmail(), is(Optional.empty()));
        assertThat(payment.getState(), is(new PaymentState("created", false)));
        assertThat(payment.getReturnUrl().get(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(payment.getPaymentProvider(), is("Sandbox"));
        assertThat(payment.getCreatedDate(), is("2016-01-01T12:00:00Z"));
        assertThat(payment.getLanguage(), is(SupportedLanguage.WELSH));
        assertThat(paymentResponse.getLinks().getSelf(), is(new Link("http://publicapi.test.localhost/v1/payments/ch_ab2341da231434l", "GET")));
        assertThat(paymentResponse.getLinks().getNextUrl(), is(new Link("http://frontend_connector/charge/token_1234567asdf", "GET")));
        PostLink expectedLink = new PostLink("http://frontend_connector/charge/", "POST", "application/x-www-form-urlencoded", Collections.singletonMap("chargeTokenId", "token_1234567asdf"));
        assertThat(paymentResponse.getLinks().getNextUrlPost(), is(expectedLink));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-with-prefilled-cardholder-details"})
    public void testCreatePaymentWithPrefilledCardholderDetails() {
        Account account = new Account("123456", TokenPaymentType.CARD);
        CreatePaymentRequest createPaymentRequest = CreatePaymentRequest.builder()
                .amount(100)
                .returnUrl("https://somewhere.gov.uk/rainbow/1")
                .reference("a reference")
                .description("a description")
                .email("joe.bogs@example.org")
                .cardholderName("J. Bogs")
                .addressLine1("address line 1")
                .addressLine2("address line 2")
                .city("address city")
                .postcode("AB1 CD2")
                .country("GB")
                .build();
        ValidCreatePaymentRequest requestPayload = new ValidCreatePaymentRequest(createPaymentRequest);
        PaymentWithAllLinks paymentResponse = createPaymentService.create(account, requestPayload);
        CardPayment payment = (CardPayment) paymentResponse.getPayment();

        assertThat(payment.getPaymentId(), is("ch_ab2341da231434l"));
        assertThat(payment.getAmount(), is(100L));
        assertThat(payment.getReference(), is("a reference"));
        assertThat(payment.getDescription(), is("a description"));
        assertThat(payment.getEmail().isPresent(), is(true));
        assertThat(payment.getEmail().get(), is("joe.bogs@example.org"));
        assertThat(payment.getCardDetails().isPresent(), is(true));
        assertThat(payment.getCardDetails().get().getCardHolderName(), is("J. Bogs"));
        assertThat(payment.getCardDetails().get().getBillingAddress().isPresent(), is(true));
        Address billingAddress = payment.getCardDetails().get().getBillingAddress().get();
        assertThat(billingAddress.getLine1(), is("address line 1"));
        assertThat(billingAddress.getLine2(), is("address line 2"));
        assertThat(billingAddress.getCity(), is("address city"));
        assertThat(billingAddress.getPostcode(), is("AB1 CD2"));
        assertThat(billingAddress.getCountry(), is("GB"));
    }
}
