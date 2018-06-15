package uk.gov.pay.api.it.pact.card;

import au.com.dius.pact.consumer.PactVerification;
import com.jayway.restassured.response.ValidatableResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.http.client.fluent.Executor;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.it.pact.PaymentBaseTest;
import uk.gov.pay.api.pact.PactProviderRule;
import uk.gov.pay.api.pact.Pacts;
import uk.gov.pay.api.utils.ApiKeyGenerator;
import uk.gov.pay.api.utils.DateTimeUtils;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;

public class CardPaymentTest extends PaymentBaseTest {
    private static final String PAYMENT_ID = "some_test_id";
    private static final String CAPTURE_SUBMIT_TIME = DateTimeUtils.toUTCDateString(TIMESTAMP.plusMinutes(1L));
    private static final String CAPTURED_DATE = TIMESTAMP.plusMinutes(5L).toLocalDate().toString();

    @Rule
    public PactProviderRule connector = new PactProviderRule("connector", this);
    
    @Rule
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class,
            resourceFilePath("config/test-config.yaml"),
            config("connectorUrl", "http://localhost:" + connector.getConfig().getPort()),
            config("publicAuthUrl", "http://localhost:" + publicAuth.getConfig().getPort() + "/v1/auth"));

    @Test
    @PactVerification({"connector", "publicauth"})
    @Pacts(pacts = {"publicapi-publicauth-card", "publicapi-connector"})
    public void getPayment() {
        String bearerToken = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_CARD", "qwer9yuhgf");

        getPaymentResponse(bearerToken)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(PAYMENT_ID))
                .body("amount", is(AMOUNT))
                .body("reference", is(REFERENCE))
                .body("gateway_transaction_id", is(nullValue()))
                .body("description", is(DESCRIPTION))
                .body("state.status", is("success"))
                .body("state.finished", is(true))
                .body("return_url", is(RETURN_URL))
                .body("email", is(EMAIL))
                .body("payment_provider", is(SANDBOX_PAYMENT_PROVIDER))
                .body("created_date", is(CREATED_DATE))
                .body("refund_summary.status", is("available"))
                .body("refund_summary.amount_available", is(100))
                .body("refund_summary.amount_submitted", is(0))
                .body("settlement_summary.capture_submit_time", is(CAPTURE_SUBMIT_TIME))
                .body("settlement_summary.captured_date", is(CAPTURED_DATE))
                .body("card_details.last_digits_card_number", is("4242"))
                .body("card_details.cardholder_name", is("Test"))
                .body("card_details.expiry_date", is("01/21"))
                .body("card_details.billing_address.line1", is("Test line 1"))
                .body("card_details.billing_address.line2", is("Test line 2"))
                .body("card_details.billing_address.postcode", is("EC11AA"))
                .body("card_details.billing_address.city", is("London"))
                .body("card_details.billing_address.county", is(nullValue()))
                .body("card_details.billing_address.city", is("London"))
                .body("card_details.billing_address.country", is("GB"))
                .body("card_details.card_brand", is("Visa"))
                .body("card_brand", is("Visa"))
                .body("_links.self.method", is("GET"))
                .body("_links.self.href", is(paymentLocationFor(PAYMENT_ID)))
                .body("_links.refunds.method", is("GET"))
                .body("_links.refunds.href", is(paymentLocationFor(PAYMENT_ID) + "/refunds"))
                .extract().body();
    }

    private ValidatableResponse getPaymentResponse(String bearerToken) {
        return given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .get("/v1/payments/" + PAYMENT_ID)
                .then();
    }

    // Close idle connections - see https://github.com/DiUS/pact-jvm/issues/342
    @After
    public void teardown() {
        Executor.closeIdleConnections();
    }
}
