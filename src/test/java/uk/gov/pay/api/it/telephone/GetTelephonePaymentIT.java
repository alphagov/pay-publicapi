package uk.gov.pay.api.it.telephone;

import org.junit.Before;
import org.junit.Test;

import uk.gov.pay.api.utils.PublicAuthMockClient;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static io.restassured.http.ContentType.JSON;

public class GetTelephonePaymentIT extends TelephonePaymentResourceITBase {
    
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);

    @Before
    public void setUpBearerToken() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void createTelephonePayment() {
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
        request_body.put("first_six_digits", "123456");
        request_body.put("telephone_number", "+447700900796");
        
        postPaymentResponse(toJson(request_body))
                .statusCode(201)
                .contentType(JSON)
                .body("amount", is(100))
                .body("reference", is("Some reference"))
                .body("description", is("Some description"))
                .body("created_date", is("2018-02-21T16:04:25Z"))
                .body("authorised_date", is("2018-02-21T16:05:33Z"))
                .body("processor_id", is("1PROC"))
                .body("auth_code", is("666"))
                .body("payment_outcome.status", is("success"))
                .body("card_type", is("visa"))
                .body("name_on_card", is("Jane Doe"))
                .body("email_address", is("jane_doe@example.com"))
                .body("card_expiry", is("01/08"))
                .body("last_four_digits", is("1234"))
                .body("first_six_digits", is("123456"))
                .body("telephone_number", is("+447700900796"))
                .body("payment_id", is("dummypaymentid123notpersisted"))
                .body("state.status", is("success"))
                .body("state.finished", is(true))
                .body("state.message", is("Created"))
                .body("state.code", is("P0010"))
                .extract().body().asString();
    }
}
