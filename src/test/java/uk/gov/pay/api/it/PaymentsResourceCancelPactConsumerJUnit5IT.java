package uk.gov.pay.api.it;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.testing.DropwizardTestSupport;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.utils.ApiKeyGenerator;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.Matchers.is;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;

@PactConsumerTest
@PactTestFor(providerName = "connector", pactVersion = PactSpecVersion.V3)
class PaymentsResourceCancelPactConsumerJUnit5IT {

    private static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");

    @RegisterExtension
    private static final WireMockExtension publicAuthServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();
    private DropwizardTestSupport<PublicApiConfig> app;

    @BeforeEach
    void setUp(MockServer mockServer) throws Exception {
        stubPublicAuthV1ApiAuth(new Account("123456", CARD, "a-token-link"));
        app = new DropwizardTestSupport<>(
                PublicApi.class,
                resourceFilePath("config/test-config.yaml"),
                config("connectorUrl", mockServer.getUrl()),
                config("publicAuthUrl", publicAuthServer.baseUrl() + "/v1/api/auth")
        );
        app.before();
    }

    @AfterEach
    void tearDown() {
        app.after();
    }

    private static void stubPublicAuthV1ApiAuth(Account account) throws JsonProcessingException {
        var entity = ImmutableMap.of("account_id", account.accountId(), "token_type", account.paymentType().name());
        var json = new ObjectMapper().writeValueAsString(entity);
        publicAuthServer.stubFor(get(urlEqualTo("/v1/api/auth"))
                .withHeader(AUTHORIZATION, equalTo("Bearer " + API_KEY))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", APPLICATION_JSON)
                        .withBody(json)));
    }

    @Pact(consumer = "publicapi", provider = "connector")
    private RequestResponsePact cancelAlreadyCancelledPayment(PactDslWithProvider builder) {
        return builder.given("a canceled charge exists",
                        Map.of(
                                "gateway_account_id", "123456",
                                "charge_id", "charge8133029783750964630"))
                .uponReceiving("cancel an already canceled charge")
                .method("POST")
                .pathFromProviderState(
                        "/v1/api/accounts/${gateway_account_id}/charges/${charge_id}/cancel",
                        "/v1/api/accounts/123456/charges/charge8133029783750964630/cancel")
                .willRespondWith()
                .status(400)
                .toPact();
    }

    @Test
    void cancelAPaymentThatIsAlreadyCanceled() {
        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post("/v1/payments/{paymentId}/cancel", "charge8133029783750964630")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("code", is("P0501"))
                .body("description", is("Cancellation of payment failed"));
    }
}
