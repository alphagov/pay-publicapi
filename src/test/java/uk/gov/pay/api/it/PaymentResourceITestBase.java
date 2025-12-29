package uk.gov.pay.api.it;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.dropwizard.testing.DropwizardTestSupport;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.it.rule.RedisDockerRule;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.utils.ApiKeyGenerator;

import java.util.Map;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static uk.gov.pay.api.utils.Urls.paymentLocationFor;

public abstract class PaymentResourceITestBase {
    //Must use same secret set in test-config.xml's apiKeyHmacSecret
    protected static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");
    protected static final String GATEWAY_ACCOUNT_ID = "GATEWAY_ACCOUNT_ID";
    protected static final String PAYMENTS_PATH = "/v1/payments/";
    static final String AGREEMENTS_PATH = "/v1/agreements/";
    static final String LEDGER_ONLY_STRATEGY = "ledger-only";

    @RegisterExtension
    private static final RedisDockerRule redisDockerRule = new RedisDockerRule();

    private static final Gson GSON = new GsonBuilder().create();

    @RegisterExtension
    protected static final WireMockExtension connectorServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @RegisterExtension
    protected static final WireMockExtension publicAuthServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @RegisterExtension
    static final WireMockExtension ledgerServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    protected static DropwizardTestSupport<PublicApiConfig> app;

    PublicApiConfig configuration;

    @BeforeAll
    static void startApp() throws Exception {
        app = new DropwizardTestSupport<>(
                PublicApi.class,
                resourceFilePath("config/test-config.yaml"),
                config("connectorUrl", connectorServer.baseUrl()),
                config("publicAuthUrl", publicAuthServer.baseUrl() + "/v1/auth"),
                config("ledgerUrl", ledgerServer.baseUrl()),
                config("redis.endpoint", redisDockerRule.getRedisUrl())
        );
        app.before();
    }

    @AfterAll
    static void afterAll() {
        app.after();
    }

    @BeforeEach
    void setup() {
        configuration = app.getConfiguration();
    }

     String frontendUrlFor(TokenPaymentType paymentType) {
        return "http://frontend_" + paymentType.toString().toLowerCase() + "/charge/";
    }

    String paymentEventsLocationFor(String chargeId) {
        return paymentLocationFor(configuration.getBaseUrl(), chargeId) + "/events";
    }

    String paymentRefundsLocationFor(String chargeId) {
        return paymentLocationFor(configuration.getBaseUrl(), chargeId) + "/refunds";
    }

    String paymentRefundLocationFor(String chargeId, String refundId) {
        return "http://publicapi.url" + PAYMENTS_PATH + chargeId + "/refunds/" + refundId;
    }

    String paymentCancelLocationFor(String chargeId) {
        return paymentLocationFor(configuration.getBaseUrl(), chargeId) + "/cancel";
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
