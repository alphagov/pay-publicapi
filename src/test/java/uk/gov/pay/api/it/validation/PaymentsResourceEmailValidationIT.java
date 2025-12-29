package uk.gov.pay.api.it.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.utils.PublicAuthMockClientJUnit5;

import java.util.Map;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.core.Is.is;

class PaymentsResourceEmailValidationIT extends PaymentResourceITestBase {

    private final PublicAuthMockClientJUnit5 publicAuthMockClient = new PublicAuthMockClientJUnit5(publicAuthServer);

    @BeforeEach
    void setUpBearerToken() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    void shouldRespondWith422_whenEmailIsGreaterThan254Chars() {

        String payload = toJson(
                Map.of("amount", 100,
                        "reference", "Some ref",
                        "description",
                        "hi", "return_url", "https://somewhere.gov.uk/rainbow/1",
                        "email", "aVeryLongEmail".repeat(20) + "@email.invalid"));

        postPaymentResponse(payload)
                .statusCode(422)
                .contentType(JSON)
                .body("size()", is(3))
                .body("field", is("email"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: email. Must be less than or equal to 254 characters length"));
    }
}
