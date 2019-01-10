package uk.gov.pay.api.it;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.http.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static uk.gov.pay.api.utils.Payloads.aSuccessfulPaymentPayload;

public class AuthorisationTest {

    @Rule
    public DropwizardAppRule<PublicApiConfig> app = new DropwizardAppRule<>(
            PublicApi.class,
            resourceFilePath("config/test-config.yaml"),
            config("connectorUrl", "http://localhost"),
            config("connectorDDUrl", "http://localhost"),
            config("publicAuthUrl", "http://localhost"));

    @Test
    public void shouldRefuseAuthorisationIfTokenIsNotPresent() {
        given().port(app.getLocalPort())
                .body(aSuccessfulPaymentPayload())
                .accept(JSON)
                .contentType(JSON)
                .post("/v1/payments/")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }
}
