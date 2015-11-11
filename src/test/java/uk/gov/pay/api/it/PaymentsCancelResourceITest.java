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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.is;
import static uk.gov.pay.api.utils.ConnectorMockClient.CONNECTOR_MOCK_CANCEL_PATH_SUFFIX;
import static uk.gov.pay.api.utils.ConnectorMockClient.CONNECTOR_MOCK_CHARGE_PATH;

public class PaymentsCancelResourceITest {
    private static final String TEST_CHARGE_ID = "ch_ab2341da231434";

    private static final String PAYMENTS_PATH = "/v1/payments/";
    private static final String CANCEL_PAYMENTS_PATH = PAYMENTS_PATH + "{paymentId}" + CONNECTOR_MOCK_CANCEL_PATH_SUFFIX;
    private static final String BEARER_TOKEN = "TEST_BEARER_TOKEN";
    private static final String GATEWAY_ACCOUNT_ID = "GATEWAY_ACCOUNT_ID";

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
            , config("publicAuthUrl", publicAuthBaseUrl())
            , config("connectorUrl", connectorMockChargeUrl()));

    private String connectorBaseUrl() {
        return "http://localhost:" + connectorMockRule.getHttpPort();
    }

    private String connectorMockChargeUrl() {
        return connectorBaseUrl() + CONNECTOR_MOCK_CHARGE_PATH;
    }

    private String publicAuthBaseUrl() {
        return "http://localhost:" + publicAuthMockRule.getHttpPort() + "/v1/auth";
    }

    @Before
    public void setup() {
        connectorMock = new ConnectorMockClient(connectorMockRule.getHttpPort(), connectorBaseUrl());
        publicAuthMock = new PublicAuthMockClient(publicAuthMockRule.getHttpPort());
    }

    @Test
    public void cancelPayment_Returns401_WhenUnauthorised() {
        publicAuthMock.respondUnauthorised();

        postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(401);
    }

    @Test
    public void successful_whenConnector_AllowsCancellation() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenCancelCharge(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID);

        postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(204);

        connectorMock.verifyCancelCharge(TEST_CHARGE_ID);
    }

    @Test
    public void cancelPayment_returns400_whenAccountIdIsMissing() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondBadRequest_WhenAccountIdIsMissing(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID ,"account_id is missing for cancellation");

        postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(400)
                .body("message", is("Cancellation of charge failed."));
    }

    @Test
    public void respondWithBadRequest_whenPaymentNotFound() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondChargeNotFound_WhenCancelCharge(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID, "some backend error message");

        postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Cancellation of charge failed."));
    }

    @Test
    public void respondWithBadRequest_whenConnector_DoesntAllowCancellation() {
        connectorMock.respondBadRequest_WhenCancelChargeNotAllowed(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID, "some other message");
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);

        postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Cancellation of charge failed."));
    }

    private ValidatableResponse postCancelPaymentResponse(String paymentId) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + BEARER_TOKEN)
                .post(CANCEL_PAYMENTS_PATH.replace("{paymentId}", paymentId))
                .then();
    }
}
