package uk.gov.pay.api.it;

import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;

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

        String payload = "{" +
                "  \"amount\" : -123," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("amount must be greater than or equal to 1 (was -123)"));
    }

    @Test
    public void createPayment_responseWith422_whenAmountIsSmallerThanTheMinimumAllowed() {

        String payload = "{" +
                "  \"amount\" : 0," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("amount must be greater than or equal to 1 (was 0)"));
    }

    @Test
    public void createPayment_responseWith422_whenAmountIsBiggerThanTheMaximumAllowed() {

        String payload = "{" +
                "  \"amount\" : 10000001," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("amount must be less than or equal to 10000000 (was 10000001)"));
    }

    @Test
    public void createPayment_responseWith422_whenAmountFieldHasNullValue() {

        String payload = "{" +
                "  \"amount\" : null," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("amount may not be null (was null)"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountFieldIsNotNumeric() {

        String payload = "{" +
                "  \"amount\" : \"£%&dasg*??<>\"," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountFieldIsNotAValidJsonField() {

        String payload = "{" +
                "  \"amount\" : { \"whatever\": 1 }," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith422_whenAmountFieldIsBlank() {

        String payload = "{" +
                "  \"amount\" : \"    \"," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("amount may not be null (was null)"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountFieldIsMissing() {

        String payload = "{" +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("amount may not be null (was null)"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsHexadecimal() {

        String payload = "{" +
                "  \"amount\" : 0x1000," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsBinary() {

        String payload = "{" +
                "  \"amount\" : 0B101," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsOctal() {

        String payload = "{" +
                "  \"amount\" : 017," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsFloat() {

        String payload = "{" +
                "  \"amount\" : 27.55," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsDouble() {

        String payload = "{" +
                "  \"amount\" : 27.55d," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsNullByteEncoded() {

        String payload = "{" +
                "  \"amount\" : %00," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.over.the/rainbow/{paymentID}\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
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
