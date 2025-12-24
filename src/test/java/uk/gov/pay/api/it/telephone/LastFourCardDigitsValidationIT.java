package uk.gov.pay.api.it.telephone;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.pay.api.utils.PublicAuthMockClientJUnit5;

import java.util.Map;

class LastFourCardDigitsValidationIT extends TelephonePaymentResourceITBase {

    private final PublicAuthMockClientJUnit5 publicAuthMockClient = new PublicAuthMockClientJUnit5(publicAuthServer);

    @BeforeEach
    void setUpBearerTokenAndRequestBody() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);

        requestBody.put("amount", 100);
        requestBody.put("reference", "Some reference");
        requestBody.put("description", "Some description");
        requestBody.put("processor_id", "1PROC");
        requestBody.put("provider_id", "1PROV");
        requestBody.put("payment_outcome", Map.of("status", "success"));
        requestBody.put("card_type", "visa");
        requestBody.put("card_expiry", "01/99");
        requestBody.put("first_six_digits", "123456");
    }

    @AfterEach
    void tearDown() {
        requestBody.clear();
    }

    @Test
    void respondWith422_whenThreeDigitsProvideOnly() {
        requestBody.put("last_four_digits", "123");
        postPaymentResponse(toJson(requestBody))
                .statusCode(422);
    }
}
