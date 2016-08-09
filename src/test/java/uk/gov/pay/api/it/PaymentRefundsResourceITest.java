package uk.gov.pay.api.it;

import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Test;
import uk.gov.pay.api.it.fixtures.PaymentRefundJsonFixture;
import uk.gov.pay.api.utils.DateTimeUtils;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static org.hamcrest.core.Is.is;

public class PaymentRefundsResourceITest extends PaymentResourceITestBase {

    private static final int AMOUNT = 1000;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final String REFUND_ID = "111999";
    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final String CREATED_DATE = DateTimeUtils.toUTCDateString(TIMESTAMP);

    @Test
    public void getRefundById_shouldGetValidResponse() {

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithGetRefundById(GATEWAY_ACCOUNT_ID, CHARGE_ID, REFUND_ID, AMOUNT, "available", CREATED_DATE);

        getPaymentRefundByIdResponse(API_KEY, CHARGE_ID, REFUND_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("refund_id", is(REFUND_ID))
                .body("amount", is(AMOUNT))
                .body("status", is("available"))
                .body("created_date", is(CREATED_DATE))
                .body("_links.self.href", is(paymentRefundLocationFor(CHARGE_ID, REFUND_ID)))
                .body("_links.payment.href", is(paymentLocationFor(CHARGE_ID)));
    }

    @Test
    public void getRefundById_shouldGetNonAuthorized_whenPublicAuthRespondsUnauthorised() {

        publicAuthMock.respondUnauthorised();

        getPaymentRefundByIdResponse(API_KEY, CHARGE_ID, REFUND_ID)
                .statusCode(401);
    }

    @Test
    public void getRefundById_shouldReturnNotFound_whenRefundDoesNotExist() {

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondRefundNotFound(GATEWAY_ACCOUNT_ID, CHARGE_ID, "unknown-refund-id");

        getPaymentRefundByIdResponse(API_KEY, CHARGE_ID, REFUND_ID)
                .statusCode(404)
                .contentType(JSON)
                .body("code", is("P0700"))
                .body("description", is("Not found"));
    }

    @Test
    public void getRefundById_returns500_whenConnectorRespondsWithResponseOtherThan200Or404() {

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondRefundWithError(GATEWAY_ACCOUNT_ID, CHARGE_ID, REFUND_ID);

        getPaymentRefundByIdResponse(API_KEY, CHARGE_ID, REFUND_ID)
                .statusCode(500)
                .contentType(JSON)
                .body("code", is("P0798"))
                .body("description", is("Downstream system error"));
    }

    @Test
    public void getRefunds_shouldGetValidResponse() {

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);

        PaymentRefundJsonFixture refund_1 = new PaymentRefundJsonFixture(100L, CREATED_DATE, "100", "available", new ArrayList<>());
        PaymentRefundJsonFixture refund_2 = new PaymentRefundJsonFixture(300L, CREATED_DATE, "300", "pending", new ArrayList<>());

        connectorMock.respondWithGetAllRefunds(GATEWAY_ACCOUNT_ID, CHARGE_ID, refund_1, refund_2);

        getPaymentRefundsResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("_links.self.href", is(paymentRefundsLocationFor(CHARGE_ID)))
                .body("_links.payment.href", is(paymentLocationFor(CHARGE_ID)))
                .body("_embedded.refunds.size()", is(2))
                .body("_embedded.refunds[0].refund_id", is("100"))
                .body("_embedded.refunds[0].created_date", is(CREATED_DATE))
                .body("_embedded.refunds[0].amount", is(100))
                .body("_embedded.refunds[0].status", is("available"))
                .body("_embedded.refunds[0]._links.size()", is(2))
                .body("_embedded.refunds[0]._links.self.href", is(paymentRefundLocationFor(CHARGE_ID, "100")))
                .body("_embedded.refunds[0]._links.payment.href", is(paymentLocationFor(CHARGE_ID)))
                .body("_embedded.refunds[1].refund_id", is("300"))
                .body("_embedded.refunds[1].created_date", is(CREATED_DATE))
                .body("_embedded.refunds[1].amount", is(300))
                .body("_embedded.refunds[1].status", is("pending"))
                .body("_embedded.refunds[1]._links.size()", is(2))
                .body("_embedded.refunds[1]._links.self.href", is(paymentRefundLocationFor(CHARGE_ID, "300")))
                .body("_embedded.refunds[1]._links.payment.href", is(paymentLocationFor(CHARGE_ID)));
    }

    @Test
    public void getRefunds_shouldGetValidResponse_whenListReturnedIsEmpty() {

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithGetAllRefunds(GATEWAY_ACCOUNT_ID, CHARGE_ID);

        getPaymentRefundsResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("_links.self.href", is(paymentRefundsLocationFor(CHARGE_ID)))
                .body("_links.payment.href", is(paymentLocationFor(CHARGE_ID)))
                .body("_embedded.refunds.size()", is(0));
    }

    @Test
    public void getRefunds_shouldGetNonAuthorized_whenPublicAuthRespondsUnauthorised() {

        publicAuthMock.respondUnauthorised();

        getPaymentRefundsResponse(API_KEY, CHARGE_ID)
                .statusCode(401);
    }

    @Test
    public void createRefund_shouldGetAcceptedResponse() {

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        String refundStatus = "available";
        connectorMock.respondAccepted_whenCreateARefund(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, REFUND_ID, refundStatus, CREATED_DATE);

        String body = "{\"amount\":" + AMOUNT + "}";

        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .body(body)
                .post(format("/v1/payments/%s/refunds", CHARGE_ID))
                .then()
                .statusCode(ACCEPTED.getStatusCode())
                .contentType(JSON)
                .body("refund_id", is(REFUND_ID))
                .body("amount", is(AMOUNT))
                .body("status", is(refundStatus))
                .body("created_date", is(CREATED_DATE))
                .body("_links.self.href", is(paymentRefundLocationFor(CHARGE_ID, REFUND_ID)))
                .body("_links.payment.href", is(paymentLocationFor(CHARGE_ID)));
    }

    @Test
    public void createRefund_shouldGetNonAuthorized_whenPublicAuthRespondsUnauthorised() {

        publicAuthMock.respondUnauthorised();

        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .header(CONTENT_TYPE, APPLICATION_JSON)
                .body("{\"amount\": 1000}")
                .post(format("/v1/payments/%s/refunds", CHARGE_ID))
                .then()
                .statusCode(401);
    }

    private ValidatableResponse getPaymentRefundByIdResponse(String bearerToken, String paymentId, String refundId) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .get(format("/v1/payments/%s/refunds/%s", paymentId, refundId))
                .then();
    }

    private ValidatableResponse getPaymentRefundsResponse(String bearerToken, String paymentId) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .get(format("/v1/payments/%s/refunds", paymentId))
                .then();
    }

}
