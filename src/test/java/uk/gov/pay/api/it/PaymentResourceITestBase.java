package uk.gov.pay.api.it;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Rule;
import org.mockserver.junit.MockServerRule;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.utils.ApiKeyGenerator;
import uk.gov.pay.api.utils.mocks.ConnectorDDMockClient;
import uk.gov.pay.api.utils.mocks.ConnectorMockClient;
import uk.gov.pay.api.utils.PublicAuthMockClient;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public abstract class PaymentResourceITestBase {
    //Must use same secret set int configured test-config.xml
    protected static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");
    protected static final String GATEWAY_ACCOUNT_ID = "GATEWAY_ACCOUNT_ID";
    protected static final String PAYMENTS_PATH = "/v1/payments/";

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

    protected ConnectorMockClient connectorMock;
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

    String paymentLocationFor(String chargeId) {
        return "http://publicapi.url" + PAYMENTS_PATH + chargeId;
    }

    protected String frontendUrlFor(TokenPaymentType paymentType) {
        return "http://frontend_"+paymentType.toString().toLowerCase()+"/charge/";
    }

    String paymentEventsLocationFor(String chargeId) {
        return paymentLocationFor(chargeId) + "/events";
    }

    String paymentRefundsLocationFor(String chargeId) {
        return paymentLocationFor(chargeId) + "/refunds";
    }

    String paymentRefundLocationFor(String chargeId, String refundId) {
        return "http://publicapi.url" + PAYMENTS_PATH + chargeId + "/refunds/" +refundId;
    }

    String paymentCancelLocationFor(String chargeId) {
        return paymentLocationFor(chargeId) + "/cancel";
    }

}
