package uk.gov.pay.api.it;

import com.jayway.jsonassert.JsonAssert;
import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Test;
import uk.gov.pay.api.utils.ChargeEventBuilder;
import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.ws.rs.core.HttpHeaders;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.*;

public class PaymentsResourceITest extends PaymentResourceITestBase {

    private static final int AMOUNT = 9999999;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final String STATUS = "someState";
    private static final String PAYMENT_PROVIDER = "Sandbox";
    private static final String RETURN_URL = "http://somewhere.gov.uk/rainbow/1";
    private static final String REFERENCE = "Some reference <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String DESCRIPTION = "Some description <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final String CREATED_DATE = DateTimeUtils.toUTCDateString(TIMESTAMP);
    private static final Map<String, String> PAYMENT_CREATED = new ChargeEventBuilder(CHARGE_ID, STATUS, CREATED_DATE).build();
    private static final List<Map<String, String>> EVENTS = newArrayList(PAYMENT_CREATED);

    private static final String SUCCESS_PAYLOAD = paymentPayload(AMOUNT, RETURN_URL, DESCRIPTION, REFERENCE);

    @Test
    public void createPayment() {

        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);

        connectorMock.respondOk_whenCreateCharge(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CHARGE_TOKEN_ID, STATUS, RETURN_URL,
                DESCRIPTION, REFERENCE, PAYMENT_PROVIDER, CREATED_DATE);

        String expectedPaymentUrl = "http://localhost:" + app.getLocalPort() + PAYMENTS_PATH + CHARGE_ID;
        String expectedPaymentEventsUrl = "http://localhost:"+ app.getLocalPort() + PAYMENTS_PATH + CHARGE_ID + "/events";

        String responseBody = postPaymentResponse(BEARER_TOKEN, SUCCESS_PAYLOAD)
                .statusCode(201)
                .contentType(JSON)
                .header(HttpHeaders.LOCATION, is(expectedPaymentUrl))
                .body("payment_id", is(CHARGE_ID))
                .body("amount", is(9999999))
                .body("reference", is(REFERENCE))
                .body("description", is(DESCRIPTION))
                .body("status", is(STATUS))
                .body("return_url", is(RETURN_URL))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("created_date", is(CREATED_DATE))
                .body("_links.self.href", is(expectedPaymentUrl))
                .body("_links.self.method", is("GET"))
                .body("_links.next_url.href", is("http://Frontend/charge/" + CHARGE_TOKEN_ID))
                .body("_links.next_url.method", is("GET"))
                .body("_links.next_url_post.href", is("http://Frontend/charge/"))
                .body("_links.next_url_post.method", is("POST"))
                .body("_links.next_url_post.type", is("application/x-www-form-urlencoded"))
                .body("_links.next_url_post.params.chargeTokenId", is(CHARGE_TOKEN_ID))
                .body("_links.events.href", is(expectedPaymentEventsUrl))
                .body("_links.events.method", is("GET"))
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

        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenCreateCharge(minimumAmount, GATEWAY_ACCOUNT_ID, CHARGE_ID, CHARGE_TOKEN_ID, STATUS, RETURN_URL,
                DESCRIPTION, REFERENCE, PAYMENT_PROVIDER, CREATED_DATE);

        postPaymentResponse(BEARER_TOKEN, paymentPayload(minimumAmount, RETURN_URL, DESCRIPTION, REFERENCE))
                .statusCode(201)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("amount", is(minimumAmount))
                .body("reference", is(REFERENCE))
                .body("description", is(DESCRIPTION))
                .body("status", is(STATUS))
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

        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenCreateCharge(amount, GATEWAY_ACCOUNT_ID, CHARGE_ID, CHARGE_TOKEN_ID, STATUS, return_url,
                description, reference, PAYMENT_PROVIDER, CREATED_DATE);

        String body = new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("description", description)
                .add("return_url", return_url)
                .build();

