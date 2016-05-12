package uk.gov.pay.api.it;

import com.jayway.jsonassert.JsonAssert;
import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Test;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.utils.ChargeEventBuilder;
import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.http.HttpStatus.SC_NOT_ACCEPTABLE;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

public class PaymentsResourceITest extends PaymentResourceITestBase {

    private static final int AMOUNT = 9999999;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final PaymentState STATE = new PaymentState("created", false, null, null);
    private static final String PAYMENT_PROVIDER = "Sandbox";
    private static final String RETURN_URL = "http://somewhere.gov.uk/rainbow/1";
    private static final String REFERENCE = "Some reference <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String DESCRIPTION = "Some description <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final String CREATED_DATE = DateTimeUtils.toUTCDateString(TIMESTAMP);
    private static final Map<String, String> PAYMENT_CREATED = new ChargeEventBuilder(STATE, CREATED_DATE).build();
    private static final List<Map<String, String>> EVENTS = Collections.singletonList(PAYMENT_CREATED);

    private static final String SUCCESS_PAYLOAD = paymentPayload(AMOUNT, RETURN_URL, DESCRIPTION, REFERENCE);

    @Test
    public void createPayment() {

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);

        connectorMock.respondOk_whenCreateCharge(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CHARGE_TOKEN_ID,
                STATE, RETURN_URL, DESCRIPTION, REFERENCE, PAYMENT_PROVIDER, CREATED_DATE);

        String responseBody = postPaymentResponse(API_KEY, SUCCESS_PAYLOAD)
                .statusCode(201)
                .contentType(JSON)
                .header(HttpHeaders.LOCATION, is(paymentLocationFor(CHARGE_ID)))
                .body("payment_id", is(CHARGE_ID))
                .body("amount", is(9999999))
                .body("reference", is(REFERENCE))
                .body("description", is(DESCRIPTION))
                .body("state.status", is(STATE.getStatus()))
                .body("return_url", is(RETURN_URL))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("created_date", is(CREATED_DATE))
                .body("_links.self.href", is(paymentLocationFor(CHARGE_ID)))
                .body("_links.self.method", is("GET"))
                .body("_links.next_url.href", is("http://Frontend/charge/" + CHARGE_TOKEN_ID))
                .body("_links.next_url.method", is("GET"))
                .body("_links.next_url_post.href", is("http://Frontend/charge/"))
                .body("_links.next_url_post.method", is("POST"))
                .body("_links.next_url_post.type", is("application/x-www-form-urlencoded"))
                .body("_links.next_url_post.params.chargeTokenId", is(CHARGE_TOKEN_ID))
                .body("_links.events.href", is(paymentEventsLocationFor(CHARGE_ID)))
                .body("_links.events.method", is("GET"))
                .body("_links.cancel.href", is(paymentCancelLocationFor(CHARGE_ID)))
                .body("_links.cancel.method", is("POST"))
                .extract().body().asString();

        JsonAssert.with(responseBody)
                .assertNotDefined("_links.self.type")
                .assertNotDefined("_links.self.params")
                .assertNotDefined("_links.next_url.type")
                .assertNotDefined("_links.next_url.params")
                .assertNotDefined("_links.events.type")
                .assertNotDefined("_links.events.params");

