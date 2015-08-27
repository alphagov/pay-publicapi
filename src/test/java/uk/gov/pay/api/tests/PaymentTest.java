package uk.gov.pay.api.tests;

import com.jayway.restassured.response.ValidatableResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.config.PublicApiConfig;
import uk.gov.pay.api.utils.ConnectorMockClient;

import javax.ws.rs.core.HttpHeaders;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.api.utils.ConnectorMockClient.CONNECTOR_MOCK_CHARGE_PATH;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonString;
import static uk.gov.pay.api.utils.LinksAssert.assertSelfLink;

public class PaymentTest {
    private static final String TEST_CHARGE_ID = "ch_ab2341da231434l";
    private static final long TEST_AMOUNT = 20032123132120l;
    private static final String GATEWAY_ACCOUNT_ID = "gw_32adf21bds3aac21";

    @Rule
    public MockServerRule connectorMockRule = new MockServerRule(this);

    private MockServerClient mockServerClient = null;
    private ConnectorMockClient connectorMock;

    @Rule
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class
            , resourceFilePath("config/test-config.yaml")
            , config("connectorUrl", connectorMockChargeUrl()));

    private String connectorMockChargeUrl() {
        return baseUrl() + CONNECTOR_MOCK_CHARGE_PATH;
    }

    private String baseUrl() {
        return "http://localhost:" + connectorMockRule.getHttpPort();
    }

    @Before
    public void setup() {
        connectorMock = new ConnectorMockClient(mockServerClient, baseUrl());
    }

    @Test
    public void createPayment() {
        connectorMock.respondOk_whenCreateCharge(TEST_AMOUNT, GATEWAY_ACCOUNT_ID, TEST_CHARGE_ID);

        ValidatableResponse response = postPaymentResponse(paymentPayload(TEST_AMOUNT, GATEWAY_ACCOUNT_ID))
                .statusCode(201)
                .contentType(JSON)
                .log().all()
                .body("amount", is(TEST_AMOUNT));

        String paymentId = response.extract().path("payment_id");
        assertThat(paymentId, is(TEST_CHARGE_ID));

        String paymentUrl = "http://localhost:" + app.getLocalPort() + "/payments/" + paymentId;

        response.header(HttpHeaders.LOCATION, is(paymentUrl));
        assertSelfLink(response, paymentUrl);
        connectorMock.verifyCreateCharge(TEST_AMOUNT, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void createPayment_responseWith4xx_whenInvalidGatewayAccount() {
        String invalidGatewayAccountId = "ada2dfa323";
        String errorMessage = "something went wrong";
        connectorMock.respondUnknownGateway_whenCreateCharge(TEST_AMOUNT, invalidGatewayAccountId, errorMessage);

        postPaymentResponse(paymentPayload(TEST_AMOUNT, invalidGatewayAccountId))
                .statusCode(400)
                .contentType(JSON)
                .body("message", is(errorMessage));

        connectorMock.verifyCreateCharge(TEST_AMOUNT, invalidGatewayAccountId);
    }

    @Test
    public void createPayment_responseWith4xx_whenFieldsMissing() {
        postPaymentResponse("{}")
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Field(s) missing: [amount, gateway_account]"));
    }

    @Test
    public void createPayment_responseWith4xx_whenConnectorResponseEmpty() {
        connectorMock.respondOk_withEmptyBody(TEST_AMOUNT, GATEWAY_ACCOUNT_ID, TEST_CHARGE_ID);

        postPaymentResponse(paymentPayload(TEST_AMOUNT, GATEWAY_ACCOUNT_ID))
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Connector response contains no payload!"));
    }

    @Test
    public void getPayment_ReturnsPayment() {
        connectorMock.respondWithChargeFound(TEST_AMOUNT, TEST_CHARGE_ID);

        ValidatableResponse response = getPaymentResponse(TEST_CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(TEST_CHARGE_ID))
                .body("amount", is(TEST_AMOUNT));

        String paymentUrl = "http://localhost:" + app.getLocalPort() + "/payments/" + TEST_CHARGE_ID;
        assertSelfLink(response, paymentUrl);
    }

    @Test
    public void getPayment_InvalidPaymentId() {
        String invalidPaymentId = "ds2af2afd3df112";
        String errorMessage = "backend-error-message";
        connectorMock.respondChargeNotFound(invalidPaymentId, errorMessage);

        getPaymentResponse(invalidPaymentId)
                .statusCode(404)
                .contentType(JSON)
                .body("message", is(errorMessage));
    }

    private String paymentPayload(long amount, String gatewayAccountId) {
        return jsonString("amount", amount, "gateway_account", gatewayAccountId);
    }

    private ValidatableResponse getPaymentResponse(String paymentId) {
        return given().port(app.getLocalPort())
                .get("/payments/" + paymentId)
                .then();
    }

    private ValidatableResponse postPaymentResponse(String payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .contentType(JSON)
                .post("/payments")
                .then();
    }
}
