package uk.gov.pay.api.tests;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.config.PublicApiConfig;

import static com.jayway.restassured.RestAssured.given;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.hamcrest.Matchers.is;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.verify.VerificationTimes.once;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonStringBuilder;

public class PaymentTest {
    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    @SuppressWarnings("unused")
    private MockServerClient mockServerClient;

    @Rule
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class
            , resourceFilePath("config/test-config.yaml")
            , config("connectorUrl", buildMockServerUrl()));

    private String buildMockServerUrl() {
        return "http://localhost:" + mockServerRule.getHttpPort() + "/payment";
    }

    @Test
    public void newPaymentCreatesNewChargeOnConnector() {
        String amountPayload = jsonStringBuilder()
                .add("amount", 1234)
                .build();

        mockServerClient
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/payment")
                                .withBody(json(amountPayload)),
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(
                                        jsonStringBuilder()
                                                .add("charge_id", "TEST_PAY_ID")
                                                .build()
                                )
                );

        given().port(app.getLocalPort())
                .contentType("application/json")
                .body(amountPayload)
                .post("/payments")
                .then()
                .statusCode(200)
                .body("pay_id", is("TEST_PAY_ID"));

        mockServerClient.verify(
                request()
                        .withMethod("POST")
                        .withPath("/payment")
                        .withBody(json(amountPayload)),
                once()
        );
    }
}
