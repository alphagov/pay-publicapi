package uk.gov.pay.api.it.telephone;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.utils.PublicAuthMockClient;

import java.util.HashMap;
import java.util.Map;

public class FirstSixCardDigitsValidationIT extends TelephonePaymentResourceITBase {
    
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);

    @Before
    public void setUpBearerToken() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void respondWith422_whenFiveDigitsProvideOnly() {
        HashMap<String, Object> request_body = new HashMap<>();
        request_body.put("amount", 100);
        request_body.put("reference", "Some reference");
        request_body.put("description", "Some description");
        request_body.put("created_date", "2018-02-21T16:04:25Z");
        request_body.put("authorised_date", "2018-02-21T16:05:33Z");
        request_body.put("processor_id", "1PROC");
        request_body.put("provider_id", "1PROV");
        request_body.put("auth_code", "666");
        request_body.put("payment_outcome", Map.of("status", "success"));
        request_body.put("card_type", "visa");
        request_body.put("name_on_card", "Jane Doe");
        request_body.put("email_address", "jane_doe@example.com");
        request_body.put("card_expiry", "01/99");
        request_body.put("last_four_digits", "1234");
        request_body.put("first_six_digits", "12345");
        request_body.put("telephone_number", "+447700900796");

        String payload = toJson(request_body);
        
        postPaymentResponse(payload)
                .statusCode(422);
    }

    @Test
    public void respondWith422_whenSevenDigitsProvideOnly() {
        HashMap<String, Object> request_body = new HashMap<>();
        request_body.put("amount", 100);
        request_body.put("reference", "Some reference");
        request_body.put("description", "Some description");
        request_body.put("created_date", "2018-02-21T16:04:25Z");
        request_body.put("authorised_date", "2018-02-21T16:05:33Z");
        request_body.put("processor_id", "1PROC");
        request_body.put("provider_id", "1PROV");
        request_body.put("auth_code", "666");
        request_body.put("payment_outcome", Map.of("status", "success"));
        request_body.put("card_type", "visa");
        request_body.put("name_on_card", "Jane Doe");
        request_body.put("email_address", "jane_doe@example.com");
        request_body.put("card_expiry", "01/99");
        request_body.put("last_four_digits", "1234");
        request_body.put("first_six_digits", "1234567");
        request_body.put("telephone_number", "+447700900796");

        String payload = toJson(request_body);

        postPaymentResponse(payload)
                .statusCode(422);
    }

    @Test
    public void respondWith422_whenFirstSixDigitsIsNull() {
        HashMap<String, Object> request_body = new HashMap<>();
        request_body.put("amount", 100);
        request_body.put("reference", "Some reference");
        request_body.put("description", "Some description");
        request_body.put("created_date", "2018-02-21T16:04:25Z");
        request_body.put("authorised_date", "2018-02-21T16:05:33Z");
        request_body.put("processor_id", "1PROC");
        request_body.put("provider_id", "1PROV");
        request_body.put("auth_code", "666");
        request_body.put("payment_outcome", Map.of("status", "success"));
        request_body.put("card_type", "visa");
        request_body.put("name_on_card", "Jane Doe");
        request_body.put("email_address", "jane_doe@example.com");
        request_body.put("card_expiry", "01/08");
        request_body.put("last_four_digits", "1234");
        request_body.put("first_six_digits", null);
        request_body.put("telephone_number", "+447700900796");

        String payload = toJson(request_body);

        postPaymentResponse(payload)
                .statusCode(422);
    }
}
