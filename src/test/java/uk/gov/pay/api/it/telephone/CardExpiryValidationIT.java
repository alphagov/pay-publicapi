package uk.gov.pay.api.it.telephone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.utils.PublicAuthMockClient;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.core.Is.is;

public class CardExpiryValidationIT extends TelephonePaymentResourceITBase {
    
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
        requestBody.put("last_four_digits", "1234");
        requestBody.put("first_six_digits", "123456");
    }
    
    @After
    public void tearDown() {
        requestBody.clear();
    }

    @Test
    public void respondWith422_whenMonthis00() {
        requestBody.put("card_expiry", "00/99");
        postPaymentResponse(toJson(requestBody))
                .statusCode(422);
    }

    @Test
    public void respondWith422_whenMonthis99() {
        requestBody.put("card_expiry", "99/99");
        postPaymentResponse(toJson(requestBody))
                .statusCode(422);
    }

    @Test
    public void respondWith422_whenCardExpiryisNull() {
        requestBody.put("card_expiry", null);
        postPaymentResponse(toJson(requestBody))
                .statusCode(422);
    }
}
