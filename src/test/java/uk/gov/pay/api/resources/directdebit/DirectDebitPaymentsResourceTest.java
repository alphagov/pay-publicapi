package uk.gov.pay.api.resources.directdebit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jayway.jsonassert.JsonAssert;
import com.spotify.docker.client.exceptions.DockerException;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.it.rule.RedisDockerRule;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.directdebit.DirectDebitConnectorCreatePaymentResponse;
import uk.gov.pay.api.utils.ApiKeyGenerator;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.api.utils.PublicAuthMockClient;
import uk.gov.pay.api.utils.mocks.ConnectorDDMockClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;
import static uk.gov.pay.api.model.directdebit.DirectDebitConnectorCreatePaymentResponse.DirectDebitConnectorCreatePaymentResponseBuilder.aDirectDebitConnectorCreatePaymentResponse;
import static uk.gov.pay.commons.testing.port.PortFactory.findFreePort;

public class DirectDebitPaymentsResourceTest {
    private PublicApiConfig configuration;

    //Must use same secret set in test-config.xml's apiKeyHmacSecret
    private static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");
    private static final String GATEWAY_ACCOUNT_ID = "GATEWAY_ACCOUNT_ID";
    private static final String PAYMENTS_PATH = "/v1/directdebit/payments/";
    private static final String REFERENCE = "a reference";
    private static final String DESCRIPTION = "a description";
    private static final String CREATED_DATE = "2018-01-01T11:12:13Z";
    private static final String PAYMENT_ID = "abc123";
    private static final String PAYMENT_PROVIDER = "a payment provider";
    private static final String MANDATE_ID = "mandate-123";

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
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);
    }

    @Test
    public void createPayment_success() throws JsonProcessingException {
        final Account account = new Account(GATEWAY_ACCOUNT_ID, TokenPaymentType.DIRECT_DEBIT);
        final String status = "created";
        final boolean finished = false;

        DirectDebitConnectorCreatePaymentResponse connectorResponse = aDirectDebitConnectorCreatePaymentResponse()
                .withPaymentExternalId(PAYMENT_ID)
                .withAmount(500L)
                .withPaymentProvider(PAYMENT_PROVIDER)
                .withCreatedDate(CREATED_DATE)
                .withDescription(DESCRIPTION)
                .withState(new PaymentState(status, finished))
                .withReference(REFERENCE)
                .build();
        
        connectorDDMockClient.respondWithPaymentCreated(connectorResponse, account.getAccountId());

        String request = new Gson().toJson(Map.of(
                "amount", 500,
                "description", DESCRIPTION,
                "reference", REFERENCE,
                "mandate_id", MANDATE_ID));

        postPaymentResponse(request)
                .statusCode(201)
                .body("payment_id", is(PAYMENT_ID))
                .body("amount", is(500))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("created_date", is(CREATED_DATE))
                .body("description", is(DESCRIPTION))
                .body("reference", is(REFERENCE))
                .body("state.status", is(status))
                .body("state.finished", is(finished))
                .body("_links.events.href", is(paymentEventsLocationFor(PAYMENT_ID)))
                .body("_links.events.method", is("GET"))
                .body("_links.self.href", is(paymentLocationFor(PAYMENT_ID)))
                .body("_links.self.method", is("GET"));
        // TODO - enable mandate link when dd-connector returns mandate id
//                .body("_links.mandate.href", is(mandateLocationFor(mandateId)))
//                .body("_links.mandate.method", is("GET"));
    }

    @Test
    public void createPayment_respondsWith422_whenZeroAmount() {
        
        String payload = new JsonStringBuilder()
                .add("amount", 0)
                .add("reference", REFERENCE)
                .add("description", DESCRIPTION)
                .add("mandate_id", "1234")
                .build();

        postPaymentResponse(payload)
                .statusCode(422)
                .contentType(JSON)
                .body("code", Is.is("P0102"))
                .body("field", Is.is("amount"))
                .body("description", Is.is("Invalid attribute value: amount. Must be greater than or equal to 1"));
    }

    // Ignored as we don't have type validation for endpoint yet
    @Test
    @Ignore
    public void createPayment_responseWith400_whenMandateIdIsNumeric() throws IOException {
        
        String payload = new Gson().toJson(Map.of(
                "amount", "9900",
                "reference", "Some reference",
                "description", "Some description",
                "mandate_id", 1234));

        InputStream body = postPaymentResponse(payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", Is.is("mandate_id"))
                .assertThat("$.code", Is.is("P0102"))
                .assertThat("$.description", Is.is("Invalid attribute value: mandate_id. Must be a valid mandate ID"));
    }
    
    @Test
    public void createPayment_responseWith400_whenMandateIdIsEmptyString() throws IOException {

        String payload = new Gson().toJson(Map.of(
                "amount", "9900",
                "reference", "Some reference",
                "description", "Some description",
                "mandate_id", ""));

        InputStream body = postPaymentResponse(payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", Is.is("mandate_id"))
                .assertThat("$.code", Is.is("P0101"))
                .assertThat("$.description", Is.is("Missing mandatory attribute: mandate_id"));
    }
    
    @Test
    public void createPayment_responseWith400_whenMandateIdIsBlank() throws IOException {

        String payload = new Gson().toJson(Map.of(
                "amount", "9900",
                "reference", "Some reference",
                "description", "Some description",
                "mandate_id", "     "));

        InputStream body = postPaymentResponse(payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", Is.is("mandate_id"))
                .assertThat("$.code", Is.is("P0101"))
                .assertThat("$.description", Is.is("Missing mandatory attribute: mandate_id"));
    }
    
    @Test
    public void createPayment_responseWith400_whenMandateIdIsNull() throws IOException {

        var payloadMap = new HashMap<>();
        payloadMap.put("amount", "9900");
        payloadMap.put("reference", "Some reference");
        payloadMap.put("description", "Some description");
        payloadMap.put("mandate_id", null);
        String payload = new Gson().toJson(payloadMap);

        InputStream body = postPaymentResponse(payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", Is.is("mandate_id"))
                .assertThat("$.code", Is.is("P0101"))
                .assertThat("$.description", Is.is("Missing mandatory attribute: mandate_id"));
    }
    
    @Test
    public void createPayment_responseWith422_whenMandateIdSizeIsGreaterThanMaxLength() throws IOException {

        String aTooLongMandateId = RandomStringUtils.randomAlphanumeric(27);

        String payload = new Gson().toJson(Map.of(
                "amount", "9900",
                "reference", "Some reference",
                "description", "Some description",
                "mandate_id", aTooLongMandateId));

        InputStream body = postPaymentResponse(payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", Is.is("mandate_id"))
                .assertThat("$.code", Is.is("P0102"))
                .assertThat("$.description", Is.is("Invalid attribute value: mandate_id. Must be less than or equal to 26 characters length"));
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

    protected ValidatableResponse postPaymentResponse(String payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post(PAYMENTS_PATH)
                .then();
    }
}
