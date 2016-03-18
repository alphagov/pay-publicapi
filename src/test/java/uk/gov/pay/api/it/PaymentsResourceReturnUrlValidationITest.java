package uk.gov.pay.api.it;

import com.jayway.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

public class PaymentsResourceReturnUrlValidationITest extends PaymentResourceITestBase {

    @Before
    public void setUpBearerToken() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void createPayment_responseWith422_whenReturnUrlIsNumeric() {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : 123" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("returnUrl must be a valid URL format (was 123)"));
    }

    @Test
    public void createPayment_responseWith422_whenReturnUrlIsEmpty() {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("returnUrl may not be empty (was )"));
    }

    @Test
    public void createPayment_responseWith422_whenReturnUrlIsBlank() {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"  \"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(2))
                .body("errors", hasItem("returnUrl may not be empty (was   )"))
                .body("errors", hasItem("returnUrl must be a valid URL format (was   )"));
    }

    @Test
    public void createPayment_responseWith400_whenReturnUrlIsMissing() {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith422_whenReturnUrlSizeIsGreaterThanMaxLengthAndHasInvalidFormat() {

        String aVeryBigInvalidReturnUrl = RandomStringUtils.randomAlphanumeric(2049);

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"" + aVeryBigInvalidReturnUrl + "\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(2))
                .body("errors", hasItem("returnUrl must be a valid URL format (was " + aVeryBigInvalidReturnUrl + ")"))
                .body("errors", hasItem("returnUrl size must be between 0 and 2048 (was " + aVeryBigInvalidReturnUrl + ")"));
    }

    @Test
    public void createPayment_responseWith422_whenReturnUrlSizeIsGreaterThanMaxLengthAndHasValidFormat() {

        String aVeryBigValidReturnUrl = "http://" + RandomStringUtils.randomAlphanumeric(2040) + ".com";

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"" + aVeryBigValidReturnUrl + "\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("returnUrl size must be between 0 and 2048 (was " + aVeryBigValidReturnUrl + ")"));
    }

    @Test
    public void createPayment_responseWith422_whenReturnUrlIsNotAnUrl() {

        String anInvalidUrl = RandomStringUtils.randomAlphanumeric(50);

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"" + anInvalidUrl + "\"" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("returnUrl must be a valid URL format (was " + anInvalidUrl + ")"));
    }

    @Test
    public void createPayment_responseWith422_whenReturnUrlIsNull() {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : null" +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .body("errors", hasSize(1))
                .body("errors", hasItem("returnUrl may not be empty (was null)"));
    }

    @Test
    public void createPayment_responseWith422_whenReturnUrlDoesNotHaveAValue() {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : " +
                "}";

        postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Unable to process JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenReturnUrlFieldIsNotExpectedJsonField() {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : []" +
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
