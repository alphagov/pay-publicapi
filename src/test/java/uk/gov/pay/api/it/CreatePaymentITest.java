package uk.gov.pay.api.it;

import com.jayway.jsonassert.JsonAssert;
import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Test;
import uk.gov.pay.api.model.generated.Address;
import uk.gov.pay.api.model.generated.CardDetails;
import uk.gov.pay.api.model.generated.PaymentState;
import uk.gov.pay.api.model.generated.RefundSummary;
import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.commons.model.SupportedLanguage;

import javax.ws.rs.core.HttpHeaders;
import java.io.InputStream;
import java.time.ZonedDateTime;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;

public class CreatePaymentITest extends PaymentResourceITestBase {

    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final int AMOUNT = 9999999;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final PaymentState CREATED = new PaymentState().code("created").finished(false);
    private static final RefundSummary REFUND_SUMMARY = new RefundSummary().status("pending").amountAvailable(100L).amountSubmitted(50L);
    private static final String PAYMENT_PROVIDER = "Sandbox";
    private static final String CARD_BRAND_LABEL = "Mastercard";
    private static final String RETURN_URL = "https://somewhere.gov.uk/rainbow/1";
    private static final String REFERENCE = "Some reference <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String EMAIL = "alice.111@mail.fake";
    private static final String DESCRIPTION = "Some description <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String CREATED_DATE = DateTimeUtils.toUTCDateString(TIMESTAMP);
    private static final Address BILLING_ADDRESS = new Address().line1("line1").line2("line2").postcode("NR2 5 6EG").city("city").country("UK");
    private static final CardDetails CARD_DETAILS = 
            new CardDetails().lastDigitsCardNumber("1234").firstDigitsCardNumber("123456").cardholderName("Mr. Payment").expiryDate("12/19").billingAddress(BILLING_ADDRESS).cardBrand(CARD_BRAND_LABEL);
    private static final String SUCCESS_PAYLOAD = paymentPayload(AMOUNT, RETURN_URL, DESCRIPTION, REFERENCE, EMAIL);

    @Test
    public void createCardPayment() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        connectorMock.respondOk_whenCreateCharge(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CHARGE_TOKEN_ID,
                CREATED, RETURN_URL, DESCRIPTION, REFERENCE, null, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH, true, REFUND_SUMMARY,
                null, CARD_DETAILS);

        String responseBody = postPaymentResponse(API_KEY, SUCCESS_PAYLOAD)
                .statusCode(201)
                .contentType(JSON)
                .header(HttpHeaders.LOCATION, is(paymentLocationFor(CHARGE_ID)))
                .body("payment.payment_id", is(CHARGE_ID))
                .body("payment.amount", is(9999999))
                .body("payment.reference", is(REFERENCE))
                .body("payment.email", nullValue())
                .body("payment.description", is(DESCRIPTION))
                .body("payment.state.status", is(CREATED.getStatus()))
                .body("payment.return_url", is(RETURN_URL))
                .body("payment.payment_provider", is(PAYMENT_PROVIDER))
                .body("payment.card_brand", is(CARD_BRAND_LABEL))
                .body("payment.created_date", is(CREATED_DATE))
                .body("payment.delayed_capture", is(true))
                .body("payment.refund_summary.status", is("pending"))
                .body("payment.refund_summary.amount_submitted", is(50))
                .body("payment.refund_summary.amount_available", is(100))
                .body("_links.self.href", is(paymentLocationFor(CHARGE_ID)))
                .body("_links.self.method", is("GET"))
                .body("_links.next_url.href", is(frontendUrlFor(CARD) + CHARGE_TOKEN_ID))
                .body("_links.next_url.method", is("GET"))
                .body("_links.next_url_post.href", is(frontendUrlFor(CARD)))
                .body("_links.next_url_post.method", is("POST"))
                .body("_links.next_url_post.type", is("application/x-www-form-urlencoded"))
                .body("_links.next_url_post.params.chargeTokenId", is(CHARGE_TOKEN_ID))
                .body("_links.events.href", is(paymentEventsLocationFor(CHARGE_ID)))
                .body("_links.events.method", is("GET"))
                .body("_links.cancel.href", is(paymentCancelLocationFor(CHARGE_ID)))
                .body("_links.cancel.method", is("POST"))
                .body("_links.refunds.href", is(paymentRefundsLocationFor(CHARGE_ID)))
                .body("_links.refunds.method", is("GET"))
                .extract().body().asString();

        JsonAssert.with(responseBody)
                .assertNotDefined("_links.self.type")
                .assertNotDefined("_links.self.params")
                .assertNotDefined("_links.next_url.type")
                .assertNotDefined("_links.next_url.params")
                .assertNotDefined("_links.events.type")
                .assertNotDefined("_links.events.params");

