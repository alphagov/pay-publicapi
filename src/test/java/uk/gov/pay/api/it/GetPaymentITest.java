package uk.gov.pay.api.it;

import com.jayway.jsonassert.JsonAssert;
import io.restassured.response.ValidatableResponse;
import org.junit.Test;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.model.SettlementSummary;
import uk.gov.pay.api.utils.ChargeEventBuilder;
import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpStatus.SC_NOT_ACCEPTABLE;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;
import static uk.gov.pay.api.utils.Urls.directDebitFrontendSecureUrl;
import static uk.gov.pay.api.utils.Urls.paymentLocationFor;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class GetPaymentITest extends PaymentResourceITestBase {

    private static final ZonedDateTime CAPTURED_DATE = ZonedDateTime.parse("2016-01-02T14:03:00Z");
    private static final ZonedDateTime CAPTURE_SUBMIT_TIME = ZonedDateTime.parse("2016-01-02T15:02:00Z");
    private static final SettlementSummary SETTLEMENT_SUMMARY = new SettlementSummary(ISO_INSTANT_MILLISECOND_PRECISION.format(CAPTURE_SUBMIT_TIME), DateTimeUtils.toLocalDateString(CAPTURED_DATE));
    private static final int AMOUNT = 9999999;
    private static final Long FEE = 5l;
    private static final Long NET_AMOUNT = 9999994l;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final PaymentState CREATED = new PaymentState("created", false, null, null);
    private static final PaymentState CAPTURED = new PaymentState("captured", false, null, null);
    public static final PaymentState AWAITING_CAPTURE_REQUEST = new PaymentState("submitted", false, null, null);
    private static final RefundSummary REFUND_SUMMARY = new RefundSummary("pending", 100L, 50L);
    private static final String PAYMENT_PROVIDER = "Sandbox";
    private static final String CARD_BRAND_LABEL = "Mastercard";
    private static final String RETURN_URL = "https://somewhere.gov.uk/rainbow/1";
    private static final String REFERENCE = "Some reference <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String EMAIL = "alice.111@mail.fake";
    private static final String GATEWAY_TRANSACTION_ID = "gateway-tx-123456";
    private static final String DESCRIPTION = "Some description <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String CREATED_DATE = ISO_INSTANT_MILLISECOND_PRECISION.format(ZonedDateTime.parse("2010-12-31T22:59:59.132012345Z"));
    private static final Map<String, String> PAYMENT_CREATED = new ChargeEventBuilder(CREATED, CREATED_DATE).build();
    private static final List<Map<String, String>> EVENTS = Collections.singletonList(PAYMENT_CREATED);
    private static final Address BILLING_ADDRESS = new Address("line1", "line2", "NR2 5 6EG", "city", "UK");
    private static final CardDetails CARD_DETAILS = new CardDetails("1234", "123456", "Mr. Payment", "12/19", BILLING_ADDRESS, CARD_BRAND_LABEL);

    @Test
    public void getPayment_ReturnsPayment() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);

        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CAPTURED, RETURN_URL,
                DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH, 
                true, CHARGE_TOKEN_ID, REFUND_SUMMARY, SETTLEMENT_SUMMARY,
                CARD_DETAILS, GATEWAY_TRANSACTION_ID);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("reference", is(REFERENCE))
                .body("email", is(EMAIL))
                .body("description", is(DESCRIPTION))
                .body("amount", is(AMOUNT))
                .body("state.status", is(CAPTURED.getStatus()))
                .body("return_url", is(RETURN_URL))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("card_brand", is(CARD_BRAND_LABEL))
                .body("created_date", is(CREATED_DATE))
                .body("language", is("en"))
                .body("delayed_capture", is(true))
                .body("provider_id", is(GATEWAY_TRANSACTION_ID))
                .body("refund_summary.status", is("pending"))
                .body("refund_summary.amount_submitted", is(50))
                .body("refund_summary.amount_available", is(100))
                .body("settlement_summary.capture_submit_time", is(ISO_INSTANT_MILLISECOND_PRECISION.format(CAPTURE_SUBMIT_TIME)))
                .body("settlement_summary.captured_date", is(DateTimeUtils.toLocalDateString(CAPTURED_DATE)))
                .body("card_details.card_brand", is(CARD_BRAND_LABEL))
                .body("card_details.cardholder_name", is(CARD_DETAILS.getCardHolderName()))
                .body("card_details.first_digits_card_number", is(CARD_DETAILS.getFirstDigitsCardNumber()))
                .body("card_details.last_digits_card_number", is(CARD_DETAILS.getLastDigitsCardNumber()))
                .body("card_details.expiry_date", is(CARD_DETAILS.getExpiryDate()))
                .body("card_details.billing_address.line1", is(CARD_DETAILS.getBillingAddress().get().getLine1()))
                .body("card_details.billing_address.line2", is(CARD_DETAILS.getBillingAddress().get().getLine2()))
                .body("card_details.billing_address.postcode", is(CARD_DETAILS.getBillingAddress().get().getPostcode()))
                .body("card_details.billing_address.country", is(CARD_DETAILS.getBillingAddress().get().getCountry()))
                .body("_links.self.href", is(paymentLocationFor(configuration.getBaseUrl(), CHARGE_ID)))
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
                .body("_links.refunds.method", is("GET"))
                .body("containsKey('corporate_card_surcharge')", is(false))
                .body("containsKey('total_amount')", is(false));
    }

    @Test
    public void getPayment_ReturnsDirectDebitPayment() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CREATED, RETURN_URL,
                DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, CHARGE_TOKEN_ID);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("reference", is(REFERENCE))
                .body("email", is(EMAIL))
                .body("description", is(DESCRIPTION))
                .body("amount", is(AMOUNT))
                .body("state.status", is(CREATED.getStatus()))
                .body("return_url", is(RETURN_URL))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("created_date", is(CREATED_DATE))
                .body("_links.self.href", is(paymentLocationFor(configuration.getBaseUrl(), CHARGE_ID)))
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
        CardDetails cardDetails = new CardDetails(null, null, "Mr. Payment", "12/19", BILLING_ADDRESS, CARD_BRAND_LABEL);

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CAPTURED, RETURN_URL,
                DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH, true, CHARGE_TOKEN_ID, REFUND_SUMMARY, SETTLEMENT_SUMMARY, cardDetails, GATEWAY_TRANSACTION_ID);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("card_details.first_digits_card_number", is(nullValue()))
                .body("card_details.last_digits_card_number", is(nullValue()));
    }

    @Test
    public void getPayment_ShouldNotIncludeCancelLinkIfPaymentCannotBeCancelled() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID,
                new PaymentState("success", true, null, null),
                RETURN_URL, DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH, false, CHARGE_TOKEN_ID, REFUND_SUMMARY,
                null, CARD_DETAILS, GATEWAY_TRANSACTION_ID);

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
                new SettlementSummary(null, null), CARD_DETAILS, GATEWAY_TRANSACTION_ID);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .root("settlement_summary")
                .body("containsKey('capture_submit_time')", is(false))
                .body("containsKey('captured_date')", is(false));
    }

    @Test
    public void getPayment_BillingAddressShouldBeNullWhenNotPresentInConnectorResponse() {
        CardDetails cardDetails = new CardDetails("1234",
                "123456",
                "Mr. Payment",
                "12/19",
                null,
                CARD_BRAND_LABEL);
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CAPTURED, RETURN_URL,
                DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH,
                true, CHARGE_TOKEN_ID, REFUND_SUMMARY, SETTLEMENT_SUMMARY, cardDetails, GATEWAY_TRANSACTION_ID);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("card_details", hasKey("billing_address"))
                .body("card_details.billing_address", is(nullValue()))
                .body("payment_id", is(CHARGE_ID));
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
                .assertThat("$.*", hasSize(2))
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
                .assertThat("$.*", hasSize(2))
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
                .body("events[0].updated", is("2010-12-31T22:59:59.132Z"))
                .body("events[0]._links.payment_url.href", is(paymentLocationFor(configuration.getBaseUrl(), CHARGE_ID)));
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
                .assertThat("$.*", hasSize(2))
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
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0398"))
                .assertThat("$.description", is("Downstream system error"));
    }

    @Test
    public void getPayment_ReturnsPaymentWithCorporateCardSurcharge() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CAPTURED, RETURN_URL,
                DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH,
                true, CHARGE_TOKEN_ID, REFUND_SUMMARY, SETTLEMENT_SUMMARY, CARD_DETAILS, 250L, AMOUNT + 250L, GATEWAY_TRANSACTION_ID, FEE, NET_AMOUNT);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("corporate_card_surcharge", is(250))
                .body("total_amount", is(AMOUNT + 250));
    }

    @Test
    public void getPayment_ReturnsPaymentWithFeeAndNetAmount() {

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, CAPTURED, RETURN_URL,
                DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH,
                true, CHARGE_TOKEN_ID, REFUND_SUMMARY, SETTLEMENT_SUMMARY, CARD_DETAILS, null, null, GATEWAY_TRANSACTION_ID,
                FEE, NET_AMOUNT);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("fee", is(FEE.intValue()))
                .body("net_amount", is(NET_AMOUNT.intValue()))
                .body("amount", is(AMOUNT));
    }

    @Test
    public void getPayment_ReturnsPaymentWithCaptureUrl() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMock.respondWithChargeFound(AMOUNT, GATEWAY_ACCOUNT_ID, CHARGE_ID, AWAITING_CAPTURE_REQUEST, RETURN_URL,
                DESCRIPTION, REFERENCE, EMAIL, PAYMENT_PROVIDER, CREATED_DATE, SupportedLanguage.ENGLISH,
                true, CHARGE_TOKEN_ID, REFUND_SUMMARY, SETTLEMENT_SUMMARY, CARD_DETAILS,
                0L, 0L, GATEWAY_TRANSACTION_ID, FEE, NET_AMOUNT);

        getPaymentResponse(API_KEY, CHARGE_ID)
                .statusCode(200)
                .contentType(JSON)
                .body("_links.capture.href", is(paymentLocationFor(configuration.getBaseUrl(), CHARGE_ID) + "/capture"))
                .body("_links.capture.method", is("POST"));
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