        postPaymentResponse(BEARER_TOKEN, body)
                .statusCode(201)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("amount", is(amount))
                .body("reference", is(reference))
                .body("description", is(description))
                .body("status", is(STATUS))
                .body("return_url", is(return_url))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("created_date", is(CREATED_DATE));
    }

    @Test
    public void createPayment_responseWith500_whenConnectorResponseIsAnUnrecognisedError() {

        String gatewayAccountId = "1234567";
        String errorMessage = "something went wrong";

        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, gatewayAccountId);

        connectorMock.respondBadRequest_whenCreateCharge(AMOUNT, gatewayAccountId, errorMessage, RETURN_URL, DESCRIPTION, REFERENCE);

        postPaymentResponse(BEARER_TOKEN, SUCCESS_PAYLOAD)
                .statusCode(500)
                .body("code", is("P0198"))
                .body("message", is("Downstream system error"));

        connectorMock.verifyCreateChargeConnectorRequest(AMOUNT, gatewayAccountId, RETURN_URL, DESCRIPTION, REFERENCE);
    }

    @Test
    public void createPayment_responseWith500_whenTokenForGatewayAccountIsValidButConnectorResponseIsNotFound() {

        String notFoundGatewayAccountId = "9876545";
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, notFoundGatewayAccountId);

        connectorMock.respondNotFound_whenCreateCharge(AMOUNT, notFoundGatewayAccountId, RETURN_URL, DESCRIPTION, REFERENCE);

        postPaymentResponse(BEARER_TOKEN, SUCCESS_PAYLOAD)
                .statusCode(500)
                .body("code", is("P0199"))
                .body("message", is("There is an error with this account. Please contact support"));

        connectorMock.verifyCreateChargeConnectorRequest(AMOUNT, notFoundGatewayAccountId, RETURN_URL, DESCRIPTION, REFERENCE);
    }

    @Test
    public void createPayment_responseWith422_whenFieldsMissing() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        String nullCheck = " may not be null (was null)";
        String emptyCheck = " may not be empty (was null)";
        postPaymentResponse(BEARER_TOKEN, "{}")
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(4))
                .body("errors", hasItems(
                        "amount" + nullCheck,
                        "description" + emptyCheck,
                        "reference" + emptyCheck,
                        "returnUrl" + emptyCheck));
    }

    @Test
    public void getPayment_ReturnsPayment() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, STATUS, RETURN_URL,
                DESCRIPTION, REFERENCE, PAYMENT_PROVIDER, CREATED_DATE, CHARGE_TOKEN_ID);

        getPaymentResponse(BEARER_TOKEN, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("reference", is(REFERENCE))
                .body("description", is(DESCRIPTION))
                .body("amount", is(AMOUNT))
                .body("status", is(STATUS))
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
                .body("_links.next_url_post.params.chargeTokenId", is(CHARGE_TOKEN_ID));
    }

    @Test
    public void getPayment_Returns401_WhenUnauthorised() {
        publicAuthMock.respondUnauthorised();

        getPaymentResponse(BEARER_TOKEN, CHARGE_ID)
                .statusCode(401);
    }

    @Test
    public void getPayment_InvalidPaymentId() {
        String invalidPaymentId = "ds2af2afd3df112";
        String errorMessage = "backend-error-message";
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondChargeNotFound(GATEWAY_ACCOUNT_ID, invalidPaymentId, errorMessage);

        getPaymentResponse(BEARER_TOKEN, invalidPaymentId)
                .statusCode(404)
                .contentType(JSON)
                .body("message", is(errorMessage));
    }

    @Test
    public void getPaymentEvents_ReturnsPaymentEvents() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeEventsFound(GATEWAY_ACCOUNT_ID, CHARGE_ID, EVENTS);

        getPaymentEventsResponse(BEARER_TOKEN, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("_links.self.href", is(paymentEventsLocationFor(CHARGE_ID)))
                .body("events", hasSize(1))
                .body("events[0].payment_id", is(CHARGE_ID))
                .body("events[0].status", is(STATUS))
                .body("events[0].updated", is("2016-01-01T12:00:00Z"))
                .body("events[0]._links.payment_url.href", is(paymentLocationFor(CHARGE_ID)));
    }

    @Test
    public void getPaymentEvents_Returns401_WhenUnauthorised() {
        publicAuthMock.respondUnauthorised();

        getPaymentEventsResponse(BEARER_TOKEN, CHARGE_ID)
                .statusCode(401);
    }

    @Test
    public void getPaymentEvents_Returns404_WhenInvalidPaymentId() {
        String invalidPaymentId = "ds2af2afd3df112";
        String errorMessage = "backend-error-message";
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondChargeEventsNotFound(GATEWAY_ACCOUNT_ID, invalidPaymentId, errorMessage);

        getPaymentEventsResponse(BEARER_TOKEN, invalidPaymentId)
                .statusCode(404)
                .contentType(JSON)
                .body("message", is(errorMessage));
    }

    @Test
    public void createPayment_Returns401_WhenUnauthorised() {
        publicAuthMock.respondUnauthorised();

        postPaymentResponse(BEARER_TOKEN, SUCCESS_PAYLOAD)
                .statusCode(401);
    }

    @Test
    public void createPayment_Returns_WhenPublicAuthInaccessible() {
        publicAuthMock.respondWithError();

        postPaymentResponse(BEARER_TOKEN, SUCCESS_PAYLOAD)
                .statusCode(503);
    }

    private String paymentLocationFor(String chargeId) {
        return "http://localhost:" + app.getLocalPort() + PAYMENTS_PATH + chargeId;
    }

    private String paymentEventsLocationFor(String chargeId) {
        return paymentLocationFor(chargeId) + "/events";
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
