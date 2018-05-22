package uk.gov.pay.api.it;

import au.com.dius.pact.consumer.PactVerification;
import com.jayway.jsonassert.JsonAssert;
import com.jayway.restassured.response.ValidatableResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.http.client.fluent.Executor;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.pact.PactProviderRule;
import uk.gov.pay.api.pact.Pacts;
import uk.gov.pay.api.utils.ApiKeyGenerator;
import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.ws.rs.core.HttpHeaders;
import java.time.ZonedDateTime;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;

public class DirectDebitPaymentTest {

    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final int AMOUNT = 100;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final PaymentState CREATED = new PaymentState("created", false, null, null);
    private static final String PAYMENT_PROVIDER = "Sandbox";
    private static final String RETURN_URL = "https://somewhere.gov.uk/rainbow/1";
    private static final String REFERENCE = "a reference";
    private static final String EMAIL = "alice.111@mail.fake";
    private static final String DESCRIPTION = "a description";
    private static final String CREATED_DATE = DateTimeUtils.toUTCDateString(TIMESTAMP);
    private static final String SUCCESS_PAYLOAD = paymentPayload(AMOUNT, RETURN_URL, DESCRIPTION, REFERENCE, EMAIL);

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
    @Pacts(pacts = {"publicapi-publicauth", "publicapi-direct-debit-connector"})
    public void createDirectDebitPayment() {
        String responseBody = postPaymentResponse(API_KEY, SUCCESS_PAYLOAD)
                .statusCode(201)
                .contentType(JSON)
                .header(HttpHeaders.LOCATION, is(paymentLocationFor(CHARGE_ID)))
                .body("payment_id", is(CHARGE_ID))
                .body("amount", is(AMOUNT))
                .body("reference", is(REFERENCE))
                .body("description", is(DESCRIPTION))
                .body("state.status", is(CREATED.getStatus()))
                .body("return_url", is(RETURN_URL))
                .body("email", is(EMAIL))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("created_date", is(CREATED_DATE))
                .body("_links.self.href", is(paymentLocationFor(CHARGE_ID)))
                .body("_links.self.method", is("GET"))
                .body("_links.next_url.href", is(frontendUrlFor(DIRECT_DEBIT) + CHARGE_TOKEN_ID))
                .body("_links.next_url.method", is("GET"))
                .body("_links.next_url_post.href", is(frontendUrlFor(DIRECT_DEBIT)))
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

    private static String paymentPayload(long amount, String returnUrl, String description, String reference, String email) {
        return new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("email", email)
                .add("description", description)
                .add("return_url", returnUrl)
                .build();
    }

    String paymentLocationFor(String chargeId) {
        return "http://publicapi.url" + PAYMENTS_PATH + chargeId;
    }

    String frontendUrlFor(TokenPaymentType paymentType) {
        return "http://frontend_"+paymentType.toString().toLowerCase()+"/charge/";
    }

    // Close idle connections - see https://github.com/DiUS/pact-jvm/issues/342
    @After 
    public void teardown() {
        Executor.closeIdleConnections();
    }
}
