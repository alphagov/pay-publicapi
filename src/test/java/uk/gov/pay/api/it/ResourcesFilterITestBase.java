package uk.gov.pay.api.it;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.DropwizardTestSupport;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.it.rule.RedisDockerRule;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.utils.ApiKeyGenerator;
import uk.gov.pay.api.utils.ChargeEventBuilder;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.api.utils.PublicAuthMockClientJUnit5;
import uk.gov.service.payments.commons.validation.DateTimeUtils;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.eclipse.jetty.http.HttpStatus.TOO_MANY_REQUESTS_429;
import static org.junit.jupiter.api.Assertions.fail;
import static uk.gov.service.payments.commons.model.CommonDateTimeFormatters.ISO_INSTANT_MILLISECOND_PRECISION;

public abstract class ResourcesFilterITestBase {

    static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf"); //Must use same secret set in test-config.xml's apiKeyHmacSecret
    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").orElseThrow();
    private static final String PAYMENTS_PATH = "/v1/payments/";
    static final int AMOUNT = 9999999;
    static final String CHARGE_ID = "ch_ab2341da231434l";
    static final PaymentState CREATED = new PaymentState("created", false, null, null);
    static final String RETURN_URL = "https://somewhere.gov.uk/rainbow/1";
    static final String REFERENCE = "Some reference";
    static final String DESCRIPTION = "Some description";
    static final String CREATED_DATE = ISO_INSTANT_MILLISECOND_PRECISION.format(TIMESTAMP);
    static final List<Map<String, String>> EVENTS = List.of(new ChargeEventBuilder(CREATED, CREATED_DATE).build());
    static final String GATEWAY_ACCOUNT_ID = "GATEWAY_ACCOUNT_ID";
    static final String PAYLOAD = paymentPayload();

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @RegisterExtension
    private static final RedisDockerRule redisDockerRule = new RedisDockerRule();

    @RegisterExtension
    static final WireMockExtension connectorServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @RegisterExtension
    private static final WireMockExtension publicAuthServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @RegisterExtension
    static final WireMockExtension ledgerServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private static DropwizardTestSupport<PublicApiConfig> app;

    private final PublicAuthMockClientJUnit5 publicAuthMockClient = new PublicAuthMockClientJUnit5(publicAuthServer);

    @BeforeAll
    static void startApp() throws Exception {
        app = new DropwizardTestSupport<>(
                PublicApi.class,
                resourceFilePath("config/test-config.yaml"),
                config("connectorUrl", connectorServer.baseUrl()),
                config("publicAuthUrl", publicAuthServer.baseUrl() + "/v1/auth"),
                config("redis.endpoint", redisDockerRule.getRedisUrl()),
                config("ledgerUrl", ledgerServer.baseUrl()),
                config("rateLimiter.noOfReq", "1"),
                config("rateLimiter.noOfReqForPost", "1")
        );
        app.before();
    }

    @AfterAll
    static void afterAll() {
        app.after();
    }

    @BeforeEach
    public void setupApiKey() {
        redisDockerRule.clearCache();
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    List<ValidatableResponse> invokeAll(List<Callable<ValidatableResponse>> tasks) throws InterruptedException {
        return executor.invokeAll(tasks)
                .stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        fail("Test fail with exception calling resource");
                        return null;
                    }
                })
                .toList();
    }

    TypeSafeMatcher<ValidatableResponse> aResponse(final int statusCode) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(ValidatableResponse validatableResponse) {
                return validatableResponse.extract().statusCode() == statusCode;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" Status code: ")
                        .appendValue(statusCode);
            }
        };
    }

    TypeSafeMatcher<ValidatableResponse> anErrorResponse() {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(ValidatableResponse validatableResponse) {
                ExtractableResponse<Response> extract = validatableResponse.extract();
                return extract.statusCode() == TOO_MANY_REQUESTS_429
                        && "P0900".equals(extract.body().<String>path("code"))
                        && "Too many requests".equals(extract.body().<String>path("description"));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" Status code: ")
                        .appendValue(TOO_MANY_REQUESTS_429)
                        .appendText(", error code: ")
                        .appendValue("P0900")
                        .appendText(", message: ")
                        .appendValue("Too many requests")
                ;
            }
        };
    }

    private static String paymentPayload() {
        return new JsonStringBuilder()
                .add("amount", (long) AMOUNT)
                .add("reference", REFERENCE)
                .add("description", DESCRIPTION)
                .add("return_url", RETURN_URL)
                .build();
    }

    ValidatableResponse getPaymentResponse(String bearerToken) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .get(PAYMENTS_PATH + CHARGE_ID)
                .then();
    }

    ValidatableResponse getPaymentEventsResponse(String bearerToken) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .get(String.format("/v1/payments/%s/events", CHARGE_ID))
                .then();
    }

    protected ValidatableResponse postPaymentResponse(String bearerToken, String payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .post(PAYMENTS_PATH)
                .then();
    }

    ValidatableResponse searchPayments(String bearerToken, ImmutableMap<String, String> queryParams) {
        return given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .queryParams(queryParams)
                .get("/v1/payments")
                .then();
    }

    ValidatableResponse postCancelPaymentResponse(String bearerToken) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .post(String.format("/v1/payments/%s/cancel", CHARGE_ID))
                .then();
    }
}