        connectorMock.verifyCreateChargeConnectorRequest(AMOUNT, GATEWAY_ACCOUNT_ID, RETURN_URL, DESCRIPTION, REFERENCE);
    }

    @Test
    public void createPayment_withMinimumAmount() {
        int minimumAmount = 1;

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenCreateCharge(minimumAmount, GATEWAY_ACCOUNT_ID, CHARGE_ID, CHARGE_TOKEN_ID,
                CREATED, RETURN_URL, DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH,
                false, REFUND_SUMMARY, null, CARD_DETAILS);

        postPaymentResponse(API_KEY, paymentPayload(minimumAmount, RETURN_URL, DESCRIPTION, REFERENCE, EMAIL))
                .statusCode(201)
                .contentType(JSON)
                .body("payment.payment_id", is(CHARGE_ID))
                .body("payment.amount", is(minimumAmount))
                .body("payment.reference", is(REFERENCE))
                .body("payment.email", is(EMAIL))
                .body("payment.description", is(DESCRIPTION))
                .body("payment.return_url", is(RETURN_URL))
                .body("payment.payment_provider", is(PAYMENT_PROVIDER))
                .body("payment.created_date", is(CREATED_DATE));

        connectorMock.verifyCreateChargeConnectorRequest(minimumAmount, GATEWAY_ACCOUNT_ID, RETURN_URL, DESCRIPTION, REFERENCE);
    }

    @Test
    public void createPayment_withAllFieldsUpToMaxLengthBoundaries_shouldBeAccepted() {
        int amount = 10000000;
        String reference = randomAlphanumeric(255);
        String description = randomAlphanumeric(255);
        String email = randomAlphanumeric(254) + "@mail.fake";
        String return_url = "https://govdemopay.gov.uk?data=" + randomAlphanumeric(1969);

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenCreateCharge(amount, GATEWAY_ACCOUNT_ID, CHARGE_ID, CHARGE_TOKEN_ID,
                CREATED, return_url, description, reference, email, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH,
                false, REFUND_SUMMARY, null, CARD_DETAILS);

        String body = new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("email", email)
                .add("card_brand", CARD_BRAND_LABEL)
                .add("description", description)
                .add("return_url", return_url)
                .build();

        postPaymentResponse(API_KEY, body)
                .statusCode(201)
                .contentType(JSON)
                .body("payment.payment_id", is(CHARGE_ID))
                .body("payment.amount", is(amount))
                .body("payment.reference", is(reference))
                .body("payment.email", is(email))
                .body("payment.description", is(description))
                .body("payment.return_url", is(return_url))
                .body("payment.payment_provider", is(PAYMENT_PROVIDER))
                .body("payment.card_brand", is(CARD_BRAND_LABEL))
                .body("payment.created_date", is(CREATED_DATE));
    }

    @Test
    public void createPayment_responseWith500_whenConnectorResponseIsAnUnrecognisedError() throws Exception {
        String gatewayAccountId = "1234567";
        String errorMessage = "something went wrong";

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, gatewayAccountId);

        connectorMock.respondBadRequest_whenCreateCharge(AMOUNT, gatewayAccountId, errorMessage, RETURN_URL, DESCRIPTION, REFERENCE);

        InputStream body = postPaymentResponse(API_KEY, SUCCESS_PAYLOAD)
                .statusCode(500)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
//                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0198"))
                .assertThat("$.description", is("Downstream system error"));

        connectorMock.verifyCreateChargeConnectorRequest(AMOUNT, gatewayAccountId, RETURN_URL, DESCRIPTION, REFERENCE);
    }

    @Test
    public void createPayment_responseWith500_whenTokenForGatewayAccountIsValidButConnectorResponseIsNotFound() {
        String notFoundGatewayAccountId = "9876545";
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, notFoundGatewayAccountId);

        connectorMock.respondNotFound_whenCreateCharge(AMOUNT, notFoundGatewayAccountId, RETURN_URL, DESCRIPTION, REFERENCE);

        postPaymentResponse(API_KEY, SUCCESS_PAYLOAD)
                .statusCode(500)
                .contentType(JSON)
                .body("code", is("P0199"))
                .body("description", is("There is an error with this account. Please contact support"));

        connectorMock.verifyCreateChargeConnectorRequest(AMOUNT, notFoundGatewayAccountId, RETURN_URL, DESCRIPTION, REFERENCE);
    }

    @Test
    public void createPayment_Returns401_WhenUnauthorised() {
        publicAuthMock.respondUnauthorised();

        postPaymentResponse(API_KEY, SUCCESS_PAYLOAD)
                .statusCode(401);
    }

    @Test
    public void createPayment_Returns_WhenPublicAuthInaccessible() {
        publicAuthMock.respondWithError();

        postPaymentResponse(API_KEY, SUCCESS_PAYLOAD)
                .statusCode(503);
    }

    private static String paymentPayload(long amount, String returnUrl, String description, String reference, String email) {
        return new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("email", email)
                .add("description", description)
                .add("return_url", returnUrl)
                .build();
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
