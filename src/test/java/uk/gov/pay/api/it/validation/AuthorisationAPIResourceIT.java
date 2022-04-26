package uk.gov.pay.api.it.validation;

import io.restassured.response.ValidatableResponse;
import org.junit.Test;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.utils.PublicAuthMockClient;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;

public class AuthorisationAPIResourceIT extends PaymentResourceITestBase {

    private static final String AUTH_PATH = "/v1/auth";
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);

    @Test
    public void authorisation_responseWith400_whenCardNumberFieldHasNullValue() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        String payload = toJson(
                Map.of("one_time_token", "1234567890",
                        "card_number", "1234567890123456",
                        "cvc", 123,
                        "expiry_date", "09/27",
                        "cardholder_name", "Joe Boggs"));

        postAuthRequest(payload)
                .statusCode(422)
                .contentType(JSON)
                .body("code", is("P1202"))
                .body("field", is("cvc"))
                .body("description", is("Invalid attribute value: cvc. Must be a string"));
    }

    @Test
    public void authorisation_respondWith204_whenRequestIsOk() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        String payload = toJson(
                Map.of("one_time_token", "1234567890",
                        "card_number", "1234567890123456",
                        "cvc", "123",
                        "expiry_date", "09/27",
                        "cardholder_name", "Joe Boggs"));

        postAuthRequest(payload)
                .statusCode(204);
    }

    protected ValidatableResponse postAuthRequest(String payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post(AUTH_PATH)
                .then();
    }
}
