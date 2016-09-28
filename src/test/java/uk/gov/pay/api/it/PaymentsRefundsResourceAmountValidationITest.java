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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

public class PaymentsRefundsResourceAmountValidationITest extends PaymentResourceITestBase {

    private static final int REFUND_AMOUNT_AVAILABLE = 9000;

    @Before
    public void setUpBearerToken() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void createPaymentRefund_responseWith422_whenAmountIsNegative() throws IOException {

        String payload = "{" +
                "  \"amount\" : -123" +
                "}";

        InputStream body = postPaymentRefundAndThen(API_KEY, "chargeId", payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("amount"))
                .assertThat("$.code", is("P0602"))
                .assertThat("$.description", is("Invalid attribute value: amount. Must be greater than or equal to 1"));
    }

    @Test
    public void createPaymentRefund_responseWith422_whenAmountIsBiggerThanTheMaximumAllowed() throws IOException {

        String payload = "{" +
                "  \"amount\" : 10000001" +
                "}";

        InputStream body = postPaymentRefundAndThen(API_KEY, "chargeId", payload)
                .statusCode(422)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("amount"))
                .assertThat("$.code", is("P0602"))
                .assertThat("$.description", is("Invalid attribute value: amount. Must be less than or equal to 10000000"));
    }

    @Test
    public void createPaymentRefund_responseWith400_whenAmountFieldHasNullValue() throws IOException {

        String payload = "{" +
                "  \"amount\" : null" +
                "}";

        InputStream body = postPaymentRefundAndThen(API_KEY, "chargeId", payload)
                .statusCode(400)
                .contentType(JSON)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("amount"))
                .assertThat("$.code", is("P0601"))
                .assertThat("$.description", is("Missing mandatory attribute: amount"));
    }

    @Test
    public void createPaymentRefund_responseWith400_whenAmountFieldIsNotNumeric() throws IOException {

        String payload = "{" +
                "  \"amount\" : \"hola world!\"" +
                "}";

        InputStream body = postPaymentRefundAndThen(API_KEY, "chargeId", payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("amount"))
                .assertThat("$.code", is("P0602"))
                .assertThat("$.description", is("Invalid attribute value: amount. Must be a valid numeric format"));
    }

    @Test
    public void createPaymentRefund_responseWith400_whenAmountFieldIsNotAValidJsonField() throws IOException {

        String payload = "{" +
                "  \"amount\" : { \"whatever\": 1 }" +
                "}";

        InputStream body = postPaymentRefundAndThen(API_KEY, "chargeId", payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("amount"))
                .assertThat("$.code", is("P0602"))
                .assertThat("$.description", is("Invalid attribute value: amount. Must be a valid numeric format"));
    }

    @Test
    public void createPaymentRefund_responseWith400_whenAmountFieldIsBlank() throws IOException {

        String payload = "{" +
                "  \"amount\" : \"    \"" +
                "}";

        InputStream body = postPaymentRefundAndThen(API_KEY, "chargeId", payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("amount"))
                .assertThat("$.code", is("P0602"))
                .assertThat("$.description", is("Invalid attribute value: amount. Must be a valid numeric format"));
    }

    @Test
    public void createPaymentRefund_responseWith400_whenAmountFieldIsMissing() throws IOException {

        String payload = "{}";

        InputStream body = postPaymentRefundAndThen(API_KEY, "chargeId", payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("amount"))
                .assertThat("$.code", is("P0601"))
                .assertThat("$.description", is("Missing mandatory attribute: amount"));
    }

    @Test
    public void createPaymentRefund_responseWith400_whenAmountIsHexadecimal() throws IOException {

        String payload = "{" +
                "  \"amount\" : 0x1000" +
                "}";

        InputStream body = postPaymentRefundAndThen(API_KEY, "chargeId", payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0697"))
                .assertThat("$.description", is("Unable to parse JSON"));
    }

    @Test
    public void createPaymentRefund_responseWith400_whenAmountIsBinary() throws IOException {

        String payload = "{" +
                "  \"amount\" : 0B101" +
                "}";

        InputStream body = postPaymentRefundAndThen(API_KEY, "chargeId", payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0697"))
                .assertThat("$.description", is("Unable to parse JSON"));
    }

    @Test
    public void createPaymentRefund_responseWith400_whenAmountIsOctal() throws IOException {

        String payload = "{" +
                "  \"amount\" : 017" +
                "}";

        InputStream body = postPaymentRefundAndThen(API_KEY, "chargeId", payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0697"))
                .assertThat("$.description", is("Unable to parse JSON"));
    }

    @Test
    public void createPaymentRefund_responseWith400_whenAmountIsNullByteEncoded() throws IOException {

        String payload = "{" +
                "  \"amount\" : %00" +
                "}";

        InputStream body = postPaymentRefundAndThen(API_KEY, "chargeId", payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0697"))
                .assertThat("$.description", is("Unable to parse JSON"));
    }

    @Test
    public void createPaymentRefund_responseWith400_whenAmountIsFloat() throws IOException {

        String payload = "{" +
                "  \"amount\" : 27.55" +
                "}";

        InputStream body = postPaymentRefundAndThen(API_KEY, "chargeId", payload)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("amount"))
                .assertThat("$.code", is("P0602"))
                .assertThat("$.description", is("Invalid attribute value: amount. Must be a valid numeric format"));
    }

    @Test
    public void createPaymentRefund_responseWith400_whenConnectorResponseIsErrorDueToAmountRequestedIsNotAvailableForRefund() throws IOException {

        int amount = 1000;
        String externalChargeId = "charge_12345";

        connectorMock.respondBadRequest_whenCreateARefund("full", amount, REFUND_AMOUNT_AVAILABLE, GATEWAY_ACCOUNT_ID, externalChargeId);

        String refundRequest = "{\"amount\":" + amount + "}";

        InputStream body = postPaymentRefundAndThen(API_KEY, externalChargeId, refundRequest)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0603"))
                .assertThat("$.description", is("The payment is not available for refund. Payment refund status: full"));
    }

    @Test
    public void createPaymentRefund_responseWith400_whenConnectorResponseIsErrorDueToChargeStatusMakesPaymentNonRefundable() throws IOException {

        int amount = 1000;
        String externalChargeId = "charge_12345";

        connectorMock.respondBadRequest_whenCreateARefund("pending", amount, REFUND_AMOUNT_AVAILABLE, GATEWAY_ACCOUNT_ID, externalChargeId);

        String refundRequest = "{\"amount\":" + amount + "}";

        InputStream body = postPaymentRefundAndThen(API_KEY, externalChargeId, refundRequest)
                .statusCode(400)
                .contentType(JSON)
                .extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0603"))
                .assertThat("$.description", is("The payment is not available for refund. Payment refund status: pending"));
    }

    private ValidatableResponse postPaymentRefundAndThen(String bearerToken, String chargeId, String payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .post(PAYMENTS_PATH + chargeId + "/refunds")
                .then();
    }
}
