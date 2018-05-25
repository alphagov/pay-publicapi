package uk.gov.pay.api.it;

import com.jayway.restassured.response.ValidatableResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Rule;
import org.mockserver.junit.MockServerRule;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.utils.ApiKeyGenerator;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.api.utils.PublicAuthMockClient;
import uk.gov.pay.api.utils.mocks.ConnectorDDMockClient;
import uk.gov.pay.api.utils.mocks.ConnectorMockClient;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

public abstract class PaymentResourceITestBase {
    //Must use same secret set int configured test-config.xml
    protected static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");
    protected static final String GATEWAY_ACCOUNT_ID = "GATEWAY_ACCOUNT_ID";
    static final String PAYMENTS_PATH = "/v1/payments/";

    @Rule
    public MockServerRule connectorMockRule = new MockServerRule(this);

    @Rule
    public MockServerRule connectorDDMockRule = new MockServerRule(this);

    @Rule
    public MockServerRule publicAuthMockRule = new MockServerRule(this);

    @Rule
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class
            , resourceFilePath("config/test-config.yaml")
            , config("connectorUrl", connectorBaseUrl())
            , config("connectorDDUrl", connectorDDBaseUrl())
            , config("publicAuthUrl", publicAuthBaseUrl()));

    ConnectorMockClient connectorMock;
    protected ConnectorDDMockClient connectorDDMock;
    protected PublicAuthMockClient publicAuthMock;

    @Before
    public void setup() {
        connectorMock = new ConnectorMockClient(connectorMockRule.getPort(), connectorBaseUrl());
        connectorDDMock = new ConnectorDDMockClient(connectorDDMockRule.getPort(), connectorDDBaseUrl());
        publicAuthMock = new PublicAuthMockClient(publicAuthMockRule.getPort());
    }

    private String connectorBaseUrl() {
        return "http://localhost:" + connectorMockRule.getPort();
    }

    private String connectorDDBaseUrl() {
        return "http://localhost:" + connectorDDMockRule.getPort();
    }

    private String publicAuthBaseUrl() {
        return "http://localhost:" + publicAuthMockRule.getPort() + "/v1/auth";
    }

    protected String paymentLocationFor(String chargeId) {
        return "http://publicapi.url" + PAYMENTS_PATH + chargeId;
    }

    protected String frontendUrlFor(TokenPaymentType paymentType) {
        return "http://frontend_" + paymentType.toString().toLowerCase() + "/charge/";
    }

    protected ValidatableResponse getPaymentResponse(String bearerToken, String paymentId) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .get(PAYMENTS_PATH + paymentId)
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

    protected static String paymentPayload(long amount, String returnUrl, String description, String reference, String email) {
        return new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("email", email)
                .add("description", description)
                .add("return_url", returnUrl)
                .build();
    }

    String paymentEventsLocationFor(String chargeId) {
        return paymentLocationFor(chargeId) + "/events";
    }

    String paymentRefundsLocationFor(String chargeId) {
        return paymentLocationFor(chargeId) + "/refunds";
    }

    String paymentRefundLocationFor(String chargeId, String refundId) {
        return "http://publicapi.url" + PAYMENTS_PATH + chargeId + "/refunds/" + refundId;
    }

    String paymentCancelLocationFor(String chargeId) {
        return paymentLocationFor(chargeId) + "/cancel";
    }

}
