package uk.gov.pay.api.it.telephone;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spotify.docker.client.exceptions.DockerException;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.it.rule.RedisDockerRule;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.telephone.CreateTelephonePaymentRequest;
import uk.gov.pay.api.utils.ApiKeyGenerator;
import uk.gov.pay.api.utils.PublicAuthMockClient;
import uk.gov.pay.api.utils.mocks.ConnectorDDMockClient;

import java.util.HashMap;
import java.util.Map;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;
import static uk.gov.pay.api.utils.Urls.paymentLocationFor;
import static uk.gov.pay.commons.testing.port.PortFactory.findFreePort;

public abstract class TelephonePaymentResourceITBase {
    //Must use same secret set in test-config.xml's apiKeyHmacSecret
    protected static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");
    protected static final String GATEWAY_ACCOUNT_ID = "GATEWAY_ACCOUNT_ID";
    protected static final String PAYMENTS_PATH = "/v1/payment_notification/";
    protected static final HashMap<String, Object> requestBody = new HashMap<>();
    protected static final CreateTelephonePaymentRequest.Builder createTelephonePaymentRequest = new CreateTelephonePaymentRequest.Builder();

    @ClassRule
    public static RedisDockerRule redisDockerRule;

    static {
        try {
            redisDockerRule = new RedisDockerRule();
        } catch (DockerException e) {
            e.printStackTrace();
        }
    }

    private static final int CONNECTOR_PORT = findFreePort();
    private static final int CONNECTOR_DD_PORT = findFreePort();
    private static final int PUBLIC_AUTH_PORT = findFreePort();
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    @ClassRule
    public static WireMockClassRule connectorMock = new WireMockClassRule(CONNECTOR_PORT);

    @ClassRule
    public static WireMockClassRule connectorDDMock = new WireMockClassRule(CONNECTOR_DD_PORT);

    @ClassRule
    public static WireMockClassRule publicAuthMock = new WireMockClassRule(PUBLIC_AUTH_PORT);

    @Rule
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class,
            resourceFilePath("config/test-config.yaml"),
            config("connectorUrl", "http://localhost:" + CONNECTOR_PORT),
            config("connectorDDUrl", "http://localhost:" + CONNECTOR_DD_PORT),
            config("publicAuthUrl", "http://localhost:" + PUBLIC_AUTH_PORT + "/v1/auth"),
            config("redis.endpoint", redisDockerRule.getRedisUrl())
    );

    PublicApiConfig configuration;

    @Before
    public void setup() {
        configuration = app.getConfiguration();
        connectorMock.resetAll();
        connectorDDMock.resetAll();
        publicAuthMock.resetAll();
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

    protected static String toJson(Map map) {
        return GSON.toJson(map);
    }
}
