package uk.gov.pay.api.it;

import com.jayway.jsonassert.JsonAssert;
import com.jayway.restassured.response.ValidatableResponse;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.eclipse.jetty.http.HttpStatus.CONFLICT_409;
import static org.eclipse.jetty.http.HttpStatus.LENGTH_REQUIRED_411;
import static org.hamcrest.Matchers.hasSize;

public class PaymentsCancelResourceITest extends PaymentResourceITestBase {

    private static final String TEST_CHARGE_ID = "ch_ab2341da231434";
    private static final String CANCEL_PAYMENTS_PATH = PAYMENTS_PATH + "%s/cancel";

    @Test
    public void cancelPayment_Returns401_WhenUnauthorised() {
        publicAuthMock.respondUnauthorised();

        postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(401);
    }

    @Test
    public void successful_whenConnector_AllowsCancellation() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenCancelCharge(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID);

        postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(204);

        connectorMock.verifyCancelCharge(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void cancelPayment_returns400_whenConnectorRespondsWithA400() throws IOException {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondBadRequest_WhenCancelCharge(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID, "Invalid account Id");

        InputStream body = postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(400)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
//                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", Is.is("P0501"))
                .assertThat("$.description", Is.is("Cancellation of payment failed"));
    }

    @Test
    public void cancelPayment_returns404_whenPaymentNotFound() throws IOException {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondChargeNotFound_WhenCancelCharge(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID, "some backend error message");

        InputStream body = postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(404)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
//                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", Is.is("P0500"))
                .assertThat("$.description", Is.is("Not found"));
    }

    @Test
    public void cancelPayment_returns409_whenConnectorReturns409() throws IOException {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respond_WhenCancelCharge(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID, "some backend error message", CONFLICT_409);

        InputStream body = postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(409)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
//                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", Is.is("P0502"))
                .assertThat("$.description", Is.is("Cancellation of payment failed"));
    }

    @Test
    public void cancelPayment_returns500_whenConnectorResponseIsUnexpected() throws IOException {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respond_WhenCancelCharge(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID, "some backend error message", LENGTH_REQUIRED_411);

        InputStream body = postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(500)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
//                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", Is.is("P0598"))
                .assertThat("$.description", Is.is("Downstream system error"));
    }

    private ValidatableResponse postCancelPaymentResponse(String paymentId) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post(String.format(CANCEL_PAYMENTS_PATH, paymentId))
                .then();
    }
}
