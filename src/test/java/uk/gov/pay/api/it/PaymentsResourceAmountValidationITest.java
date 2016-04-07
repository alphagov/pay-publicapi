package uk.gov.pay.api.it;

import com.jayway.jsonassert.JsonAssert;
import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

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
    public void createPayment_responseWith422_whenAmountIsNegative() throws IOException {

        String payload = "{" +
                "  \"amount\" : -123," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        InputStream body = postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Invalid attribute value: amount. Must be greater than or equal to 1"));
    }

    @Test
    public void createPayment_responseWith422_whenAmountIsBiggerThanTheMaximumAllowed() throws IOException {

        String payload = "{" +
                "  \"amount\" : 10000001," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        InputStream body = postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: amount. Must be less than or equal to 10000000"));
    }

    @Test
    public void createPayment_responseWith422_whenAmountFieldHasNullValue() throws IOException {

        String payload = "{" +
                "  \"amount\" : null," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        InputStream body = postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0103"))
                .assertThat("$.description", is("Missing mandatory attribute: amount"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountFieldIsNotNumeric() throws IOException {

        String payload = "{" +
                "  \"amount\" : \"hola world!\"," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        InputStream body = postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0100"))
                .assertThat("$.description", is("Invalid attribute value: amount. Unrecognised format"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountFieldIsNotAValidJsonField() throws IOException {

        String payload = "{" +
                "  \"amount\" : { \"whatever\": 1 }," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        System.out.println("payload = " + payload);
        InputStream body = postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0100"))
                .assertThat("$.description", is("Invalid attribute value: amount. Unrecognised format"));
    }

    @Test
    public void createPayment_responseWith422_whenAmountFieldIsBlank() throws IOException {

        String payload = "{" +
                "  \"amount\" : \"    \"," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        InputStream body = postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0100"))
                .assertThat("$.description", is("Invalid attribute value: amount. Unrecognised format"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountFieldIsMissing() throws IOException {

        String payload = "{" +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        InputStream body = postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0103"))
                .assertThat("$.description", is("Missing mandatory attribute: amount"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsHexadecimal() throws IOException {

        String payload = "{" +
                "  \"amount\" : 0x1000," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        InputStream body = postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0100"))
                .assertThat("$.description", is("Unable to parse JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsBinary() throws IOException {

        String payload = "{" +
                "  \"amount\" : 0B101," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        InputStream body = postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0100"))
                .assertThat("$.description", is("Unable to parse JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsOctal() throws IOException {

        String payload = "{" +
                "  \"amount\" : 017," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        InputStream body = postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0100"))
                .assertThat("$.description", is("Unable to parse JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsNullByteEncoded() throws IOException {

        String payload = "{" +
                "  \"amount\" : %00," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        InputStream body = postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0100"))
                .assertThat("$.description", is("Unable to parse JSON"));
    }

    @Test
    public void createPayment_responseWith400_whenAmountIsFloat() throws IOException {

        String payload = "{" +
                "  \"amount\" : 27.55," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        InputStream body = postPaymentResponse(BEARER_TOKEN, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0100"))
                .assertThat("$.description", is("Invalid attribute value: amount. Unrecognised format"));
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
