package uk.gov.pay.api.it.validation;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.utils.PublicAuthMockClient;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;

@RunWith(JUnitParamsRunner.class)
public class StringDeserializerValidationIT extends PaymentResourceITestBase {

    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);

    @Before
    public void setup() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);
    }
    
    @Test
    @Parameters({"2", "2.5", "true"})
    public void shouldFailForConversions(String value) {

        String payload = format("{" +
                "  \"reference\" : %s," +
                "  \"return_url\" : \"https://example.com\"" +
                "}", value);

        given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post("/v1/directdebit/mandates")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("size()", is(3))
                .body("field", is("reference"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: reference. Must be of type String"));
    }
}
