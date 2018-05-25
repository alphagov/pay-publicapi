package uk.gov.pay.api.it;

import com.jayway.jsonassert.JsonAssert;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static com.jayway.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

public class PaymentsResourceDescriptionValidationITest extends PaymentResourceITestBase {

    @Before
    public void setUpBearerToken() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void createPayment_responseWith400_whenDescriptionIsNumeric() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"description\" : 1234," +
                "  \"reference\" : \"Some reference\"," +
                "  \"return_url\" : \"http://my-payments.com\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("description"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: description. Must be a valid string format"));
    }

    @Test
    public void createPayment_responseWith400_whenDescriptionIsEmpty() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"description\" : \"\"," +
                "  \"reference\" : \"Some reference\"," +
                "  \"return_url\" : \"http://my-payments.com\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("description"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: description"));
    }

    @Test
    public void createPayment_responseWith400_whenDescriptionIsBlank() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"description\" : \"    \"," +
                "  \"reference\" : \"Some reference\"," +
                "  \"return_url\" : \"http://my-payments.com\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("description"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: description"));
    }

    @Test
    public void createPayment_responseWith400_whenDescriptionIsMissing() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"return_url\" : \"http://my-payments.com\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("description"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: description"));
    }

    @Test
    public void createPayment_responseWith400_whenDescriptionIsNull() throws IOException {


        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"description\" : null," +
                "  \"reference\" : \"Some reference\"," +
                "  \"return_url\" : \"http://my-payments.com\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("description"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: description"));
    }

    @Test
    public void createPayment_responseWith422_whenDescriptionSizeIsGreaterThanMaxLength() throws IOException {

        String aVeryLongReference = RandomStringUtils.randomAlphanumeric(256);

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"description\" : \"" + aVeryLongReference + "\"," +
                "  \"reference\" : \"Some reference\"," +
                "  \"return_url\" : \"https://www.example.com/return_url\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("description"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: description. Must be less than or equal to 255 characters length"));
    }

    @Test
    public void createPayment_responseWith400_whenDescriptionHasNotAValidJsonValue() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"description\" : " +
                "  \"reference\" : \"Some reference\"," +
                "  \"return_url\" : \"http://my-payments.com\"" +
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
    public void createPayment_responseWith400_whenDescriptionFieldIsNotExpectedJsonField() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"description\" : {\"whatever\" : 1}," +
                "  \"reference\" : \"Some reference\"," +
                "  \"return_url\" : \"http://my-payments.com\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("description"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: description. Must be a valid string format"));
    }
}
