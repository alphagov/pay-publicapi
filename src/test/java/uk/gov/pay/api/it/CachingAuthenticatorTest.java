package uk.gov.pay.api.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.socket.PortFactory;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.utils.ApiKeyGenerator;
import uk.gov.pay.api.utils.JsonStringBuilder;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class CachingAuthenticatorTest {
    
    private String accountId = "123";
    private String bearerToken = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");

    private int publicAuthRulePort = PortFactory.findFreePort();
    private int connectorRulePort = PortFactory.findFreePort();
    
    @Rule
    public WireMockRule publicAuthRule = new WireMockRule(publicAuthRulePort);

    @Rule
    public WireMockRule connectorRule = new WireMockRule(connectorRulePort);
    
    @Rule
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class, 
            resourceFilePath("config/test-config.yaml"), 
            config("publicAuthUrl", "http://localhost:" + publicAuthRulePort + "/v1/api/auth"),
            config("connectorUrl", "http://localhost:" + connectorRulePort));

    @Before
    public void setup() throws Exception {
        setUpMockForPublicAuth();
        setUpMockForConnector();
    }
    
    @After
    public void cleanup() {
        publicAuthRule.resetRequests();
    }
    
    @Test
    public void testAuthenticationRequestsAreCached() throws Exception {
        makeRequest();
        Thread.sleep(1000); //pause for 1 second as there's a rate limit of 1 request per second
        makeRequest();

        publicAuthRule.verify(1, getRequestedFor(urlEqualTo("/v1/api/auth")));
    }
    
    @Test
    public void testAuthenticationCacheExpires() throws Exception {
        makeRequest();
        Thread.sleep(3000); //expireAfterWrite is set to 3seconds in test-config.yaml
        makeRequest();

        publicAuthRule.verify(2, getRequestedFor(urlEqualTo("/v1/api/auth")));
    }

    private void makeRequest() {
        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .get("/v1/payments/paymentId")
                .then()
                .statusCode(200)
                .contentType(JSON);
    }

    private void setUpMockForConnector() {
        connectorRule.stubFor(get(urlEqualTo(format("/v1/api/accounts/%s/charges/paymentId", accountId)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(aPayment())));
    }

    private void setUpMockForPublicAuth() throws Exception {
        Map<String, String> entity = ImmutableMap.of("account_id", accountId, "token_type", "CARD");
        String json = new ObjectMapper().writeValueAsString(entity);
        publicAuthRule.stubFor(get(urlEqualTo("/v1/api/auth"))
                .withHeader(AUTHORIZATION, equalTo("Bearer " + bearerToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(json)));
    }
    
    private String aPayment() {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder()
                .add("charge_id", "chargeId")
                .add("amount", 100)
                .add("reference", "ref 12")
                .add("state", new PaymentState("created", false, null, null))
                .add("email", "test@example.com")
                .add("description", "description")
                .add("return_url", "http://example.com")
                .add("payment_provider", "sandbox")
                .add("card_brand", "VISA")
                .add("created_date", "2018-07-25T13:12:00");
        return jsonStringBuilder.build();
    }
}
