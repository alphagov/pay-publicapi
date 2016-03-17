package uk.gov.pay.api.it;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Rule;
import org.mockserver.junit.MockServerRule;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.config.PublicApiConfig;
import uk.gov.pay.api.utils.ConnectorMockClient;
import uk.gov.pay.api.utils.PublicAuthMockClient;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public abstract class PaymentResourceITestBase {

    protected static final String BEARER_TOKEN = "TEST_BEARER_TOKEN";
    protected static final String GATEWAY_ACCOUNT_ID = "GATEWAY_ACCOUNT_ID";
    protected static final String PAYMENTS_PATH = "/v1/payments/";

    @Rule
    public MockServerRule connectorMockRule = new MockServerRule(this);

    @Rule
    public MockServerRule publicAuthMockRule = new MockServerRule(this);

    @Rule
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class
            , resourceFilePath("config/test-config.yaml")
            , config("connectorUrl", connectorBaseUrl())
            , config("publicAuthUrl", publicAuthBaseUrl()));

    protected ConnectorMockClient connectorMock;
    protected PublicAuthMockClient publicAuthMock;

    @Before
    public void setup() {
        connectorMock = new ConnectorMockClient(connectorMockRule.getHttpPort(), connectorBaseUrl());
        publicAuthMock = new PublicAuthMockClient(publicAuthMockRule.getHttpPort());
    }

    private String connectorBaseUrl() {
        return "http://localhost:" + connectorMockRule.getHttpPort();
    }

    private String publicAuthBaseUrl() {
        return "http://localhost:" + publicAuthMockRule.getHttpPort() + "/v1/auth";
    }
}
