package uk.gov.pay.api.resources.directdebit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.spotify.docker.client.exceptions.DockerException;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.it.rule.RedisDockerRule;
import uk.gov.pay.api.model.CreateDirectDebitPaymentRequest;
import uk.gov.pay.api.model.CreatePaymentRequestBuilder;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.directdebit.DirectDebitConnectorCreatePaymentResponse;
import uk.gov.pay.api.utils.ApiKeyGenerator;
import uk.gov.pay.api.utils.PublicAuthMockClient;
import uk.gov.pay.api.utils.mocks.ConnectorDDMockClient;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.is;
import static uk.gov.pay.api.model.directdebit.DirectDebitConnectorCreatePaymentResponse.DirectDebitConnectorCreatePaymentResponseBuilder.aDirectDebitConnectorCreatePaymentResponse;
import static uk.gov.pay.commons.testing.port.PortFactory.findFreePort;

public class DirectDebitPaymentsResourceTest {
    private PublicApiConfig configuration;

    //Must use same secret set in test-config.xml's apiKeyHmacSecret
    protected static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");
    protected static final String GATEWAY_ACCOUNT_ID = "GATEWAY_ACCOUNT_ID";
    protected static final String PAYMENTS_PATH = "/v1/directdebit/payments/";

    private static final int CONNECTOR_DD_PORT = findFreePort();
    private static final int PUBLIC_AUTH_PORT = findFreePort();

    @ClassRule
    public static RedisDockerRule redisDockerRule;

    static {
        try {
            redisDockerRule = new RedisDockerRule();
        } catch (DockerException e) {
            e.printStackTrace();
        }
    }

    @ClassRule
    public static WireMockClassRule connectorDDMock = new WireMockClassRule(CONNECTOR_DD_PORT);

    @ClassRule
    public static WireMockClassRule publicAuthMock = new WireMockClassRule(PUBLIC_AUTH_PORT);

    @Rule
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class,
            resourceFilePath("config/test-config.yaml"),
            config("connectorDDUrl", "http://localhost:" + CONNECTOR_DD_PORT),
            config("publicAuthUrl", "http://localhost:" + PUBLIC_AUTH_PORT + "/v1/auth"),
            config("redis.endpoint", redisDockerRule.getRedisUrl())
    );

    private ConnectorDDMockClient connectorDDMockClient = new ConnectorDDMockClient(connectorDDMock);
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);
    
    @Before
    public void setup() {
        configuration = app.getConfiguration();
        connectorDDMock.resetAll();
        publicAuthMock.resetAll();
    }

    String paymentEventsLocationFor(String chargeId) {
        return configuration.getBaseUrl() + "v1/directdebit/payments/" + chargeId + "/events";
    }

    private String paymentLocationFor(String chargeId) {
        return configuration.getBaseUrl() + "v1/directdebit/payments/" + chargeId;
    }

    String mandateLocationFor(String mandateId) {
        return configuration.getBaseUrl() + "v1/directdebit/mandates/" + mandateId;
    }

    protected ValidatableResponse postPaymentResponse(CreateDirectDebitPaymentRequest payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post(PAYMENTS_PATH)
                .then();
    }

    @Test
    public void paymentCreatedSuccessfully() throws JsonProcessingException {
        final Account account = new Account("test", TokenPaymentType.DIRECT_DEBIT);
        final String paymentId = "abc123";
        final String createdDate = "2018-01-01T11:12:13Z";
        final String referenceText = "a reference";
        final String descriptionText = "a description";
        final String paymentProviderText = "a payment provider";
        final String status = "created";
        final boolean finished = false;

        DirectDebitConnectorCreatePaymentResponse connectorResponse = aDirectDebitConnectorCreatePaymentResponse()
                .withPaymentExternalId(paymentId)
                .withAmount(500L)
                .withPaymentProvider(paymentProviderText)
                .withCreatedDate(createdDate)
                .withDescription(descriptionText)
                .withState(new PaymentState(status, finished))
                .withReference(referenceText)
                .build();

        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, account.getAccountId());
        connectorDDMockClient.respondWithPaymentCreated(connectorResponse, account.getAccountId());

        var createDirectDebitPaymentRequest = (CreateDirectDebitPaymentRequest) CreatePaymentRequestBuilder
                .builder()
                .amount(500)
                .description(descriptionText)
                .reference(referenceText)
                .mandateId("test")
                .build();

        postPaymentResponse(createDirectDebitPaymentRequest)
                .statusCode(201)
                .body("payment_id", is(paymentId))
                .body("amount", is(500))
                .body("payment_provider", is(paymentProviderText))
                .body("created_date", is(createdDate))
                .body("description", is(descriptionText))
                .body("reference", is(referenceText))
                .body("state.status", is(status))
                .body("state.finished", is(finished))
                .body("_links.events.href", is(paymentEventsLocationFor(paymentId)))
                .body("_links.events.method", is("GET"))
                .body("_links.self.href", is(paymentLocationFor(paymentId)))
                .body("_links.self.method", is("GET"));
        // TODO - enable mandate link when dd-connector returns mandate id
//                .body("_links.mandate.href", is(mandateLocationFor(mandateId)))
//                .body("_links.mandate.method", is("GET"));
    }
}
