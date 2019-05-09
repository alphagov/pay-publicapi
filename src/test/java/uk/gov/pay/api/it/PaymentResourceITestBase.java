package uk.gov.pay.api.it;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.spotify.docker.client.exceptions.DockerException;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.it.rule.RedisDockerRule;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.utils.ApiKeyGenerator;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.mockserver.socket.PortFactory.findFreePort;
import static uk.gov.pay.api.utils.Urls.paymentLocationFor;

public abstract class PaymentResourceITestBase {
    //Must use same secret set in test-config.xml's apiKeyHmacSecret
    protected static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");
    protected static final String GATEWAY_ACCOUNT_ID = "GATEWAY_ACCOUNT_ID";
    protected static final String PAYMENTS_PATH = "/v1/payments/";

    @ClassRule
    public static RedisDockerRule redisDockerRule;

    static {
        try {
            redisDockerRule = new RedisDockerRule();
        } catch (DockerException e) {
            e.printStackTrace();
        }
    }

    private static final int CONNECTOR_PORT = findFreePort();
    private static final int CONNECTOR_DD_PORT = findFreePort();
    private static final int PUBLIC_AUTH_PORT = findFreePort();
    
    @Rule
    public WireMockClassRule connectorMock = new WireMockClassRule(CONNECTOR_PORT);

    @Rule
    public WireMockClassRule connectorDDMock = new WireMockClassRule(CONNECTOR_DD_PORT);

    @Rule
    public WireMockClassRule publicAuthMock = new WireMockClassRule(PUBLIC_AUTH_PORT);
    
    @Rule
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class
            , resourceFilePath("config/test-config.yaml")
            , config("connectorUrl", "http://localhost:" + CONNECTOR_PORT)
            , config("connectorDDUrl", "http://localhost:" + CONNECTOR_DD_PORT)
            , config("publicAuthUrl", "http://localhost:" + PUBLIC_AUTH_PORT + "/v1/auth")
            , config("redis.endpoint", redisDockerRule.getRedisUrl())
    );

    protected PublicApiConfig configuration;

    @Before
    public void setup() {
        configuration = app.getConfiguration();
    }

    String frontendUrlFor(TokenPaymentType paymentType) {
        return "http://frontend_" + paymentType.toString().toLowerCase() + "/charge/";
    }
    
    String paymentEventsLocationFor(String chargeId) {
        return paymentLocationFor(configuration.getBaseUrl(), chargeId) + "/events";
    }

    String paymentRefundsLocationFor(String chargeId) {
        return paymentLocationFor(configuration.getBaseUrl(), chargeId) + "/refunds";
    }

    String paymentRefundLocationFor(String chargeId, String refundId) {
        return "http://publicapi.url" + PAYMENTS_PATH + chargeId + "/refunds/" + refundId;
    }

    String paymentCancelLocationFor(String chargeId) {
        return paymentLocationFor(configuration.getBaseUrl(), chargeId) + "/cancel";
    }

}
