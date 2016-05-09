package uk.gov.pay.api.it;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.ExtractableResponse;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.utils.ChargeEventBuilder;
import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.api.utils.JsonStringBuilder;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static uk.gov.pay.api.it.fixtures.PaymentSearchResultBuilder.aSuccessfulSearchResponse;

public class ResourcesFiltersITest extends PaymentResourceITestBase {

    private static final int AMOUNT = 9999999;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final PaymentState STATE = new PaymentState("created", false, null, null);
    private static final String STATUS = "created";
    private static final String PAYMENT_PROVIDER = "Sandbox";
    private static final String RETURN_URL = "http://somewhere.gov.uk/rainbow/1";
    private static final String REFERENCE = "Some reference";
    private static final String DESCRIPTION = "Some description";
    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final String CREATED_DATE = DateTimeUtils.toUTCDateString(TIMESTAMP);
    private static final Map<String, String> PAYMENT_CREATED = new ChargeEventBuilder(STATE, STATUS, CREATED_DATE).build();
    private static final List<Map<String, String>> EVENTS = Collections.singletonList(PAYMENT_CREATED);

    private static final String PAYLOAD = paymentPayload(AMOUNT, RETURN_URL, DESCRIPTION, REFERENCE);
    private ExecutorService executor = Executors.newFixedThreadPool(2);

    @Before
    public void setupApiKey() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void createPayment_whenRateLimitIsReached_shouldReturn429Response() throws Exception {

        connectorMock.respondOk_whenCreateCharge(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CHARGE_TOKEN_ID, STATE, STATUS,
                RETURN_URL, DESCRIPTION, REFERENCE, PAYMENT_PROVIDER, CREATED_DATE);

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> postPaymentResponse(API_KEY, PAYLOAD),
                () -> postPaymentResponse(API_KEY, PAYLOAD)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        assertThat(finishedTasks, hasItem(aResponse(201)));
        assertThat(finishedTasks, hasItem(anErrorResponse(429, "P0900", "Too many requests")));
    }

    @Test
    public void createPayment_whenInvalidAuthorizationHeader_shouldReturn401Response() throws Exception {

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> postPaymentResponse("InvalidToken", PAYLOAD),
                () -> postPaymentResponse("InvalidToken", PAYLOAD)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        finishedTasks.get(0).statusCode(401);
        finishedTasks.get(1).statusCode(401);
    }

