package uk.gov.pay.api.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.utils.ApiKeyGenerator;
import uk.gov.pay.api.utils.JsonStringBuilder;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.lang.String.format;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;

class CachingAuthenticatorIT {

    private final String accountId = "123";
    private final String bearerToken = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");

    @RegisterExtension
    private static final WireMockExtension publicAuthServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @RegisterExtension
    private static final WireMockExtension connectorServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private static DropwizardTestSupport<PublicApiConfig> app;

    @BeforeEach
    void setup() throws Exception {
        setUpMockForPublicAuth();
        setUpMockForConnector();
        app = new DropwizardTestSupport<>(
                PublicApi.class,
                resourceFilePath("config/test-config.yaml"),
                config("publicAuthUrl", publicAuthServer.baseUrl() + "/v1/api/auth"),
                config("authenticationCachePolicy", "expireAfterWrite=3s"),
                config("rateLimiter.noOfReqPerNode", "4"),
                config("connectorUrl", connectorServer.baseUrl())
        );
        app.before();
    }

    @AfterEach
    void stopApp() {
        app.after();
    }

    @Test
    void testAuthenticationRequestsAreCached() {
        makeRequest();
        makeRequest();

        publicAuthServer.verify(1, getRequestedFor(urlEqualTo("/v1/api/auth")));
    }

    @Test
    void testAuthenticationCacheExpires() throws Exception {
        makeRequest();
        Thread.sleep(3000); //expireAfterWrite is set to 3 seconds in app config
        makeRequest();

        publicAuthServer.verify(2, getRequestedFor(urlEqualTo("/v1/api/auth")));
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
        connectorServer.stubFor(get(urlEqualTo(format("/v1/api/accounts/%s/charges/paymentId", accountId)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(aPayment())));
    }

    private void setUpMockForPublicAuth() throws JsonProcessingException {
        String tokenLink = "some-token-link";
        Account account = new Account(accountId, CARD, tokenLink);
        Map<String, String> entity = ImmutableMap.of("account_id", account.accountId(), "token_type", account.paymentType().name());
        String json = new ObjectMapper().writeValueAsString(entity);
        publicAuthServer.stubFor(get(urlEqualTo("/v1/api/auth"))
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
                .add("language", "en")
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
