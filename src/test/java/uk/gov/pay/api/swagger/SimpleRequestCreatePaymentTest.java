package uk.gov.pay.api.swagger;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.Response;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.model.SimpleResponse;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.junit.Assert.assertEquals;

public class SimpleRequestCreatePaymentTest {

    private static Logger logger = LoggerFactory.getLogger(SimpleRequestCreatePaymentTest.class);

    private final OpenApiInteractionValidator classUnderTest =
            OpenApiInteractionValidator.createFor("swagger/swagger.json").build();
    private final SimpleResponse okResponse = SimpleResponse.Builder.ok().build();

    @Test
    public void testSearchPayments() {

        final Request request = SimpleRequest.Builder
                .get("/v1/payments")
                .withAuthorization("auth-key")
                .withAccept("application/json")
                .build();

        final Response response = SimpleResponse.Builder
                .ok()
                .withContentType("application/json")
                .withBody(load("responses/search-payments.json"))
                .build();

        assertEquals(0, classUnderTest.validate(request, okResponse).getMessages().size());

        assertValidResponse(classUnderTest, response);
        assertEquals(0,
                classUnderTest.validateResponse("/v1/payments",
                        Request.Method.GET, response).getMessages().size());
    }

    private void assertValidResponse(OpenApiInteractionValidator classUnderTest, Response response) {

        List msg = classUnderTest.validateResponse("/v1/payments",
                Request.Method.GET, response).getMessages();

        if (!msg.isEmpty()) {
            for (Object m : msg) {
                System.out.println(m);
            }
        }
        assertEquals(0, msg.size());
    }

    private static String load(String location) {
        return fixture(location);
    }
}
