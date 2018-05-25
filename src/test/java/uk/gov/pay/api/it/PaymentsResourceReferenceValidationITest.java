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

public class PaymentsResourceReferenceValidationITest extends PaymentResourceITestBase {

    @Before
    public void setUpBearerToken() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void createPayment_responseWith400_whenReferenceIsNumeric() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : 1234," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://my-payments.com\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("reference"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: reference. Must be a valid string format"));
    }

    @Test
    public void createPayment_responseWith400_whenReferenceIsEmpty() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://my-payments.com\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("reference"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: reference"));
    }

    @Test
    public void createPayment_responseWith400_whenReferenceIsBlank() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"    \"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://my-payments.com\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("reference"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: reference"));
    }

    @Test
    public void createPayment_responseWith400_whenReferenceIsMissing() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://my-payments.com\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("reference"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: reference"));
    }

    @Test
    public void createPayment_responseWith400_whenReferenceIsNull() throws IOException {


        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : null," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://my-payments.com\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("reference"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: reference"));
    }

    @Test
    public void createPayment_responseWith422_whenReferenceSizeIsGreaterThanMaxLength() throws IOException {

        String aVeryLongReference = RandomStringUtils.randomAlphanumeric(256);

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"" + aVeryLongReference + "\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"https://www.example.com/return_url\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("reference"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: reference. Must be less than or equal to 255 characters length"));
    }

    @Test
    public void createPayment_responseWith400_whenReferenceHasNotAValidJsonValue() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : " +
                "  \"description\" : \"Some description\"," +
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
    public void createPayment_responseWith400_whenReferenceFieldIsNotExpectedJsonField() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : {\"whatever\" : 1}," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://my-payments.com\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("reference"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: reference. Must be a valid string format"));
    }
}
