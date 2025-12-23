package uk.gov.pay.api.it;

import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.pay.api.app.PublicApi;
import uk.gov.pay.api.app.config.PublicApiConfig;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@ExtendWith(DropwizardExtensionsSupport.class)
class PingIT {

    private static final DropwizardAppExtension<PublicApiConfig> app = new DropwizardAppExtension<>(
            PublicApi.class,
            resourceFilePath("config/test-config.yaml")
    );

    @Test
    void testPing() {
        given().port(app.getAdminPort())
                .get("/healthcheck")
                .then()
                .body("ping.healthy", is(true));
    }
}
