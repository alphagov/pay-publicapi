package uk.gov.pay.api.it.validation;

import com.jayway.jsonassert.JsonAssert;
import com.jayway.restassured.response.ValidatableResponse;
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

public class PaymentsResourceLanguageValidationITest extends PaymentResourceITestBase {

    @Before
    public void setUpBearerToken() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void createPayment_responseWith422_whenLanguageIsNotSupported() throws IOException {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 9900,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://example.com\",\n" +
                "  \"language\": \"fr\"\n" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("language"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: language. Must be \"en\" or \"cy\""));
    }

    @Test
    public void createPayment_responseWith400_whenLanguageIsNumeric() throws IOException {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 9900,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://example.com\",\n" +
                "  \"language\": 1337\n" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("language"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: language. Must be \"en\" or \"cy\""));
    }

    @Test
    public void createPayment_responseWith400_whenLanguageIsEmpty() throws IOException {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 9900,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://example.com\",\n" +
                "  \"language\": \"\"\n" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("language"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: language. Must be \"en\" or \"cy\""));
    }

    @Test
    public void createPayment_responseWith400_whenLanguageIsBlank() throws IOException {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 9900,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://example.com\",\n" +
                "  \"language\": \" \"\n" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("language"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: language. Must be \"en\" or \"cy\""));
    }

    @Test
    public void createPayment_responseWith400_whenLanguageIsNull() throws IOException {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 9900,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://example.com\",\n" +
                "  \"language\": null\n" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("language"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: language. Must be \"en\" or \"cy\""));
    }

    @Test
    public void createPayment_responseWith400_whenLanguageHasNotAValidJsonValue() throws IOException {
        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"https://example.com\"," +
                "  \"language\" : " +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0197"))
                .assertThat("$.description", is("Unable to parse JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenLanguageFieldIsNotExpectedJsonField() throws IOException {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 9900,\n" +
                "  \"language\": {\n" +
                "    \"whatever\": 1\n" +
                "  },\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://example.com\"\n" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("language"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: language. Must be \"en\" or \"cy\""));
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
