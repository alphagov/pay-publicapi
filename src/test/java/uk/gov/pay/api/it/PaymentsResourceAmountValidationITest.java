package uk.gov.pay.api.it;

import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.collect.Maps.newHashMap;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

public class PaymentsResourceAmountValidationITest extends PaymentResourceITestBase {

    @Before
    public void setUpBearerToken() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void createPayment_responseWith422_whenAmountIsNegative() {

        postPaymentResponse(BEARER_TOKEN, paymentPayload(-123))
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("amount must be greater than or equal to 1 (was -123)"));
    }

    @Test
    public void createPayment_responseWith422_whenAmountIsSmallerThanTheMinimumAllowed() {

        postPaymentResponse(BEARER_TOKEN, paymentPayload(0))
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("amount must be greater than or equal to 1 (was 0)"));
    }

    @Test
    public void createPayment_responseWith422_whenAmountIsBiggerThanTheMaximumAllowed() {

        postPaymentResponse(BEARER_TOKEN, paymentPayload(10000001L))
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("amount must be less than or equal to 10000000 (was 10000001)"));
    }

    @Test
    public void createPayment_responseWith422_whenAmountFieldHasNullValue() {

        postPaymentResponse(BEARER_TOKEN, paymentPayload(null))
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("amount may not be null (was null)"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountFieldIsNotNumeric() {

        postPaymentResponse(BEARER_TOKEN, paymentPayload("£%&dasg*??<>"))
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountFieldIsNotAValidJsonField() {

        postPaymentResponse(BEARER_TOKEN, paymentPayload(newHashMap()))
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith422_whenAmountFieldIsBlank() {

        postPaymentResponse(BEARER_TOKEN, paymentPayload("\"    \""))
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("amount may not be null (was null)"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsHexadecimal() {

        postPaymentResponse(BEARER_TOKEN, paymentPayload("0x1000"))
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsBinary() {

        postPaymentResponse(BEARER_TOKEN, paymentPayload("0B101"))
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsOctal() {

        postPaymentResponse(BEARER_TOKEN, paymentPayload("017"))
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsFloat() {

        postPaymentResponse(BEARER_TOKEN, paymentPayload(27.55))
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsDouble() {

        postPaymentResponse(BEARER_TOKEN, paymentPayload(27.55d))
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsNullByteEncoded() {

        postPaymentResponse(BEARER_TOKEN, paymentPayload("%00"))
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    private String paymentPayload(Object amount) {
        return "{" +
                "  \"amount\" : " + amount + "," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";
    }

    private ValidatableResponse postPaymentResponse(String bearerToken, String payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .post(PAYMENTS_PATH)
                .then();
    }
}