        connectorMock.verifyCreateChargeConnectorRequest(AMOUNT, GATEWAY_ACCOUNT_ID, RETURN_URL, DESCRIPTION, REFERENCE);
    }

    @Test
    public void createPayment_withMinimumAmount() {

        int minimumAmount = 1;

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenCreateCharge(minimumAmount, GATEWAY_ACCOUNT_ID, CHARGE_ID, CHARGE_TOKEN_ID,
                STATE, RETURN_URL, DESCRIPTION, REFERENCE, PAYMENT_PROVIDER, CREATED_DATE);

        postPaymentResponse(API_KEY, paymentPayload(minimumAmount, RETURN_URL, DESCRIPTION, REFERENCE))
                .statusCode(201)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("amount", is(minimumAmount))
                .body("reference", is(REFERENCE))
                .body("description", is(DESCRIPTION))
                .body("return_url", is(RETURN_URL))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("created_date", is(CREATED_DATE));

        connectorMock.verifyCreateChargeConnectorRequest(minimumAmount, GATEWAY_ACCOUNT_ID, RETURN_URL, DESCRIPTION, REFERENCE);
    }

    @Test
    public void createPayment_withAllFieldsUpToMaxLengthBoundaries_shouldBeAccepted() {

        int amount = 10000000;
        String reference = randomAlphanumeric(255);
        String description = randomAlphanumeric(255);
        String return_url = "http://govdemopay.gov.uk?data=" + randomAlphanumeric(1970);

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenCreateCharge(amount, GATEWAY_ACCOUNT_ID, CHARGE_ID, CHARGE_TOKEN_ID,
                STATE, return_url, description, reference, PAYMENT_PROVIDER, CREATED_DATE);

        String body = new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("description", description)
                .add("return_url", return_url)
                .build();

        postPaymentResponse(API_KEY, body)
                .statusCode(201)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("amount", is(amount))
                .body("reference", is(reference))
                .body("description", is(description))
                .body("return_url", is(return_url))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("created_date", is(CREATED_DATE));
    }

    @Test
    public void createPayment_responseWith500_whenConnectorResponseIsAnUnrecognisedError() throws Exception {

        String gatewayAccountId = "1234567";
        String errorMessage = "something went wrong";

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, gatewayAccountId);

        connectorMock.respondBadRequest_whenCreateCharge(AMOUNT, gatewayAccountId, errorMessage, RETURN_URL, DESCRIPTION, REFERENCE);

        InputStream body = postPaymentResponse(API_KEY, SUCCESS_PAYLOAD)
                .statusCode(500)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0198"))
                .assertThat("$.description", is("Downstream system error"));

        connectorMock.verifyCreateChargeConnectorRequest(AMOUNT, gatewayAccountId, RETURN_URL, DESCRIPTION, REFERENCE);
    }

    @Test
    public void createPayment_responseWith500_whenTokenForGatewayAccountIsValidButConnectorResponseIsNotFound() {

        String notFoundGatewayAccountId = "9876545";
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, notFoundGatewayAccountId);

        connectorMock.respondNotFound_whenCreateCharge(AMOUNT, notFoundGatewayAccountId, RETURN_URL, DESCRIPTION, REFERENCE);

        postPaymentResponse(API_KEY, SUCCESS_PAYLOAD)
                .statusCode(500)
                .contentType(JSON)
                .body("code", is("P0199"))
                .body("description", is("There is an error with this account. Please contact support"));

        connectorMock.verifyCreateChargeConnectorRequest(AMOUNT, notFoundGatewayAccountId, RETURN_URL, DESCRIPTION, REFERENCE);
    }

    @Test
    public void getPayment_ReturnsPayment() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, STATE, RETURN_URL,
                DESCRIPTION, REFERENCE, PAYMENT_PROVIDER, CREATED_DATE, CHARGE_TOKEN_ID);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("reference", is(REFERENCE))
                .body("description", is(DESCRIPTION))
                .body("amount", is(AMOUNT))
                .body("state.status", is(STATE))
                .body("return_url", is(RETURN_URL))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("created_date", is(CREATED_DATE))
                .body("_links.self.href", is(paymentLocationFor(CHARGE_ID)))
                .body("_links.self.method", is("GET"))
                .body("_links.events.href", is(paymentEventsLocationFor(CHARGE_ID)))
                .body("_links.events.method", is("GET"))
                .body("_links.next_url.href", is("http://Frontend/charge/" + CHARGE_ID))
                .body("_links.next_url.method", is("GET"))
                .body("_links.next_url_post.href", is("http://Frontend/charge/"))
                .body("_links.next_url_post.method", is("POST"))
                .body("_links.next_url_post.type", is("application/x-www-form-urlencoded"))
                .body("_links.next_url_post.params.chargeTokenId", is(CHARGE_TOKEN_ID))
                .body("_links.events.href", is(paymentEventsLocationFor(CHARGE_ID)))
                .body("_links.events.method", is("GET"))
                .body("_links.next_url_post.params.chargeTokenId", is(CHARGE_TOKEN_ID))
                .body("_links.cancel.href", is(paymentCancelLocationFor(CHARGE_ID)))
                .body("_links.cancel.method", is("POST"));
    }

    @Test
    public void getPayment_ShouldNotIncludeCancelLinkIfPaymentCannotBeCancelled() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID,
                new PaymentState("confirmed", true, null, null),
                RETURN_URL, DESCRIPTION, REFERENCE, PAYMENT_PROVIDER, CREATED_DATE, CHARGE_TOKEN_ID);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("_links.cancel", is(nullValue()));
    }

    @Test
    public void getPayment_Returns401_WhenUnauthorised() {
        publicAuthMock.respondUnauthorised();

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(401);
    }

    @Test
    public void getPayment_returns404_whenConnectorRespondsWith404() throws IOException {

        String paymentId = "ds2af2afd3df112";
        String errorMessage = "backend-error-message";
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondChargeNotFound(GATEWAY_ACCOUNT_ID, paymentId, errorMessage);

        InputStream body = getPaymentResponse(API_KEY, paymentId)
                .statusCode(404)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0200"))
                .assertThat("$.description", is("Not found"));
    }

    @Test
    public void getPayment_returns500_whenConnectorRespondsWithResponseOtherThan200Or404() throws IOException {

        String paymentId = "ds2af2afd3df112";
        String errorMessage = "backend-error-message";
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWhenGetCharge(GATEWAY_ACCOUNT_ID, paymentId, errorMessage, SC_NOT_ACCEPTABLE);

        InputStream body = getPaymentResponse(API_KEY, paymentId)
                .statusCode(500)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0298"))
                .assertThat("$.description", is("Downstream system error"));
    }

    @Test
    public void getPaymentEvents_ReturnsPaymentEvents() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeEventsFound(GATEWAY_ACCOUNT_ID, CHARGE_ID, EVENTS);

        getPaymentEventsResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("_links.self.href", is(paymentEventsLocationFor(CHARGE_ID)))
                .body("events", hasSize(1))
                .body("events[0].payment_id", is(CHARGE_ID))
                .body("events[0].state.status", is(STATE))
                .body("events[0].updated", is("2016-01-01T12:00:00Z"))
                .body("events[0]._links.payment_url.href", is(paymentLocationFor(CHARGE_ID)));
    }

    @Test
    public void getPaymentEvents_Returns401_WhenUnauthorised() {
        publicAuthMock.respondUnauthorised();

        getPaymentEventsResponse(API_KEY, CHARGE_ID)
                .statusCode(401);
    }

    @Test
    public void getPaymentEvents_returns404_whenConnectorRespondsWith404() throws IOException {

        String paymentId = "ds2af2afd3df112";
        String errorMessage = "backend-error-message";
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondChargeEventsNotFound(GATEWAY_ACCOUNT_ID, paymentId, errorMessage);

        InputStream body = getPaymentEventsResponse(API_KEY, paymentId)
                .statusCode(404)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0300"))
                .assertThat("$.description", is("Not found"));
    }

    @Test
    public void getPaymentEvents_returns500_whenConnectorRespondsWithResponseOtherThan200Or404() throws IOException {

        String paymentId = "ds2af2afd3df112";
        String errorMessage = "backend-error-message";
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWhenGetChargeEvents(GATEWAY_ACCOUNT_ID, paymentId, errorMessage, SC_NOT_ACCEPTABLE);

        InputStream body = getPaymentEventsResponse(API_KEY, paymentId)
                .statusCode(500)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0398"))
                .assertThat("$.description", is("Downstream system error"));
    }

    @Test
    public void createPayment_Returns401_WhenUnauthorised() {
        publicAuthMock.respondUnauthorised();

        postPaymentResponse(API_KEY, SUCCESS_PAYLOAD)
                .statusCode(401);
    }

    @Test
    public void createPayment_Returns_WhenPublicAuthInaccessible() {
        publicAuthMock.respondWithError();

        postPaymentResponse(API_KEY, SUCCESS_PAYLOAD)
                .statusCode(503);
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
}
