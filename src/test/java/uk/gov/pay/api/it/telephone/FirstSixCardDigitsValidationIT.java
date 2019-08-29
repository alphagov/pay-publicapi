package uk.gov.pay.api.it.telephone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.utils.PublicAuthMockClient;

import java.util.HashMap;
import java.util.Map;

public class FirstSixCardDigitsValidationIT extends TelephonePaymentResourceITBase {
    
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);

    private static final HashMap<String, Object> requestBody = new HashMap<>();

    @Before
    public void setUpBearerTokenAndRequestBody() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);

        requestBody.put("amount", 100);
        requestBody.put("reference", "Some refeerence");
        requestBody.put("description", "Some description");
        requestBody.put("processor_id", "1PROC");
        requestBody.put("provider_id", "1PROV");
        requestBody.put("payment_outcome", Map.of("status", "success"));
        requestBody.put("card_type", "visa");
        requestBody.put("card_expiry", "00/99");
        requestBody.put("last_four_digits", "1234");
    }

    @After
    public void tearDown() {
        requestBody.clear();
    }
    
    @Test
    public void respondWith422_whenFiveDigitsProvideOnly() {
        requestBody.put("first_six_digits", "12345");
        postPaymentResponse(toJson(requestBody))
                .statusCode(422);
    }

    @Test
    public void respondWith422_whenSevenDigitsProvideOnly() {
        requestBody.put("first_six_digits", "1234567");
        postPaymentResponse(toJson(requestBody))
                .statusCode(422);
    }

    @Test
    public void respondWith422_whenFirstSixDigitsIsNull() {
        requestBody.put("first_six_digits", null);
        postPaymentResponse(toJson(requestBody))
                .statusCode(422);
    }
}
