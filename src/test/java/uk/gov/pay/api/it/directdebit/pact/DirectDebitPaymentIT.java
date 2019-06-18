package uk.gov.pay.api.it.directdebit.pact;

import au.com.dius.pact.consumer.PactVerification;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.restassured.response.ValidatableResponse;
import org.apache.http.client.fluent.Executor;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.utils.ApiKeyGenerator;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.core.HttpHeaders;
import java.time.ZonedDateTime;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.utils.Urls.directDebitPaymentLocationFor;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class DirectDebitPaymentIT {

    private static final int AMOUNT = 100;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final PaymentState CREATED = new PaymentState("created", false, null, null, null);
    private static final String MANDATE_ID = "test_mandate_id_xyz";
    private static final String REFERENCE = "a reference";
    private static final String DESCRIPTION = "a description";
    private static final String CREATED_DATE = ISO_INSTANT_MILLISECOND_PRECISION.format(ZonedDateTime.parse("2010-12-31T22:59:59.132012345Z"));

    //Must use same secret set int configured test-config.xml
    protected static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");
    protected static final String GATEWAY_ACCOUNT_ID = "GATEWAY_ACCOUNT_ID";
    protected static final String PAYMENTS_PATH = "/v1/directdebit/payments/";

    @Rule
    public PactProviderRule directDebitConnector = new PactProviderRule("direct-debit-connector", this);

    @Rule
    public PactProviderRule publicAuth = new PactProviderRule("publicauth", this);

    @Rule
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class,
            resourceFilePath("config/test-config.yaml"),
            config("connectorDDUrl", "http://localhost:" + directDebitConnector.getConfig().getPort()),
            config("publicAuthUrl", "http://localhost:" + publicAuth.getConfig().getPort() + "/v1/auth"));

    @Test
    @PactVerification({"direct-debit-connector", "publicauth"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-collect-payment"})
    @Pacts(pacts = {"publicapi-publicauth"}, publish = false)
    public void createPayment() {

        String publicApiBaseUrl = app.getConfiguration().getBaseUrl();
        String payload = new JsonStringBuilder()
                .add("amount", AMOUNT)
                .add("reference", REFERENCE)
                .add("description", DESCRIPTION)
                .add("mandate_id", MANDATE_ID)
                .build();

        postPaymentResponse(API_KEY, payload)
                .statusCode(201)
                .contentType(JSON)
                .header(HttpHeaders.LOCATION, is(directDebitPaymentLocationFor(publicApiBaseUrl, CHARGE_ID)))
                .body("payment_id", is(CHARGE_ID))
                .body("amount", is(AMOUNT))
                .body("reference", is(REFERENCE))
                .body("description", is(DESCRIPTION))
                .body("state.status", is(CREATED.getStatus()))
                .body("created_date", is(CREATED_DATE))
                .body("_links.self.href", is(directDebitPaymentLocationFor(publicApiBaseUrl, CHARGE_ID)))
                .body("_links.self.method", is("GET"));
    }

    private ValidatableResponse postPaymentResponse(String bearerToken, String payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .post(PAYMENTS_PATH)
                .then();
    }

    // Close idle connections - see https://github.com/DiUS/pact-jvm/issues/342
    @After
    public void teardown() {
        Executor.closeIdleConnections();
    }
}
