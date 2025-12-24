package uk.gov.pay.api.it.validation;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.pay.api.it.telephone.TelephonePaymentResourceITBase;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.utils.PublicAuthMockClientJUnit5;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static java.lang.String.format;
import static org.hamcrest.core.Is.is;

public class StringDeserializerValidationIT extends TelephonePaymentResourceITBase {

    private final PublicAuthMockClientJUnit5 publicAuthMockClient = new PublicAuthMockClientJUnit5(publicAuthServer);

    @BeforeEach
    public void setup() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, TokenPaymentType.CARD);
    }

    @ParameterizedTest
    @CsvSource({"2", "2.5", "true"})
    public void shouldFailForConversions(String value) {

        String payload = format("{" +
                "  \"amount\" : 100," +
                "  \"description\" : \"desc\"," +
                "  \"reference\" : %s," +
                "  \"processor_id\" : \"1PROC\"" +
                "}", value);

        given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post("/v1/payment_notification/")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("size()", is(3))
                .body("field", is("reference"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: reference. Must be of type String"));
    }
}
