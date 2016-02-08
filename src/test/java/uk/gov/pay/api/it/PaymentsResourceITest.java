package uk.gov.pay.api.it;

import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Test;
import uk.gov.pay.api.utils.ChargeEventBuilder;

import javax.ws.rs.core.HttpHeaders;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonStringBuilder;

public class PaymentsResourceITest extends PaymentResourceITestBase {
    private static final String PAYMENT_EVENTS_PATH = "/v1/payments/%s/events";
    private static final long TEST_AMOUNT = 20032123132120l;
    private static final String TEST_CHARGE_ID = "ch_ab2341da231434l";
    private static final String TEST_STATUS = "someState";
    private static final String TEST_PAYMENT_PROVIDER = "Sandbox";
    private static final String TEST_RETURN_URL = "http://somewhere.over.the/rainbow/{paymentID}";
    private static final String TEST_REFERENCE = "Some reference <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String TEST_DESCRIPTION = "Some description <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final LocalDateTime TEST_TIMESTAMP = LocalDateTime.of(2016, Month.JANUARY, 1, 12, 00, 00);
    private static final String TEST_CREATED_DATE = TEST_TIMESTAMP.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:SS"));
    private static final Map<String, String> TEST_PAYMENT_CREATED = new ChargeEventBuilder(TEST_CHARGE_ID, TEST_STATUS, TEST_TIMESTAMP).build();
    private static final List<Map<String, String>> TEST_EVENTS = newArrayList(TEST_PAYMENT_CREATED);

    private static final String SUCCESS_PAYLOAD = paymentPayload(TEST_AMOUNT, TEST_RETURN_URL, TEST_DESCRIPTION, TEST_REFERENCE);

    @Test
    public void createPayment() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenCreateCharge(TEST_AMOUNT, GATEWAY_ACCOUNT_ID, TEST_CHARGE_ID, TEST_STATUS, TEST_RETURN_URL,
                TEST_DESCRIPTION, TEST_REFERENCE, TEST_PAYMENT_PROVIDER, TEST_CREATED_DATE);

        ValidatableResponse response = postPaymentResponse(BEARER_TOKEN, SUCCESS_PAYLOAD)
                .statusCode(201)
                .contentType(JSON)
                .body("payment_id", is(TEST_CHARGE_ID))
                .body("amount", is(TEST_AMOUNT))
                .body("reference", is(escapeHtml4(TEST_REFERENCE)))
                .body("description", is(escapeHtml4(TEST_DESCRIPTION)))
                .body("status", is(TEST_STATUS))
                .body("return_url", is(TEST_RETURN_URL))
                .body("payment_provider", is(TEST_PAYMENT_PROVIDER))
                .body("created_date", is(TEST_CREATED_DATE));

        String paymentId = response.extract().path("payment_id");
        assertThat(paymentId, is(TEST_CHARGE_ID));

        String paymentUrl = paymentLocationFor(paymentId);

        response.header(HttpHeaders.LOCATION, is(paymentUrl));

        response.body("_links.self.href", is(paymentUrl));
        response.body("_links.self.method", is("GET"));
        response.body("_links.next_url.href", is(cardDetailsUrlFor(TEST_CHARGE_ID)));
        response.body("_links.next_url.method", is("GET"));