    @Test
    public void getPayment_whenRateLimitIsReached_shouldReturn429Response() throws Exception {

        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, STATE, STATUS,
                RETURN_URL, DESCRIPTION, REFERENCE, PAYMENT_PROVIDER, CREATED_DATE, CHARGE_TOKEN_ID);

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> getPaymentResponse(API_KEY, CHARGE_ID),
                () -> getPaymentResponse(API_KEY, CHARGE_ID)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        assertThat(finishedTasks, hasItem(aResponse(200)));
        assertThat(finishedTasks, hasItem(anErrorResponse(429, "P0900", "Too many requests")));
    }

    @Test
    public void getPayment_whenInvalidAuthorizationHeader_shouldReturn401Response() throws Exception {

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> getPaymentResponse("InvalidToken", CHARGE_ID),
                () -> getPaymentResponse("InvalidToken", CHARGE_ID)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        finishedTasks.get(0).statusCode(401);
        finishedTasks.get(1).statusCode(401);
    }

    @Test
    public void getPaymentEvents_whenRateLimitIsReached_shouldReturn429Response() throws Exception {

        connectorMock.respondWithChargeEventsFound(GATEWAY_ACCOUNT_ID, CHARGE_ID, EVENTS);

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> getPaymentEventsResponse(API_KEY, CHARGE_ID),
                () -> getPaymentEventsResponse(API_KEY, CHARGE_ID)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        assertThat(finishedTasks, hasItem(aResponse(200)));
        assertThat(finishedTasks, hasItem(anErrorResponse(429, "P0900", "Too many requests")));
    }

    @Test
    public void getPaymentEvents_whenInvalidAuthorizationHeader_shouldReturn401Response() throws Exception {

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> getPaymentEventsResponse("InvalidToken", CHARGE_ID),
                () -> getPaymentEventsResponse("InvalidToken", CHARGE_ID)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        finishedTasks.get(0).statusCode(401);
        finishedTasks.get(1).statusCode(401);
    }

    @Test
    public void searchPayments_whenRateLimitIsReached_shouldReturn429Response() throws Exception {

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, REFERENCE, null, null, null,
                aSuccessfulSearchResponse()
                        .withMatchingInProgressState("created")
                        .withMatchingReference(REFERENCE)
                        .numberOfResults(1)
                        .build());

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> searchPayments(API_KEY, ImmutableMap.of("reference", REFERENCE)),
                () -> searchPayments(API_KEY, ImmutableMap.of("reference", REFERENCE))
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        assertThat(finishedTasks, hasItem(aResponse(200)));
        assertThat(finishedTasks, hasItem(anErrorResponse(429, "P0900", "Too many requests")));
    }

    @Test
    public void searchPayments_whenInvalidAuthorizationHeader_shouldReturn401Response() throws Exception {

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> searchPayments("InvalidToken", ImmutableMap.of("reference", REFERENCE)),
                () -> searchPayments("InvalidToken", ImmutableMap.of("reference", REFERENCE))
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        finishedTasks.get(0).statusCode(401);
        finishedTasks.get(1).statusCode(401);
    }

    @Test
    public void cancelPayment_whenRateLimitIsReached_shouldReturn429Response() throws Exception {

        connectorMock.respondOk_whenCancelCharge(CHARGE_ID, GATEWAY_ACCOUNT_ID);

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> postCancelPaymentResponse(API_KEY, CHARGE_ID),
                () -> postCancelPaymentResponse(API_KEY, CHARGE_ID)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        assertThat(finishedTasks, hasItem(aResponse(204)));
        assertThat(finishedTasks, hasItem(anErrorResponse(429, "P0900", "Too many requests")));
    }

    @Test
    public void cancelPayment_whenInvalidAuthorizationHeader_shouldReturn401Response() throws Exception {

        List<Callable<ValidatableResponse>> tasks = Arrays.asList(
                () -> postCancelPaymentResponse("InvalidToken", CHARGE_ID),
                () -> postCancelPaymentResponse("InvalidToken", CHARGE_ID)
        );

        List<ValidatableResponse> finishedTasks = invokeAll(tasks);

        finishedTasks.get(0).statusCode(401);
        finishedTasks.get(1).statusCode(401);
    }

    private List<ValidatableResponse> invokeAll(List<Callable<ValidatableResponse>> tasks) throws InterruptedException {
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
                .collect(Collectors.toList());
    }

    private TypeSafeMatcher<ValidatableResponse> aResponse(final int statusCode) {
        return new TypeSafeMatcher<ValidatableResponse>() {
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

    private TypeSafeMatcher<ValidatableResponse> anErrorResponse(int statusCode, String publicApiErrorCode, String expectedDescription) {
        return new TypeSafeMatcher<ValidatableResponse>() {
            @Override
            protected boolean matchesSafely(ValidatableResponse validatableResponse) {
                ExtractableResponse<Response> extract = validatableResponse.extract();
                System.out.println("extract.body().asString() = " + extract.body().asString());
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

    private ValidatableResponse getPaymentResponse(String bearerToken, String paymentId) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .get(PAYMENTS_PATH + paymentId)
                .then();
    }

    private ValidatableResponse getPaymentEventsResponse(String bearerToken, String paymentId) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .get(String.format("/v1/payments/%s/events", paymentId))
                .then();
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

    private ValidatableResponse searchPayments(String bearerToken, ImmutableMap<String, String> queryParams) {
        return given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .queryParameters(queryParams)
                .get("/v1/payments")
                .then();
    }

    private ValidatableResponse postCancelPaymentResponse(String bearerToken, String paymentId) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .post(String.format("/v1/payments/%s/cancel", paymentId))
                .then();
    }
}
