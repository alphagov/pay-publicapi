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
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.CreateCardPaymentRequestBuilder;
import uk.gov.pay.api.model.CreatedPaymentWithAllLinks;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.links.Link;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;
import uk.gov.pay.api.model.links.PostLink;
import uk.gov.service.payments.commons.model.AuthorisationMode;
import uk.gov.service.payments.commons.model.ErrorIdentifier;
import uk.gov.service.payments.commons.model.SupportedLanguage;
import uk.gov.service.payments.commons.model.charge.ExternalMetadata;
import uk.gov.service.payments.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.service.payments.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.client.Client;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CreatePaymentServicePactTest {

    private CreatePaymentService createPaymentService;

    @Rule
    public PactProviderRule connectorRule = new PactProviderRule("connector", this);

    @Mock
    private PublicApiConfig configuration;

    private Account account;

    @Before
    public void setup() {
        when(configuration.getConnectorUrl()).thenReturn(connectorRule.getUrl()); // We will actually send real requests here, which will be intercepted by pact
        when(configuration.getBaseUrl()).thenReturn("http://publicapi.test.localhost/");

        PublicApiUriGenerator publicApiUriGenerator = new PublicApiUriGenerator(configuration);
        ConnectorUriGenerator connectorUriGenerator = new ConnectorUriGenerator(configuration);
        Client client = RestClientFactory.buildClient(new RestClientConfig(false));
        createPaymentService = new CreatePaymentService(client, publicApiUriGenerator, connectorUriGenerator);
        account = new Account("123456", TokenPaymentType.CARD, "a-token-link");
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-with-minimum-fields"})
    public void testCreatePaymentWithMinimumFields() {
        Account account = new Account("123456", TokenPaymentType.CARD, "a-token-link");
        var requestPayload = CreateCardPaymentRequestBuilder.builder()
                .amount(100)
                .returnUrl("https://somewhere.gov.uk/rainbow/1")
                .reference("a reference")
                .description("a description")
                .build();

        CreatedPaymentWithAllLinks createdPaymentWithAllLinks = createPaymentService.create(account, requestPayload, null);
        PaymentWithAllLinks paymentResponse = createdPaymentWithAllLinks.getPayment();
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
        assertThat(payment.getMoto(), is(false));
        assertThat(paymentResponse.getLinks().getSelf(), is(new Link("http://publicapi.test.localhost/v1/payments/ch_ab2341da231434l", "GET")));
        assertThat(paymentResponse.getLinks().getNextUrl(), is(new Link("http://frontend_connector/charge/token_1234567asdf", "GET")));
        PostLink expectedLink = new PostLink("http://frontend_connector/charge/", "POST", "application/x-www-form-urlencoded", Collections.singletonMap("chargeTokenId", "token_1234567asdf"));
        assertThat(paymentResponse.getLinks().getNextUrlPost(), is(expectedLink));
    }
    
    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment"})
    public void testCreatePayment() {
        Map<String, Object> metadata = Map.of(
                "ledger_code", 123,
                "fund_code", "ISIN122038",
                "cancellable", false);
        var requestPayload = CreateCardPaymentRequestBuilder.builder()
                .amount(100)
                .returnUrl("https://somewhere.gov.uk/rainbow/1")
                .reference("a reference")
                .description("a description")
                .metadata(new ExternalMetadata(metadata))
                .delayedCapture(Boolean.TRUE)
                .moto(Boolean.TRUE)
                .language(SupportedLanguage.WELSH)
                .email("joe.bogs@example.org")
                .cardholderName("J. Bogs")
                .addressLine1("address line 1")
                .addressLine2("address line 2")
                .city("address city")
                .postcode("AB1 CD2")
                .country("GB")
                .build();

        CreatedPaymentWithAllLinks paymentWithAllLinks = createPaymentService.create(account, requestPayload, null);
        PaymentWithAllLinks paymentResponse = paymentWithAllLinks.getPayment();
        CardPayment payment = (CardPayment) paymentResponse.getPayment();

        assertThat(payment.getPaymentId(), is("ch_ab2341da231434l"));
        assertThat(payment.getAmount(), is(100L));
        assertThat(payment.getReference(), is("a reference"));
        assertThat(payment.getDescription(), is("a description"));
        assertThat(payment.getState(), is(new PaymentState("created", false)));
        assertThat(payment.getReturnUrl().get(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(payment.getPaymentProvider(), is("Sandbox"));
        assertThat(payment.getCreatedDate(), is("2016-01-01T12:00:00Z"));
        assertThat(paymentResponse.getLinks().getSelf(), is(new Link("http://publicapi.test.localhost/v1/payments/ch_ab2341da231434l", "GET")));
        assertThat(paymentResponse.getLinks().getNextUrl(), is(new Link("http://frontend_connector/charge/token_1234567asdf", "GET")));
        PostLink expectedLink = new PostLink("http://frontend_connector/charge/", "POST", "application/x-www-form-urlencoded", Collections.singletonMap("chargeTokenId", "token_1234567asdf"));
        assertThat(paymentResponse.getLinks().getNextUrlPost(), is(expectedLink));
        assertThat(payment.getMetadata().getMetadata(), is(metadata));
        assertThat(payment.getDelayedCapture(), is(true));
        assertThat(payment.getLanguage(), is(SupportedLanguage.WELSH));
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

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-for-disabled-account"})
    public void creating_payment_for_disabled_account_should_return_403() {
        var requestPayload = CreateCardPaymentRequestBuilder.builder()
                .amount(100)
                .returnUrl("https://somewhere.gov.uk/rainbow/1")
                .reference("a reference")
                .description("a description")
                .build();

        try {
            createPaymentService.create(account, requestPayload, null);
            fail("Expected CreateChargeException to be thrown");
        } catch (CreateChargeException e) {
            assertThat(e.getErrorIdentifier(), is(ErrorIdentifier.ACCOUNT_DISABLED));
        }
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-with-authorisation-mode-moto-api"})
    public void testCreatePaymentWithAuthorisationModeMotoApi() {
        Account account = new Account("123456", TokenPaymentType.CARD, "a-token-link");
        var requestPayload = CreateCardPaymentRequestBuilder.builder()
                .amount(100)
                .reference("a reference")
                .description("a description")
                .authorisationMode(AuthorisationMode.MOTO_API)
                .build();

        CreatedPaymentWithAllLinks paymentWithAllLinks = createPaymentService.create(account, requestPayload, null);
        PaymentWithAllLinks paymentResponse = paymentWithAllLinks.getPayment();
        CardPayment payment = (CardPayment) paymentResponse.getPayment();
        assertThat(paymentResponse.getLinks().getAuthUrlPost(), is(new PostLink("http://publicapi.test.localhost/v1/auth", "POST", "application/json", Collections.singletonMap("one_time_token", "token_1234567asdf"))));
        assertThat(payment.getPaymentId(), is("ch_123abc456def"));
        assertThat(payment.getAuthorisationMode(), is(AuthorisationMode.MOTO_API));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-with-authorisation-mode-moto-api-not-allowed"})
    public void testCreatePaymentWithAuthorisationModeMotoApi_whenNotAllowedForAccount_shouldReturn422() {
        Account account = new Account("667", TokenPaymentType.CARD, "a-token-link");
        var requestPayload = CreateCardPaymentRequestBuilder.builder()
                .amount(100)
                .reference("a reference")
                .description("a description")
                .authorisationMode(AuthorisationMode.MOTO_API)
                .build();
        try {
            createPaymentService.create(account, requestPayload, null);
            fail("Expected CreateChargeException to be thrown");
        } catch (CreateChargeException e) {
            assertThat(e.getErrorIdentifier(), is(ErrorIdentifier.AUTHORISATION_API_NOT_ALLOWED));
        }
    }
    
    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-with-zero-amount-not-allowed"})
    public void creating_payment_with_zero_amount_when_not_allowed_for_account_should_return_422() {
        var requestPayload = CreateCardPaymentRequestBuilder.builder()
                .amount(0)
                .returnUrl("https://somewhere.gov.uk/rainbow/1")
                .reference("a reference")
                .description("a description")
                .build();

        try {
            createPaymentService.create(account, requestPayload, null);
            fail("Expected CreateChargeException to be thrown");
        } catch (CreateChargeException e) {
            assertThat(e.getErrorIdentifier(), is(ErrorIdentifier.ZERO_AMOUNT_NOT_ALLOWED));
        }
    }
    
    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-with-moto-not-allowed"})
    public void creating_payment_with_moto_when_not_allowed_for_account_should_return_422() {
        var requestPayload = CreateCardPaymentRequestBuilder.builder()
                .amount(100)
                .returnUrl("https://somewhere.gov.uk/rainbow/1")
                .reference("a reference")
                .description("a description")
                .moto(true)
                .build();

        try {
            createPaymentService.create(account, requestPayload, null);
            fail("Expected CreateChargeException to be thrown");
        } catch (CreateChargeException e) {
            assertThat(e.getErrorIdentifier(), is(ErrorIdentifier.MOTO_NOT_ALLOWED));
        }
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-with-credentials-in-created-not-allowed"})
    public void shouldThrowExceptionWithIdentifierAccountNotLinkedToPSP_IfGatewayAccountCredentialIsNotConfiguredInConnector() {
        Account account = new Account("444", TokenPaymentType.CARD, "a-token-link");
        var requestPayload = CreateCardPaymentRequestBuilder.builder()
                .amount(100)
                .returnUrl("https://somewhere.gov.uk/rainbow/1")
                .reference("a reference")
                .description("a description")
                .build();

        try {
            createPaymentService.create(account, requestPayload, null);
            fail("Expected CreateChargeException to be thrown");
        } catch (CreateChargeException e) {
            assertThat(e.getErrorIdentifier(), is(ErrorIdentifier.ACCOUNT_NOT_LINKED_WITH_PSP));
        }
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-with-invalid-return-url"})
    public void shouldThrowExceptionWithIdentifierInvalidAttributeValue_whenReturnUrlInvalid() {
        Account account = new Account("444", TokenPaymentType.CARD, "a-token-link");
        var requestPayload = CreateCardPaymentRequestBuilder.builder()
                .amount(100)
                .returnUrl("invalid")
                .reference("a reference")
                .description("a description")
                .build();

        try {
            createPaymentService.create(account, requestPayload, null);
            fail("Expected CreateChargeException to be thrown");
        } catch (CreateChargeException e) {
            assertThat(e.getErrorIdentifier(), is(ErrorIdentifier.INVALID_ATTRIBUTE_VALUE));
            assertThat(e.getConnectorErrorMessage(), is("Invalid attribute value: return_url. Must be a valid URL format"));
        }
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-with-missing-return-url"})
    public void shouldThrowExceptionWithIdentifierMissingMandatoryAttribute_whenReturnUrlMissing() {
        Account account = new Account("444", TokenPaymentType.CARD, "a-token-link");
        var requestPayload = CreateCardPaymentRequestBuilder.builder()
                .amount(100)
                .reference("a reference")
                .description("a description")
                .build();

        try {
            createPaymentService.create(account, requestPayload, null);
            fail("Expected CreateChargeException to be thrown");
        } catch (CreateChargeException e) {
            assertThat(e.getErrorIdentifier(), is(ErrorIdentifier.MISSING_MANDATORY_ATTRIBUTE));
            assertThat(e.getConnectorErrorMessage(), is("Missing mandatory attribute: return_url"));
        }
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-with-idempotency-key-200-response"})
    public void testCreatePaymentWithMatchingBodyAndSameIdempotencyKeyAsExistingPayment() {
        var idempotencyKey = "Ida the idempotency key";
        var requestPayload = CreateCardPaymentRequestBuilder.builder()
                .amount(2046)
                .reference("referential")
                .description("describable")
                .agreementId("abcdefghijklmnopqrstuvwxyz")
                .authorisationMode(AuthorisationMode.AGREEMENT)
                .build();

        CreatedPaymentWithAllLinks paymentResponse = createPaymentService.create(account, requestPayload, idempotencyKey);
        PaymentWithAllLinks paymentWithAllLinks = paymentResponse.getPayment();
        CardPayment payment = (CardPayment) paymentWithAllLinks.getPayment();

        assertThat(payment.getPaymentId(), is("chargeable"));
        assertThat(payment.getAmount(), is(2046L));
        assertThat(payment.getReference(), is("referential"));
        assertThat(payment.getDescription(), is("describable"));
        assertThat(payment.getAgreementId(), is("abcdefghijklmnopqrstuvwxyz"));
        assertThat(payment.getAuthorisationMode(), is(AuthorisationMode.AGREEMENT));
        assertThat(payment.getState(), is(new PaymentState("created", false)));
        assertThat(payment.getPaymentProvider(), is("sandbox"));
        assertThat(payment.getCreatedDate(), is("2023-04-20T13:30:00.000Z"));
        assertThat(paymentWithAllLinks.getLinks().getSelf(), is(new Link("http://publicapi.test.localhost/v1/payments/chargeable", "GET")));
        assertThat(paymentWithAllLinks.getLinks().getRefunds().getHref(), containsString("v1/payments/chargeable/refunds"));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = {"publicapi-connector-create-payment-with-idempotency-key-409-response"})
    public void testCreatePaymentWithDifferentBodyAndSameIdempotencyKeyAsExistingPayment() {
        var idempotencyKey = "Ida the idempotency key";
        var requestPayload = CreateCardPaymentRequestBuilder.builder()
                .amount(2046)
                .reference("different referential")
                .description("describable")
                .agreementId("abcdefghijklmnopqrstuvwxyz")
                .authorisationMode(AuthorisationMode.AGREEMENT)
                .build();

        CreateChargeException cr = assertThrows(CreateChargeException.class,
                () -> createPaymentService.create(account, requestPayload, idempotencyKey));

        assertThat(cr.getErrorStatus(), is(409));
        assertThat(cr.getErrorIdentifier(), is(ErrorIdentifier.IDEMPOTENCY_KEY_USED));
        assertThat(cr.getConnectorErrorMessage(), is("The Idempotency-Key has already been used to create a payment"));
    }

    @Test
    @PactVerification({"connector"})
    @Pacts(pacts = "publicapi-connector-take-a-recurring-payment")
    public void testTakeARecurringPayment() {
        var requestPayload = CreateCardPaymentRequestBuilder.builder()
                .amount(2046)
                .reference("a-reference")
                .description("a description")
                .agreementId("abcdefghijklmnopqrstuvwxyz")
                .authorisationMode(AuthorisationMode.AGREEMENT)
                .build();

        CreatedPaymentWithAllLinks paymentResponse = createPaymentService.create(account, requestPayload, null);
        PaymentWithAllLinks paymentWithAllLinks = paymentResponse.getPayment();
        CardPayment payment = (CardPayment) paymentWithAllLinks.getPayment();

        assertThat(payment.getPaymentId(), is("valid-charge-id"));
        assertThat(payment.getAmount(), is(2046L));
        assertThat(payment.getReference(), is("a-reference"));
        assertThat(payment.getDescription(), is("a description"));
        assertThat(payment.getAgreementId(), is("abcdefghijklmnopqrstuvwxyz"));
        assertThat(payment.getAuthorisationMode(), is(AuthorisationMode.AGREEMENT));
        assertThat(payment.getState(), is(new PaymentState("created", false)));
        assertThat(payment.getPaymentProvider(), is("sandbox"));
        assertThat(payment.getCreatedDate(), is(notNullValue()));
        assertThat(paymentWithAllLinks.getLinks().getSelf(), is(new Link("http://publicapi.test.localhost/v1/payments/valid-charge-id", "GET")));
        assertThat(paymentWithAllLinks.getLinks().getRefunds().getHref(), containsString("v1/payments/valid-charge-id/refunds"));
    }
}
