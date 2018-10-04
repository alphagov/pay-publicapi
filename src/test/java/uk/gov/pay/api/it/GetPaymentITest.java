package uk.gov.pay.api.it;

import com.jayway.jsonassert.JsonAssert;
import com.jayway.restassured.response.ValidatableResponse;
import org.junit.Test;
import uk.gov.pay.api.model.generated.Address;
import uk.gov.pay.api.model.generated.CardDetails;
import uk.gov.pay.api.model.generated.PaymentState;
import uk.gov.pay.api.model.generated.RefundSummary;
import uk.gov.pay.api.model.generated.SettlementSummary;
import uk.gov.pay.api.utils.ChargeEventBuilder;
import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpStatus.SC_NOT_ACCEPTABLE;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;
import static uk.gov.pay.api.utils.DateTimeUtils.toLocalDateString;
import static uk.gov.pay.api.utils.DateTimeUtils.toUTCDateString;

public class GetPaymentITest extends PaymentResourceITestBase {

    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final ZonedDateTime CAPTURED_DATE = ZonedDateTime.parse("2016-01-02T14:03:00Z");
    private static final ZonedDateTime CAPTURE_SUBMIT_TIME = ZonedDateTime.parse("2016-01-02T15:02:00Z");
    private static final SettlementSummary SETTLEMENT_SUMMARY = 
            new SettlementSummary().capturedDate(toLocalDateString(CAPTURED_DATE)).captureSubmitTime(toUTCDateString(CAPTURE_SUBMIT_TIME));
    private static final int AMOUNT = 9999999;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final PaymentState CREATED = new PaymentState().status("created").finished(false);
    private static final PaymentState CAPTURED = new PaymentState().status("captured").finished(false);
    private static final RefundSummary REFUND_SUMMARY = new RefundSummary().status("pending").amountAvailable(100L).amountSubmitted(50L);
    private static final String PAYMENT_PROVIDER = "Sandbox";
    private static final String CARD_BRAND_LABEL = "Mastercard";
    private static final String RETURN_URL = "https://somewhere.gov.uk/rainbow/1";
    private static final String REFERENCE = "Some reference <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String EMAIL = "alice.111@mail.fake";
    private static final String DESCRIPTION = "Some description <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String CREATED_DATE = toUTCDateString(TIMESTAMP);
    private static final Map<String, String> PAYMENT_CREATED = new ChargeEventBuilder(CREATED, CREATED_DATE).build();
    private static final List<Map<String, String>> EVENTS = Collections.singletonList(PAYMENT_CREATED);
    private static final Address BILLING_ADDRESS = new Address().line1("line1").line2("line2").postcode("NR2 5 6EG").city("city").country("UK");
    private static final CardDetails CARD_DETAILS = new CardDetails()
            .lastDigitsCardNumber("1234")
            .firstDigitsCardNumber("123456")
            .cardholderName("Mr. Payment")
            .expiryDate("12/19")
            .billingAddress(BILLING_ADDRESS)
            .cardBrand(CARD_BRAND_LABEL);

