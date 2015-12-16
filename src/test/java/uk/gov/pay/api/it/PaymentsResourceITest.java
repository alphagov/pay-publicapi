package uk.gov.pay.api.it;

import com.jayway.restassured.response.ValidatableResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.junit.MockServerRule;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.config.PublicApiConfig;
import uk.gov.pay.api.utils.ConnectorMockClient;
import uk.gov.pay.api.utils.PublicAuthMockClient;

import javax.ws.rs.core.HttpHeaders;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonStringBuilder;
import static uk.gov.pay.api.utils.LinksAssert.assertLink;

public class PaymentsResourceITest {
    private static final String TEST_CHARGE_ID = "ch_ab2341da231434l";
    private static final long TEST_AMOUNT = 20032123132120l;
    private static final String TEST_STATUS = "someState";
    private static final String TEST_RETURN_URL = "http://somewhere.over.the/rainbow/{paymentID}";
    private static final String GATEWAY_ACCOUNT_ID = "gw_32adf21bds3aac21";
    private static final String PAYMENTS_PATH = "/v1/payments/";
    private static final String BEARER_TOKEN = "TEST-BEARER-TOKEN";
    private static final String TEST_REFERENCE = "Some reference <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String TEST_DESCRIPTION = "Some description <script> alert('This is a ?{simple} XSS attack.')</script>";

    private static final String SUCCESS_PAYLOAD = paymentPayload(TEST_AMOUNT, TEST_RETURN_URL, TEST_DESCRIPTION, TEST_REFERENCE);

    @Rule
    public MockServerRule connectorMockRule = new MockServerRule(this);

    @Rule
    public MockServerRule publicAuthMockRule = new MockServerRule(this);

    private ConnectorMockClient connectorMock;
    private PublicAuthMockClient publicAuthMock;

    @Rule
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class
            , resourceFilePath("config/test-config.yaml")
            , config("connectorUrl", connectorBaseUrl())
            , config("publicAuthUrl", publicAuthBaseUrl()));

    private String connectorBaseUrl() {
        return "http://localhost:" + connectorMockRule.getHttpPort();
    }

    private String publicAuthBaseUrl() {
        return "http://localhost:" + publicAuthMockRule.getHttpPort() + "/v1/auth";
    }

    private String paymentLocationFor(String chargeId) {
        return "http://localhost:" + app.getLocalPort() + PAYMENTS_PATH + chargeId;
    }

    @Before
    public void setup() {
        connectorMock = new ConnectorMockClient(connectorMockRule.getHttpPort(), connectorBaseUrl());
        publicAuthMock = new PublicAuthMockClient(publicAuthMockRule.getHttpPort());
    }

    @Test
    public void createPayment() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenCreateCharge(TEST_AMOUNT, GATEWAY_ACCOUNT_ID, TEST_CHARGE_ID, TEST_STATUS, TEST_RETURN_URL, TEST_DESCRIPTION, TEST_REFERENCE);

        ValidatableResponse response = postPaymentResponse(BEARER_TOKEN, SUCCESS_PAYLOAD)
                .statusCode(201)
                .contentType(JSON)
                .body("payment_id", is(TEST_CHARGE_ID))
                .body("amount", is(TEST_AMOUNT))
                .body("reference", is(escapeHtml4(TEST_REFERENCE)))
                .body("description", is(escapeHtml4(TEST_DESCRIPTION)))
                .body("status", is(TEST_STATUS))
                .body("return_url", is(TEST_RETURN_URL));

        String paymentId = response.extract().path("payment_id");
        assertThat(paymentId, is(TEST_CHARGE_ID));

        String paymentUrl = paymentLocationFor(paymentId);

        response.header(HttpHeaders.LOCATION, is(paymentUrl));
        assertLink(response, paymentUrl, "self");
        assertLink(response, cardDetailsUrlFor(TEST_CHARGE_ID), "next_url");
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

        postPaymentResponse(BEARER_TOKEN, "{\"description\":\"\", \"reference\":null}")
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Field(s) missing: [description, amount, reference, return_url]"));
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
        connectorMock.respondWithChargeFound(TEST_AMOUNT, GATEWAY_ACCOUNT_ID, TEST_CHARGE_ID, TEST_STATUS, TEST_RETURN_URL, TEST_DESCRIPTION, TEST_REFERENCE);

        ValidatableResponse response = getPaymentResponse(BEARER_TOKEN, TEST_CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(TEST_CHARGE_ID))
                .body("reference", is(escapeHtml4(TEST_REFERENCE)))
                .body("description", is(escapeHtml4(TEST_DESCRIPTION)))
                .body("amount", is(TEST_AMOUNT))
                .body("status", is(TEST_STATUS))
                .body("return_url", is(TEST_RETURN_URL));

        assertLink(response, paymentLocationFor(TEST_CHARGE_ID), "self");
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
