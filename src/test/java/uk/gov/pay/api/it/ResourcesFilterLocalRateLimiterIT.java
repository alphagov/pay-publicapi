package uk.gov.pay.api.it;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.dropwizard.testing.DropwizardTestSupport;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardDetailsFromResponse;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.utils.ApiKeyGenerator;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.api.utils.PublicAuthMockClientJUnit5;
import uk.gov.pay.api.utils.mocks.ConnectorMockClientJUnit5;
import uk.gov.service.payments.commons.model.SupportedLanguage;
import uk.gov.service.payments.commons.validation.DateTimeUtils;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.pay.api.utils.mocks.ChargeResponseFromConnector.ChargeResponseFromConnectorBuilder.aCreateOrGetChargeResponseFromConnector;
import static uk.gov.service.payments.commons.model.CommonDateTimeFormatters.ISO_INSTANT_MILLISECOND_PRECISION;

class ResourcesFilterLocalRateLimiterIT {

    private static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");
    private static final String GATEWAY_ACCOUNT_ID = "GATEWAY_ACCOUNT_ID";
    private static final String PAYMENTS_PATH = "/v1/payments/";

    private static final int AMOUNT = 9999999;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final PaymentState CREATED = new PaymentState("created", false, null, null);
    private static final RefundSummary REFUND_SUMMARY = new RefundSummary("pending", 100L, 50L);
    private static final String PAYMENT_PROVIDER = "Sandbox";
    private static final String RETURN_URL = "https://somewhere.gov.uk/rainbow/1";
    private static final String REFERENCE = "Some reference";
    private static final String DESCRIPTION = "Some description";
    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final String CREATED_DATE = ISO_INSTANT_MILLISECOND_PRECISION.format(TIMESTAMP);
    private static final Address BILLING_ADDRESS = new Address("line1", "line2", "NR2 5 6EG", "city", "UK");
    private static final CardDetailsFromResponse CARD_DETAILS = new CardDetailsFromResponse("1234", "123456", "Mr. Payment", "12/19", BILLING_ADDRESS, "Visa", "credit");

    private static final String PAYLOAD = paymentPayload(AMOUNT, RETURN_URL, DESCRIPTION, REFERENCE);
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    @RegisterExtension
    private static final WireMockExtension publicAuthServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @RegisterExtension
    private static final WireMockExtension connectorServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private static DropwizardTestSupport<PublicApiConfig> app;

    @BeforeEach
    void setup() {
        ConnectorMockClientJUnit5 connectorMockClient = new ConnectorMockClientJUnit5(connectorServer);
        PublicAuthMockClientJUnit5 publicAuthMockClient = new PublicAuthMockClientJUnit5(publicAuthServer);

        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);

        connectorMockClient.respondCreated_whenCreateCharge(CHARGE_TOKEN_ID, GATEWAY_ACCOUNT_ID, aCreateOrGetChargeResponseFromConnector()
                .withAmount(AMOUNT)
                .withChargeId(CHARGE_ID)
                .withState(CREATED)
                .withReturnUrl(RETURN_URL)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withPaymentProvider(PAYMENT_PROVIDER)
                .withCreatedDate(CREATED_DATE)
                .withLanguage(SupportedLanguage.ENGLISH)
                .withDelayedCapture(false)
                .withRefundSummary(REFUND_SUMMARY)
                .withCardDetails(CARD_DETAILS)
                .build());
    }

    @BeforeAll
    static void startApp() throws Exception {
        app = new DropwizardTestSupport<>(
                PublicApi.class,
                resourceFilePath("config/test-config.yaml"),
                config("connectorUrl", connectorServer.baseUrl()),
                config("publicAuthUrl", publicAuthServer.baseUrl() + "/v1/auth")
        );
        app.before();
    }

    @AfterAll
    static void stopApp() {
        app.after();
    }

    @Test
    void shouldFallbackToLocalRateLimiter_whenRedisIsUnavailableAndRateLimitIsReached_send429Response() throws Exception {

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> postPaymentResponse(API_KEY, PAYLOAD),
                () -> postPaymentResponse(API_KEY, PAYLOAD),
                () -> postPaymentResponse(API_KEY, PAYLOAD)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        assertThat(finishedTasks, hasItem(anErrorResponse(429, "P0900", "Too many requests")));
    }

    @Test
    void shouldFallbackToLocalRateLimiter_whenRedisIsUnavailableAndContinueRequest() {

        ValidatableResponse response = postPaymentResponse(API_KEY, PAYLOAD);

        response.statusCode(201);
    }

    private List<ValidatableResponse> invokeAll(List<Callable<ValidatableResponse>> tasks) throws InterruptedException {
        return executor.invokeAll(tasks)
                .stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .toList();
    }

    private TypeSafeMatcher<ValidatableResponse> anErrorResponse(int statusCode, String publicApiErrorCode, String expectedDescription) {
        return new TypeSafeMatcher<>() {
            @Override
            protected boolean matchesSafely(ValidatableResponse validatableResponse) {
                ExtractableResponse<Response> extract = validatableResponse.extract();
                return extract.statusCode() == statusCode
                        && publicApiErrorCode.equals(extract.body().<String>path("code"))
                        && expectedDescription.equals(extract.body().<String>path("description"));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(" Status code: ")
                        .appendValue(statusCode)
                        .appendText(", error code: ")
                        .appendValue(publicApiErrorCode)
                        .appendText(", message: ")
                        .appendValue(expectedDescription)
                ;
            }
        };
    }

    private static String paymentPayload(long amount, String returnUrl, String description, String reference) {
        return new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("description", description)
                .add("return_url", returnUrl)
                .build();
    }

    private ValidatableResponse postPaymentResponse(String bearerToken, String payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .post(PAYMENTS_PATH)
                .then();
    }
}
