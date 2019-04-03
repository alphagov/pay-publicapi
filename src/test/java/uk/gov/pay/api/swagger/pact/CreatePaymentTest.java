package uk.gov.pay.api.swagger.pact;

import com.atlassian.oai.validator.pact.PactProviderValidationResults;
import com.atlassian.oai.validator.pact.PactProviderValidator;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.fail;

/**
 * An example Pact Provider test that uses the {@link PactProviderValidator} to validate consumer Pacts
 * against a service Swagger API specification.
 */
public class CreatePaymentTest {

    public static final String SWAGGER_JSON_URL = "swagger/swagger.json";

    /**
     * This test simulates running against a Consumer where all interactions in the Pact spec are valid according
     * to the Swagger API spec.
     */
    @Test
    public void validate_withLocalPact_withValidInteractions() {

        final PactProviderValidator validator = PactProviderValidator
                .createFor(SWAGGER_JSON_URL)
                .withConsumer("ExampleConsumer", pactUrl("valid-create-payment.json"))
                .build();

        assertNoBreakingChanges(validator.validate());

    }
    private URL pactUrl(final String name) {
        return getClass().getResource("/pacts/" + name);
    }

    private void assertNoBreakingChanges(final PactProviderValidationResults results) {

        if (results != null && results.hasErrors()) {
            final StringBuilder msg = new StringBuilder("Validation errors found.\n\t");
            msg.append(results.getValidationFailureReport().replace("\n", "\n\t"));
            fail(msg.toString());
        }
    }

}
