package uk.gov.pay.api.it.validation;

import com.jayway.jsonassert.JsonAssert;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.utils.PublicAuthMockClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

public class CreatePaymentReturnUrlValidationITest extends PaymentResourceITestBase {

    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);
    
    @Before
    public void setUpBearerToken() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void respondWith422_whenReturnUrlIsHttp() {
        String payload = toJson(Map.of("amount", 100,
                "reference", "Some ref",
                "description","hi", 
                "return_url", "http://somewhere.gov.uk/"));

        postPaymentResponse(payload)
                .statusCode(422)
                .contentType(JSON)
                .body("size()", is(3))
                .body("field", is("return_url"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: return_url. Must be a valid URL format"));
    }
    
    @Test
    public void respondWith400_whenReturnUrlIsNumeric() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : 123" +
                "}";

        InputStream body = postPaymentResponse(payload)
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
    public void respondWith400_whenReturnUrlIsEmpty() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"\"" +
                "}";

        InputStream body = postPaymentResponse(payload)
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
    public void respondWith400_whenReturnUrlIsBlank() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"  \"" +
                "}";

        InputStream body = postPaymentResponse(payload)
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
    public void respondWith400_whenReturnUrlIsMissing() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"" +
                "}";

        InputStream body = postPaymentResponse(payload)
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
    public void respondWith400_whenReturnUrlIsNull() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : null" +
                "}";

        InputStream body = postPaymentResponse(payload)
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
    public void respondWith422_whenReturnUrlSizeIsGreaterThanMaxLengthAndHasValidFormat_lengthIsFirstToCheck_failFast() throws IOException {

        String aVeryBigValidReturnUrl = "https://payments.gov.uk?something=" + "aVeryLongString12345".repeat(100);

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"" + aVeryBigValidReturnUrl + "\"" +
                "}";

        InputStream body = postPaymentResponse(payload)
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
    public void respondWith422_whenReturnUrlIsNotAnUrl() throws IOException {

        String anInvalidUrl = RandomStringUtils.randomAlphanumeric(50);

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"" + anInvalidUrl + "\"" +
                "}";

        InputStream body = postPaymentResponse(payload)
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
    public void respondWith400_whenReturnUrlHasNotAValidJsonValue() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : " +
                "}";

        InputStream body = postPaymentResponse(payload)
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
    public void respondWith400_whenReturnUrlFieldIsNotExpectedJsonField() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : []" +
                "}";

        InputStream body = postPaymentResponse(payload)
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
}
