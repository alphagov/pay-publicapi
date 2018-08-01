package uk.gov.pay.api.it;

import au.com.dius.pact.consumer.PactVerification;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.utils.ApiKeyGenerator;
import uk.gov.pay.commons.testing.pact.consumers.PactProviderRule;
import uk.gov.pay.commons.testing.pact.consumers.Pacts;

import javax.ws.rs.core.Response;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;

public class GetDirectDebitEventsTest {

    static final String API_KEY = ApiKeyGenerator.apiKeyValueOf("TEST_BEARER_TOKEN", "qwer9yuhgf");
    
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

    private String baseUrl;
    
    @Before
    public void setUp() {
        baseUrl = app.getConfiguration().getBaseUrl();
    }
    
    @Test
    @PactVerification({"direct-debit-connector", "publicauth"})
    @Pacts(pacts = {"publicapi-direct-debit-connector-get-events"})
    @Pacts(pacts = {"publicapi-publicauth"}, publish = false)
    public void getDirectDebitEvents() {
        String requestPath = "/v1/events?to_date=2018-03-13T10:00:05Z&from_date=2018-03-13T10:00:03Z&display_size=100&page=1&agreement_id=1";
        
        given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .get(requestPath)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(JSON)
                .body("count", is(2))
                .body("results", hasSize(2))
                .body("results[0].external_id", is("201"))
                .body("results[0].event_date", is("2018-03-13T10:00:04.666Z"))
                .body("results[0].event", is("PAYMENT_ACKNOWLEDGED_BY_PROVIDER"))
                .body("results[0].event_type", is("CHARGE"))
                .body("results[0].agreement_id", is("1"))
                .body("results[0].payment_id", is("4"))
                .body("results[0]._links.agreement", is(app.getConfiguration().getBaseUrl() + "v1/agreements/1"))
                .body("results[0]._links.payment", is(app.getConfiguration().getBaseUrl() + "v1/payments/4"))
                .body("_links.next_page", isEmptyOrNullString())
                .body("_links.prev_page", isEmptyOrNullString())
                .body("_links.self.href", is( baseUrl + "v1/events?to_date=2018-03-13T10:00:04Z&from_date=2018-03-13T10:00:04Z&agreement_id=1&page=1&display_size=100"))
                .body("_links.last_page.href", is(baseUrl + "v1/events?to_date=2018-03-13T10:00:04Z&from_date=2018-03-13T10:00:04Z&agreement_id=1&page=1&display_size=100"))
                .body("_links.first_page.href", is(baseUrl + "v1/events?to_date=2018-03-13T10:00:04Z&from_date=2018-03-13T10:00:04Z&agreement_id=1&page=1&display_size=100"));
    }
    
    @Test
    @PactVerification({"publicauth"})
    @Pacts(pacts = {"publicapi-publicauth"}, publish = false)
    public void shouldReturnBadRequestIfDatesAreInvalid() {
        String requestPath = "/v1/events?to_date=bad-date&from_date=bad-date";

        given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .get(requestPath)
                .then()
                .statusCode(422)
                .body(containsString("to_date"))
                .body(containsString("from_date"));
    }
}