        connectorMock.verifyCreateCharge(TEST_AMOUNT, GATEWAY_ACCOUNT_ID, TEST_RETURN_URL, TEST_DESCRIPTION, TEST_REFERENCE);
    }

    @Test
    public void createPayment_responseWith4xx_whenInvalidGatewayAccount() {
        String invalidGatewayAccountId = "ada2dfa323";
        String errorMessage = "something went wrong";
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, invalidGatewayAccountId);

        connectorMock.respondUnknownGateway_whenCreateCharge(TEST_AMOUNT, invalidGatewayAccountId, errorMessage, TEST_RETURN_URL, TEST_DESCRIPTION, TEST_REFERENCE);

        postPaymentResponse(BEARER_TOKEN, SUCCESS_PAYLOAD)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is(errorMessage));

        connectorMock.verifyCreateCharge(TEST_AMOUNT, invalidGatewayAccountId, TEST_RETURN_URL, TEST_DESCRIPTION, TEST_REFERENCE);
    }

    @Test
    public void createPayment_responseWith4xx_whenFieldsMissing() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        String nullCheck = " may not be null (was null)";
        String emptyCheck = " may not be empty (was null)";
        postPaymentResponse(BEARER_TOKEN, "{}")
                .statusCode(422)
                .contentType(JSON)
                .body("errors", is(Arrays.asList(
                            "amount"      + nullCheck,
                            "description" + emptyCheck,
                            "reference"   + emptyCheck,
                            "returnUrl"   + emptyCheck)));
    }

    @Test
    public void createPayment_responseWith4xx_whenConnectorResponseEmpty() {
        connectorMock.respondOk_withEmptyBody(TEST_AMOUNT, GATEWAY_ACCOUNT_ID, TEST_CHARGE_ID, TEST_RETURN_URL, TEST_DESCRIPTION, TEST_REFERENCE);
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);

        postPaymentResponse(BEARER_TOKEN, SUCCESS_PAYLOAD)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Connector response contains no payload!"));
    }

    @Test
    public void getPayment_ReturnsPayment() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeFound(TEST_AMOUNT, GATEWAY_ACCOUNT_ID, TEST_CHARGE_ID, TEST_STATUS, TEST_RETURN_URL,
                TEST_DESCRIPTION, TEST_REFERENCE, TEST_PAYMENT_PROVIDER, TEST_CREATED_DATE);

        ValidatableResponse response = getPaymentResponse(BEARER_TOKEN, TEST_CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(TEST_CHARGE_ID))
                .body("reference", is(escapeHtml4(TEST_REFERENCE)))
                .body("description", is(escapeHtml4(TEST_DESCRIPTION)))
                .body("amount", is(TEST_AMOUNT))
                .body("status", is(TEST_STATUS))
                .body("return_url", is(TEST_RETURN_URL))
                .body("payment_provider", is(TEST_PAYMENT_PROVIDER))
                .body("created_date", is(TEST_CREATED_DATE));

        response.body("_links.self.href", is(paymentLocationFor(TEST_CHARGE_ID)));
    }

    @Test
    public void getPayment_Returns401_WhenUnauthorised() {
        publicAuthMock.respondUnauthorised();

        getPaymentResponse(BEARER_TOKEN, TEST_CHARGE_ID)
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
        connectorMock.respondWithChargeEventsFound(GATEWAY_ACCOUNT_ID, TEST_CHARGE_ID, TEST_EVENTS);

        ValidatableResponse response = getPaymentEventsResponse(BEARER_TOKEN, TEST_CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(TEST_CHARGE_ID))
                .body("events", hasSize(1));

        List<Map<String,String>> list = response.extract().body().jsonPath().getList("events");
        assertEquals(list.get(0).get("payment_id"), TEST_CHARGE_ID);
        assertEquals(list.get(0).get("status"), TEST_STATUS);
        assertEquals(list.get(0).get("updated"), "2016-01-01 12:00:00");

        response.body("_links.self.href", is(paymentEventsLocationFor(TEST_CHARGE_ID)));
    }

    @Test
    public void getPaymentEvents_Returns401_WhenUnauthorised() {
        publicAuthMock.respondUnauthorised();

        getPaymentEventsResponse(BEARER_TOKEN, TEST_CHARGE_ID)
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

    private String cardDetailsUrlFor(String chargeId) {
        return "http://Frontend/charge/" + chargeId;
    }

    private static String paymentPayload(long amount, String returnUrl, String description, String reference) {
        return jsonStringBuilder()
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
                .get(String.format(PAYMENT_EVENTS_PATH, paymentId))
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
