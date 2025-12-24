package uk.gov.pay.api.it;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import uk.gov.pay.api.utils.PublicAuthMockClientJUnit5;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static uk.gov.pay.api.utils.Payloads.aSuccessfulPaymentPayload;

class AuthorisationIT extends PaymentResourceITestBase {

    private final PublicAuthMockClientJUnit5 publicAuthMockClient = new PublicAuthMockClientJUnit5(publicAuthServer);

    @Test
    void shouldRefuseAuthorisationIfTokenIsNotPresent() {
        given().port(app.getLocalPort())
                .body(aSuccessfulPaymentPayload())
                .accept(JSON)
                .contentType(JSON)
                .post("/v1/payments/")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    void shouldReturn500IfPublicAuthReturnsInvalidTokenPaymentType() {
        publicAuthMockClient.respondWithInvalidTokenType(API_KEY, "1");

        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .body(aSuccessfulPaymentPayload())
                .accept(JSON)
                .contentType(JSON)
                .post("/v1/payments/")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
}
