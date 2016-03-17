package uk.gov.pay.api.it;

import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.is;

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
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenCancelCharge(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID);

        postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(204);

        connectorMock.verifyCancelCharge(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void cancelPayment_returns400_whenAccountIdIsMissing() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondBadRequest_WhenAccountIdIsMissing(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID, "Invalid account Id");

        postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(400)
                .body("message", is("Cancellation of charge failed."));
    }

    @Test
    public void respondWithBadRequest_whenPaymentNotFound() {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondChargeNotFound_WhenCancelCharge(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID, "some backend error message");

        postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Cancellation of charge failed."));
    }

    @Test
    public void respondWithBadRequest_whenConnector_DoesntAllowCancellation() {
        connectorMock.respondBadRequest_WhenCancelChargeNotAllowed(TEST_CHARGE_ID, GATEWAY_ACCOUNT_ID, "some other message");
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);

        postCancelPaymentResponse(TEST_CHARGE_ID)
                .statusCode(400)
                .contentType(JSON)
                .body("message", is("Cancellation of charge failed."));
    }

    private ValidatableResponse postCancelPaymentResponse(String paymentId) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + BEARER_TOKEN)
                .post(String.format(CANCEL_PAYMENTS_PATH, paymentId))
                .then();
    }
}
