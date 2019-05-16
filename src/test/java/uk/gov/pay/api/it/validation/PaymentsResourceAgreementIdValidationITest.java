package uk.gov.pay.api.it.validation;

import com.jayway.jsonassert.JsonAssert;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.service.AgreementService;
import uk.gov.pay.api.utils.PublicAuthMockClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

public class PaymentsResourceAgreementIdValidationITest extends PaymentResourceITestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentsResourceAgreementIdValidationITest.class);
    
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);
    
    @Before
    public void setUpBearerToken() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void createPayment_responseWith400_whenAgreementIdIsNumeric() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"https://somewhere.com\"," +
                "  \"agreement_id\" : 1234" +
                "}";

        LOGGER.info(payload);
        
        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("agreement_id"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: agreement_id. Must be a valid agreement ID"));
    }

    @Test
    public void createPayment_responseWith400_whenAgreementIdIsEmpty() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"https://somewhere.com\"," +
                "  \"agreement_id\" : \"\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("agreement_id"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: agreement_id"));
    }

    @Test
    public void createPayment_responseWith400_whenAgreementIdIsBlank() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"https://somewhere.com\"," +
                "  \"agreement_id\" : \"    \"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("agreement_id"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: agreement_id"));
    }
    
    @Test
    public void createPayment_responseWith400_whenAgreementIdIsNull() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"https://somewhere.com\"," +
                "  \"agreement_id\" : null" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("agreement_id"))
                .assertThat("$.code", is("P0101"))
                .assertThat("$.description", is("Missing mandatory attribute: agreement_id"));
    }

    @Test
    public void createPayment_responseWith422_whenAgreementIdSizeIsGreaterThanMaxLength() throws IOException {

        String aTooLongAgreementId = RandomStringUtils.randomAlphanumeric(27);
        
        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"https://somewhere.com\"," +
                "  \"agreement_id\" : \"" + aTooLongAgreementId + "\"" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("agreement_id"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: agreement_id. Must be less than or equal to 26 characters length"));
    }

    @Test
    public void createPayment_responseWith400_whenAgreementIdHasNotAValidJsonValue() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"https://somewhere.com\"," +
                "  \"agreement_id\" : " +
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
    public void createPayment_responseWith400_whenAgreementIdFieldIsNotExpectedJsonField() throws IOException {

        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"https://somewhere.com\"," +
                "  \"agreement_id\" : []" +
                "}";

        InputStream body = postPaymentResponse(API_KEY, payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("agreement_id"))
                .assertThat("$.code", is("P0102"))
                .assertThat("$.description", is("Invalid attribute value: agreement_id. Must be a valid agreement ID"));
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
