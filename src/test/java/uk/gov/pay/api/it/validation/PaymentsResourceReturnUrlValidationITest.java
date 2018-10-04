package uk.gov.pay.api.it.validation;

import com.jayway.jsonassert.JsonAssert;
import com.jayway.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.it.PaymentResourceITestBase;

import java.io.IOException;
import java.io.InputStream;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

public class PaymentsResourceReturnUrlValidationITest extends PaymentResourceITestBase {

    @Before
    public void setUpBearerToken() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void createPayment_responseWith400_whenReturnUrlIsNumeric() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : 123" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("return_url"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: return_url. Must be a valid URL format"));
    }

    @Test
    public void createPayment_responseWith400_whenReturnUrlIsEmpty() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("return_url"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: return_url"));
    }

    @Test
    public void createPayment_responseWith400_whenReturnUrlIsBlank() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"  \"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("return_url"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: return_url"));
    }

    @Test
    public void createPayment_responseWith400_whenReturnUrlIsMissing() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("return_url"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: return_url"));
    }

    @Test
    public void createPayment_responseWith400_whenReturnUrlIsNull() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : null" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("return_url"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: return_url"));
    }

    @Test
    public void createPayment_responseWith422_whenReturnUrlSizeIsGreaterThanMaxLengthAndHasInvalidFormat() throws IOException {

        String aVeryBigInvalidReturnUrl = RandomStringUtils.randomAlphanumeric(2001);

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"" + aVeryBigInvalidReturnUrl + "\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("return_url"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: return_url. Must be less than or equal to 2000 characters length"));
    }

    @Test
    public void createPayment_responseWith422_whenReturnUrlSizeIsGreaterThanMaxLengthAndHasValidFormat_lengthIsFirstToCheck_failFast() throws IOException {

        String aVeryBigValidReturnUrl = "http://payments.gov.uk?something=" + RandomStringUtils.randomAlphanumeric(2000);

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"" + aVeryBigValidReturnUrl + "\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("return_url"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: return_url. Must be less than or equal to 2000 characters length"));
    }

    @Test
    public void createPayment_responseWith422_whenReturnUrlIsNotAnUrl() throws IOException {

        String anInvalidUrl = RandomStringUtils.randomAlphanumeric(50);

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"" + anInvalidUrl + "\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("return_url"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: return_url. Must be a valid URL format"));
    }

    @Test
    public void createPayment_responseWith400_whenReturnUrlHasNotAValidJsonValue() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : " +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
//                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0197"))
                .assertThat("$.description", is("Unable to parse JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenReturnUrlFieldIsNotExpectedJsonField() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : []" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("return_url"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: return_url. Must be a valid URL format"));
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