    @Test
    public void getPayment_ReturnsPayment() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CAPTURED, RETURN_URL,
                DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH, true, CHARGE_TOKEN_ID, REFUND_SUMMARY, SETTLEMENT_SUMMARY,
                CARD_DETAILS);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment.payment_id", is(CHARGE_ID))
                .body("payment.reference", is(REFERENCE))
                .body("payment.email", is(EMAIL))
                .body("payment.description", is(DESCRIPTION))
                .body("payment.amount", is(AMOUNT))
                .body("payment.state.status", is(CAPTURED.getStatus()))
                .body("payment.return_url", is(RETURN_URL))
                .body("payment.payment_provider", is(PAYMENT_PROVIDER))
                .body("payment.card_brand", is(CARD_BRAND_LABEL))
                .body("payment.created_date", is(CREATED_DATE))
                .body("payment.language", is("en"))
                .body("payment.delayed_capture", is(true))
                .body("payment.refund_summary.status", is("pending"))
                .body("payment.refund_summary.amount_submitted", is(50))
                .body("payment.refund_summary.amount_available", is(100))
                .body("payment.settlement_summary.capture_submit_time", is(toUTCDateString(CAPTURE_SUBMIT_TIME)))
                .body("payment.settlement_summary.captured_date", is(toLocalDateString(CAPTURED_DATE)))
                .body("payment.card_details.card_brand", is(CARD_BRAND_LABEL))
                .body("payment.card_details.cardholder_name", is(CARD_DETAILS.getCardholderName()))
                .body("payment.card_details.first_digits_card_number", is(CARD_DETAILS.getFirstDigitsCardNumber()))
                .body("payment.card_details.last_digits_card_number", is(CARD_DETAILS.getLastDigitsCardNumber()))
                .body("payment.card_details.expiry_date", is(CARD_DETAILS.getExpiryDate()))
                .body("payment.card_details.billing_address.line1", is(CARD_DETAILS.getBillingAddress().getLine1()))
                .body("payment.card_details.billing_address.line2", is(CARD_DETAILS.getBillingAddress().getLine2()))
                .body("payment.card_details.billing_address.postcode", is(CARD_DETAILS.getBillingAddress().getPostcode()))
                .body("payment.card_details.billing_address.country", is(CARD_DETAILS.getBillingAddress().getCountry()))
                .body("_links.self.href", is(paymentLocationFor(CHARGE_ID)))
                .body("_links.self.method", is("GET"))
                .body("_links.events.href", is(paymentEventsLocationFor(CHARGE_ID)))
                .body("_links.events.method", is("GET"))
                .body("_links.next_url.href", is(frontendUrlFor(CARD) + CHARGE_ID))
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
                .body("_links.refunds.method", is("GET"));
    }

    @Test
    public void getPayment_ReturnsDirectDebitPayment() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CREATED, RETURN_URL,
                DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, CHARGE_TOKEN_ID);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment.payment_id", is(CHARGE_ID))
                .body("payment.reference", is(REFERENCE))
                .body("payment.email", is(EMAIL))
                .body("payment.description", is(DESCRIPTION))
                .body("payment.amount", is(AMOUNT))
                .body("payment.state.status", is(CREATED.getStatus()))
                .body("payment.return_url", is(RETURN_URL))
                .body("payment.payment_provider", is(PAYMENT_PROVIDER))
                .body("payment.created_date", is(CREATED_DATE))
                .body("_links.self.href", is(paymentLocationFor(CHARGE_ID)))
                .body("_links.self.method", is("GET"))
                .body("_links.next_url.href", is(directDebitFrontendSecureUrl() + CHARGE_ID))
                .body("_links.next_url.method", is("GET"))
                .body("_links.next_url_post.href", is(directDebitFrontendSecureUrl()))
                .body("_links.next_url_post.method", is("POST"))
                .body("_links.next_url_post.type", is("application/x-www-form-urlencoded"))
                .body("_links.next_url_post.params.chargeTokenId", is(CHARGE_TOKEN_ID))
                .body("_links.cancel", is(nullValue()))
                .body("_links.events", is(nullValue()))
                .body("_links.refunds", is(nullValue()));
    }

    @Test
    public void getPayment_DoesNotReturnCardDigits_IfNotPresentInCardDetails() {
        CardDetails cardDetails = new CardDetails().cardholderName("Mr. Payment").expiryDate("12/19").billingAddress(BILLING_ADDRESS).cardBrand(CARD_BRAND_LABEL);

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CAPTURED, RETURN_URL,
                DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH, true, CHARGE_TOKEN_ID, REFUND_SUMMARY, SETTLEMENT_SUMMARY, cardDetails);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment.payment_id", is(CHARGE_ID))
                .body("payment.card_details.first_digits_card_number", is(nullValue()))
                .body("payment.card_details.last_digits_card_number", is(nullValue()));
    }
    
    
    @Test
    public void getPayment_ShouldNotIncludeCancelLinkIfPaymentCannotBeCancelled() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID,
                new PaymentState().status("success").finished(true),
                RETURN_URL, DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH, false, CHARGE_TOKEN_ID, REFUND_SUMMARY,
                null, CARD_DETAILS);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("_links.cancel", is(nullValue()));
    }

    @Test
    public void getPayment_ShouldNotIncludeSettlementFieldsIfNull() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID,
                CREATED, RETURN_URL, DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH, false, CHARGE_TOKEN_ID, REFUND_SUMMARY,
                new SettlementSummary(), CARD_DETAILS);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .root("payment.settlement_summary")
                .body("containsKey('payment.settlement_summary.capture_submit_time')", is(false))
                .body("containsKey('payment.settlement_summary.captured_date')", is(false));
    }

    @Test
    public void getPayment_Returns401_WhenUnauthorised() {
        publicAuthMock.respondUnauthorised();

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(401);
    }

    @Test
    public void getPayment_returns404_whenConnectorRespondsWith404() throws IOException {
        String paymentId = "ds2af2afd3df112";
        String errorMessage = "backend-error-message";
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondChargeNotFound(GATEWAY_ACCOUNT_ID, paymentId, errorMessage);

        InputStream body = getPaymentResponse(API_KEY, paymentId)
                .statusCode(404)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
//                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0200"))
                .assertThat("$.description", is("Not found"));
    }

    @Test
    public void getPayment_returns500_whenConnectorRespondsWithResponseOtherThan200Or404() throws IOException {
        String paymentId = "ds2af2afd3df112";
        String errorMessage = "backend-error-message";
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWhenGetCharge(GATEWAY_ACCOUNT_ID, paymentId, errorMessage, SC_NOT_ACCEPTABLE);

        InputStream body = getPaymentResponse(API_KEY, paymentId)
                .statusCode(500)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
//                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0298"))
                .assertThat("$.description", is("Downstream system error"));
    }

    @Test
    public void getPaymentEvents_ReturnsPaymentEvents() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeEventsFound(GATEWAY_ACCOUNT_ID, CHARGE_ID, EVENTS);

        getPaymentEventsResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("_links.self.href", is(paymentEventsLocationFor(CHARGE_ID)))
                .body("events", hasSize(1))
                .body("events[0].payment_id", is(CHARGE_ID))
                .body("events[0].state.status", is(CREATED.getStatus()))
                .body("events[0].updated", is("2016-01-01T12:00:00Z"))
                .body("events[0]._links.payment_url.href", is(paymentLocationFor(CHARGE_ID)));
    }

    @Test
    public void getPaymentEvents_Returns401_WhenUnauthorised() {
        publicAuthMock.respondUnauthorised();

        getPaymentEventsResponse(API_KEY, CHARGE_ID)
                .statusCode(401);
    }

    @Test
    public void getPaymentEvents_returns404_whenConnectorRespondsWith404() throws IOException {
        String paymentId = "ds2af2afd3df112";
        String errorMessage = "backend-error-message";
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondChargeEventsNotFound(GATEWAY_ACCOUNT_ID, paymentId, errorMessage);

        InputStream body = getPaymentEventsResponse(API_KEY, paymentId)
                .statusCode(404)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
//                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0300"))
                .assertThat("$.description", is("Not found"));
    }

    @Test
    public void getPaymentEvents_returns500_whenConnectorRespondsWithResponseOtherThan200Or404() throws IOException {
        String paymentId = "ds2af2afd3df112";
        String errorMessage = "backend-error-message";
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWhenGetChargeEvents(GATEWAY_ACCOUNT_ID, paymentId, errorMessage, SC_NOT_ACCEPTABLE);

        InputStream body = getPaymentEventsResponse(API_KEY, paymentId)
                .statusCode(500)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
//                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0398"))
                .assertThat("$.description", is("Downstream system error"));
    }

    private ValidatableResponse getPaymentResponse(String bearerToken, String paymentId) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .get(PAYMENTS_PATH + paymentId)
                .then();
    }

    private ValidatableResponse getPaymentEventsResponse(String bearerToken, String paymentId) {
        return given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .get(String.format("/v1/payments/%s/events", paymentId))
                .then();
    }
}
