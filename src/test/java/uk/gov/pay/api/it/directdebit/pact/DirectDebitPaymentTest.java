package uk.gov.pay.api.it.directdebit.pact;

import au.com.dius.pact.consumer.PactVerification;
import com.jayway.jsonassert.JsonAssert;
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
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.core.HttpHeaders;
import java.time.ZonedDateTime;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.utils.Payloads.aSuccessfulPaymentPayload;
import static uk.gov.pay.api.utils.Urls.directDebitFrontendSecureUrl;
import static uk.gov.pay.api.utils.Urls.paymentLocationFor;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class DirectDebitPaymentTest {

    private static final int AMOUNT = 100;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final String CHARGE_TOKEN_ID = "ebf23f8c-6a9d-4f7d-afd5-bcc7b1b6a0e2";
    private static final PaymentState STARTED = new PaymentState("started", false, null, null);
    private static final String RETURN_URL = "https://somewhere.gov.uk/rainbow/1";
    private static final String REFERENCE = "a reference";
    private static final String EMAIL = "alice.111@mail.fake";
    private static final String DESCRIPTION = "a description";
    private static final String CREATED_DATE = ISO_INSTANT_MILLISECOND_PRECISION.format(ZonedDateTime.parse("2010-12-31T22:59:59.132012345Z"));
    private static final String SUCCESS_PAYLOAD = aSuccessfulPaymentPayload(AMOUNT, RETURN_URL, DESCRIPTION, REFERENCE, EMAIL);

    //Must use same secret set int configured test-config.xml
    protected static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");
    protected static final String GATEWAY_ACCOUNT_ID = "GATEWAY_ACCOUNT_ID";
    protected static final String PAYMENTS_PATH = "/v1/payments/";
    
    @Rule
    public PactProviderRule directDebitConnector = new PactProviderRule("direct-debit-connector", this);
    
    @Rule
    public PactProviderRule publicAuth = new PactProviderRule("publicauth", this);
    
    @Rule 
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class,
            resourceFilePath("config/test-config.yaml"),
            config("connectorUrl", "http://localhost"),
            config("connectorDDUrl", "http://localhost:" + directDebitConnector.getConfig().getPort()),
            config("publicAuthUrl", "http://localhost:" + publicAuth.getConfig().getPort() + "/v1/auth"));
    
    @Test
    @PactVerification({"direct-debit-connector", "publicauth"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-create-payment"})
    @Pacts(pacts = {"publicapi-publicauth"}, publish = false)
    public void createPayment() {
        
        String publicApiBaseUrl = app.getConfiguration().getBaseUrl();
        
        String responseBody = postPaymentResponse(API_KEY, SUCCESS_PAYLOAD)
                .statusCode(201)
                .contentType(JSON)
                .header(HttpHeaders.LOCATION, is(paymentLocationFor(publicApiBaseUrl, CHARGE_ID)))
                .body("payment_id", is(CHARGE_ID))
                .body("amount", is(AMOUNT))
                .body("reference", is(REFERENCE))
                .body("description", is(DESCRIPTION))
                .body("state.status", is(STARTED.getStatus()))
                .body("return_url", is(RETURN_URL))
                .body("created_date", is(CREATED_DATE))
                .body("_links.self.href", is(paymentLocationFor(publicApiBaseUrl, CHARGE_ID)))
                .body("_links.self.method", is("GET"))
                .body("_links.next_url.href", is(directDebitFrontendSecureUrl() + CHARGE_TOKEN_ID))
                .body("_links.next_url.method", is("GET"))
                .body("_links.next_url_post.href", is(directDebitFrontendSecureUrl()))
                .body("_links.next_url_post.method", is("POST"))
                .body("_links.next_url_post.type", is("application/x-www-form-urlencoded"))
                .body("_links.next_url_post.params.chargeTokenId", is(CHARGE_TOKEN_ID))
                .body("card_brand", is(nullValue()))
                .body("refund_summary", is(nullValue()))
                .body("_links.cancel", is(nullValue()))
                .body("_links.events", is(nullValue()))
                .body("_links.refunds", is(nullValue()))
                .extract().body().asString();

        JsonAssert.with(responseBody)
                .assertNotDefined("_links.self.type")
                .assertNotDefined("_links.self.params")
                .assertNotDefined("_links.next_url.type")
                .assertNotDefined("_links.next_url.params");
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
