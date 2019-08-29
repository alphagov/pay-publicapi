package uk.gov.pay.api.it.telephone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.gov.pay.api.utils.PublicAuthMockClient;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static io.restassured.http.ContentType.JSON;

public class GetTelephonePaymentIT extends TelephonePaymentResourceITBase {
    
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);

    @Before
    public void setUpBearerTokenAndRequestBody() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);

        requestBody.put("amount", 100);
        requestBody.put("reference", "Some reference");
        requestBody.put("description", "Some description");
        requestBody.put("processor_id", "1PROC");
        requestBody.put("provider_id", "1PROV");
        requestBody.put("payment_outcome", Map.of("status", "success"));
        requestBody.put("card_type", "visa");
        requestBody.put("card_expiry", "01/08");
        requestBody.put("last_four_digits", "1234");
        requestBody.put("first_six_digits", "123456");
    }

    @After
    public void tearDown() {
        requestBody.clear();
    }

    @Test
    public void createTelephonePaymentWithAllFields() {
        requestBody.put("auth_code", "666");
        requestBody.put("created_date", "2018-02-21T16:04:25Z");
        requestBody.put("authorised_date", "2018-02-21T16:05:33Z");
        requestBody.put("name_on_card", "Jane Doe");
        requestBody.put("email_address", "jane_doe@example.com");
        requestBody.put("telephone_number", "+447700900796");
        
        postPaymentResponse(toJson(requestBody))
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
                .body("state.code", is("P0010"));
    }

    @Test
    public void createTelephonePaymentWithRequiredFields() {
        postPaymentResponse(toJson(requestBody))
                .statusCode(201)
                .contentType(JSON)
                .body("amount", is(100))
                .body("reference", is("Some reference"))
                .body("description", is("Some description"))
                .body("processor_id", is("1PROC"))
                .body("payment_outcome.status", is("success"))
                .body("card_type", is("visa"))
                .body("card_expiry", is("01/08"))
                .body("last_four_digits", is("1234"))
                .body("first_six_digits", is("123456"))
                .body("payment_id", is("dummypaymentid123notpersisted"))
                .body("state.status", is("success"))
                .body("state.finished", is(true))
                .body("state.message", is("Created"))
                .body("state.code", is("P0010"));
    }
}
