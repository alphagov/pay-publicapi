package uk.gov.pay.api.it;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.ExtractableResponse;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.response.ValidatableResponse;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.utils.ChargeEventBuilder;
import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.api.utils.JsonStringBuilder;

import java.time.ZonedDateTime;
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
import static org.junit.Assert.fail;

abstract public class ResourcesFilterITestBase extends PaymentResourceITestBase {

    protected static final int AMOUNT = 9999999;
    protected static final String CHARGE_ID = "ch_ab2341da231434l";
    protected static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    protected static final PaymentState CREATED = new PaymentState("created", false, null, null);
    protected static final RefundSummary REFUND_SUMMARY = new RefundSummary("pending", 100L, 50L);
    protected static final String PAYMENT_PROVIDER = "Sandbox";
    protected static final String CARD_BRAND = "master-card";
    protected static final String CARD_BRAND_LABEL = "Mastercard";
    protected static final String RETURN_URL = "https://somewhere.gov.uk/rainbow/1";
    protected static final String REFERENCE = "Some reference";
    protected static final String EMAIL = "alice.111@mail.fake";
    protected static final String DESCRIPTION = "Some description";
    protected static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    protected static final String CREATED_DATE = DateTimeUtils.toUTCDateString(TIMESTAMP);
    protected static final Map<String, String> PAYMENT_CREATED = new ChargeEventBuilder(CREATED, CREATED_DATE).build();
    protected static final List<Map<String, String>> EVENTS = Collections.singletonList(PAYMENT_CREATED);
    protected static final Address BILLING_ADDRESS = new Address("line1", "line2", "NR2 5 6EG", "city", "UK");
    protected static final CardDetails CARD_DETAILS = new CardDetails("1234", "Mr. Payment", "12/19", BILLING_ADDRESS, "Visa");

    protected static final String PAYLOAD = paymentPayload(AMOUNT, RETURN_URL, DESCRIPTION, REFERENCE);
    protected ExecutorService executor = Executors.newFixedThreadPool(2);

    @Before
    public void setupApiKey() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    protected List<ValidatableResponse> invokeAll(List<Callable<ValidatableResponse>> tasks) throws InterruptedException {
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

    protected TypeSafeMatcher<ValidatableResponse> aResponse(final int statusCode) {
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

    protected TypeSafeMatcher<ValidatableResponse> anErrorResponse(int statusCode, String publicApiErrorCode, String expectedDescription) {
        return new TypeSafeMatcher<ValidatableResponse>() {
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

    protected static String paymentPayload(long amount, String returnUrl, String description, String reference) {
        return new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("description", description)
                .add("return_url", returnUrl)
                .build();
    }

    protected ValidatableResponse getPaymentResponse(String bearerToken, String paymentId) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .get(PAYMENTS_PATH + paymentId)
                .then();
    }

    protected ValidatableResponse getPaymentEventsResponse(String bearerToken, String paymentId) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .get(String.format("/v1/payments/%s/events", paymentId))
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

    protected ValidatableResponse searchPayments(String bearerToken, ImmutableMap<String, String> queryParams) {
        return given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .queryParameters(queryParams)
                .get("/v1/payments")
                .then();
    }

    protected ValidatableResponse postCancelPaymentResponse(String bearerToken, String paymentId) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .post(String.format("/v1/payments/%s/cancel", paymentId))
                .then();
    }
}
