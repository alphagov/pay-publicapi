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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.api.utils.ConnectorMockClient.CONNECTOR_MOCK_CHARGE_PATH;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonString;
import static uk.gov.pay.api.utils.LinksAssert.assertSelfLink;

public class PaymentTest {
    private static final long TEST_CHARGE_ID = 2341231434l;
    private static final long GATEWAY_ACCOUNT_ID = 322121;

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
    public void createCharge() {
        int amount = 12345;
        connectorMock.respondOk_whenCreateCharge(amount, GATEWAY_ACCOUNT_ID, TEST_CHARGE_ID);

        ValidatableResponse response = postPaymentResponse(chargePayload(amount, GATEWAY_ACCOUNT_ID))
                .statusCode(201)
                .contentType(JSON)
                .log().all()
                .body("amount", equalTo(amount));

        Long paymentId = response.extract().path("payment_id");
        assertThat(paymentId, is(TEST_CHARGE_ID));

        String paymentUrl = "http://localhost:" + app.getLocalPort() + "/payments/" + paymentId;

        response.header(HttpHeaders.LOCATION, is(paymentUrl));
        assertSelfLink(response, paymentUrl);
        connectorMock.verifyCreateCharge(amount, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void createCharge_responseWith4xx_whenInvalidGatewayAccount() {
        long amount = 12345;
        int invalidGatewayAccountId = 2323;
        String errorMessage = "something went wrong";
        connectorMock.respondUnknownGateway_whenCreateCharge(amount, invalidGatewayAccountId, errorMessage);

        postPaymentResponse(chargePayload(amount, invalidGatewayAccountId))
                .statusCode(400)
                .contentType(JSON)
                .body("message", is(errorMessage));

        connectorMock.verifyCreateCharge(amount, invalidGatewayAccountId);
    }

    @Test
    public void createCharge_responseWith4xx_whenFieldsMissing() {
        postPaymentResponse("{}")
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Field(s) missing: [amount, gateway_account]"));
    }

    @Test
    public void createCharge_responseWith4xx_whenConnectorResponseEmpty() {
        long amount = 12345;
        connectorMock.respondOk_withEmptyBody(amount, GATEWAY_ACCOUNT_ID, TEST_CHARGE_ID);

        postPaymentResponse(chargePayload(amount, GATEWAY_ACCOUNT_ID))
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Connector response contains no payload!"));
    }

    @Test
    public void getCharge_ReturnsCharge() {
        int amount = 12345;
        connectorMock.respondWithChargeFound(amount, TEST_CHARGE_ID);

        ValidatableResponse response = getPaymentResponse(TEST_CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(TEST_CHARGE_ID))
                .body("amount", is(amount));

        String paymentUrl = "http://localhost:" + app.getLocalPort() + "/payments/" + TEST_CHARGE_ID;
        assertSelfLink(response, paymentUrl);
    }

    @Test
    public void getCharge_InvalidChargeId() {
        long paymentId = 23112;
        String errorMessage = "backend-error-message";
        connectorMock.respondChargeNotFound(paymentId, errorMessage);

        getPaymentResponse(paymentId)
                .statusCode(404)
                .contentType(JSON)
                .body("message", is(errorMessage));
    }

    private ValidatableResponse postPaymentResponse(String payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .contentType(JSON)
                .post("/payments")
                .then();
    }

    private String chargePayload(long amount, long gatewayAccountId) {
        return jsonString("amount", amount, "gateway_account", gatewayAccountId);
    }

    private ValidatableResponse getPaymentResponse(long paymentId) {
        return given().port(app.getLocalPort())
                .get("/payments/" + paymentId)
                .then();
    }
}
