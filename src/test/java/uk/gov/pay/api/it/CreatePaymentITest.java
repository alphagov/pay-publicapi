package uk.gov.pay.api.it;

import com.jayway.jsonassert.JsonAssert;
import io.restassured.response.ValidatableResponse;
import org.junit.Test;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.api.utils.PublicAuthMockClient;
import uk.gov.pay.api.utils.mocks.ConnectorMockClient;
import uk.gov.pay.api.utils.mocks.CreateChargeRequestParams;
import uk.gov.pay.commons.model.SupportedLanguage;

import javax.ws.rs.core.HttpHeaders;
import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;
import static uk.gov.pay.api.utils.Urls.paymentLocationFor;
import static uk.gov.pay.api.utils.mocks.ChargeResponseFromConnector.ChargeResponseFromConnectorBuilder.aCreateOrGetChargeResponseFromConnector;
import static uk.gov.pay.api.utils.mocks.CreateChargeRequestParams.CreateChargeRequestParamsBuilder.aCreateChargeRequestParams;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class CreatePaymentITest extends PaymentResourceITestBase {

    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final int AMOUNT = 9999999;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final PaymentState CREATED = new PaymentState("created", false, null, null);
    private static final RefundSummary REFUND_SUMMARY = new RefundSummary("pending", 100L, 50L);
    private static final String PAYMENT_PROVIDER = "Sandbox";
    private static final String CARD_BRAND_LABEL = "Mastercard";
    private static final String RETURN_URL = "https://somewhere.gov.uk/rainbow/1";
    private static final String REFERENCE = "Some reference <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String DESCRIPTION = "Some description <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String CREATED_DATE = ISO_INSTANT_MILLISECOND_PRECISION.format(TIMESTAMP);
    private static final Address BILLING_ADDRESS = new Address("line1", "line2", "NR2 5 6EG", "city", "UK");
    private static final CardDetails CARD_DETAILS = new CardDetails("1234", "123456", "Mr. Payment", "12/19", BILLING_ADDRESS, CARD_BRAND_LABEL);
    private static final String SUCCESS_PAYLOAD = paymentPayload(aCreateChargeRequestParams()
            .withAmount(AMOUNT)
            .withDescription(DESCRIPTION)
            .withReference(REFERENCE)
            .withReturnUrl(RETURN_URL).build());
    private static final String GATEWAY_TRANSACTION_ID = "gateway-tx-123456";

    private ConnectorMockClient connectorMockClient = new ConnectorMockClient(connectorMock);
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);

    @Test
    public void createCardPaymentWithEmptyMetadataDoesNotStoreMetadata() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        String payload = new JsonStringBuilder()
                .add("amount", 100)
                .add("reference", REFERENCE)
                .add("description", DESCRIPTION)
                .add("return_url", RETURN_URL)
                .add("metadata", Map.of())
                .build();

        connectorMockClient.respondOk_whenCreateCharge(GATEWAY_ACCOUNT_ID, aCreateChargeRequestParams()
                .withAmount(100)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withReturnUrl(RETURN_URL)
                .build());

        postPaymentResponse(payload)
                .statusCode(201)
                .contentType(JSON).log().body()
                .body("$", not(hasKey("metadata")));
    }

    @Test
    public void createCardPaymentWithMetadata() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        CreateChargeRequestParams createChargeRequestParams = aCreateChargeRequestParams()
                .withAmount(100)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withReturnUrl(RETURN_URL)
                .withMetadata(Map.of("reconciled", true, "ledger_code", 123, "fuh", "fuh you"))
                .build();
        connectorMockClient.respondOk_whenCreateCharge(GATEWAY_ACCOUNT_ID, createChargeRequestParams);

        postPaymentResponse(paymentPayload(createChargeRequestParams))
                .statusCode(201)
                .contentType(JSON)
                .body("metadata.reconciled", is(true))
                .body("metadata.ledger_code", is(123))
                .body("metadata.fuh", is("fuh you"));

        connectorMockClient.verifyCreateChargeConnectorRequest(GATEWAY_ACCOUNT_ID, createChargeRequestParams);
    }

    @Test
    public void createCardPaymentWithPrefilledCardholderDetails() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        CreateChargeRequestParams createChargeRequestParams = aCreateChargeRequestParams()
                .withAmount(100)
                .withDescription("description")
                .withReference("reference")
                .withReturnUrl(RETURN_URL)
                .withEmail("j.bogs@example.org")
                .witCardHolderName("J. Bogs")
                .withAddressLine1("address line 1")
                .withAddressLine2("address line 2")
                .withAddressPostcode("AB1 CD2")
                .withAddressCity("address city")
                .withAddressCountry("GB")
                .build();
        connectorMockClient.respondOk_whenCreateCharge(GATEWAY_ACCOUNT_ID, createChargeRequestParams);

        postPaymentResponse(paymentPayload(createChargeRequestParams))
                .statusCode(201)
                .contentType(JSON)
                .body("email", is("j.bogs@example.org"))
                .body("card_details.cardholder_name", is("J. Bogs"))
                .body("card_details.billing_address.line1", is("address line 1"))
                .body("card_details.billing_address.line2", is("address line 2"))
                .body("card_details.billing_address.postcode", is("AB1 CD2"))
                .body("card_details.billing_address.city", is("address city"))
                .body("card_details.billing_address.country", is("GB"));
        connectorMockClient.verifyCreateChargeConnectorRequest(GATEWAY_ACCOUNT_ID, createChargeRequestParams);
    }

    @Test
    public void createCardPaymentShouldRespondWith400ErrorWhenNumericFieldInPrefilledCardholderDetails() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        String payload = new JsonStringBuilder()
                .add("amount", 1000)
                .add("reference", "reference")
                .add("description", "description")
                .add("return_url", RETURN_URL)
                .addToNestedMap("line1", 123, "prefilled_cardholder_details", "billing_address")
                .build();
        postPaymentResponse(payload)
                .statusCode(400)
                .contentType(JSON)
                .body("code", is("P0102"))
                .body("field", is("line1"))
                .body("description", is("Invalid attribute value: line1. Field must be a string"));
    }

    @Test
    public void createCardPaymentWithSomePrefilledCardholderDetails() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        CreateChargeRequestParams createChargeRequestParams = aCreateChargeRequestParams()
                .withAmount(100)
                .withDescription("description")
                .withReference("reference")
                .withReturnUrl(RETURN_URL)
                .witCardHolderName("J. Bogs")
                .withAddressLine1("address line 1")
                .withAddressCity("address city")
                .withAddressCountry("GB")
                .build();
        connectorMockClient.respondOk_whenCreateCharge(GATEWAY_ACCOUNT_ID, createChargeRequestParams);

        postPaymentResponse(paymentPayload(createChargeRequestParams))
                .statusCode(201)
                .contentType(JSON)
                .body("email", is(nullValue()))
                .body("card_details.cardholder_name", is("J. Bogs"))
                .body("card_details.billing_address.line1", is("address line 1"))
                .body("card_details.billing_address.line2", is(nullValue()))
                .body("card_details.billing_address.postcode", is(nullValue()))
                .body("card_details.billing_address.city", is("address city"))
                .body("card_details.billing_address.country", is("GB"));
        connectorMockClient.verifyCreateChargeConnectorRequest(GATEWAY_ACCOUNT_ID, createChargeRequestParams);
    }

    @Test
    public void createCardPayment() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        connectorMockClient.respondOk_whenCreateCharge(CHARGE_TOKEN_ID, GATEWAY_ACCOUNT_ID, aCreateOrGetChargeResponseFromConnector()
                .withAmount(AMOUNT)
                .withChargeId(CHARGE_ID)
                .withState(CREATED)
                .withReturnUrl(RETURN_URL)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withPaymentProvider(PAYMENT_PROVIDER)
                .withGatewayTransactionId(GATEWAY_TRANSACTION_ID)
                .withCreatedDate(CREATED_DATE)
                .withLanguage(SupportedLanguage.ENGLISH)
                .withDelayedCapture(true)
                .withRefundSummary(REFUND_SUMMARY)
                .withCardDetails(CARD_DETAILS)
                .build());

        String responseBody = postPaymentResponse(SUCCESS_PAYLOAD)
                .statusCode(201)
                .contentType(JSON)
                .header(HttpHeaders.LOCATION, is(paymentLocationFor(configuration.getBaseUrl(), CHARGE_ID)))
                .body("payment_id", is(CHARGE_ID))
                .body("amount", is(9999999))
                .body("reference", is(REFERENCE))
                .body("email", nullValue())
                .body("description", is(DESCRIPTION))
                .body("state.status", is(CREATED.getStatus()))
                .body("return_url", is(RETURN_URL))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("card_brand", is(CARD_BRAND_LABEL))
                .body("created_date", is(CREATED_DATE))
                .body("delayed_capture", is(true))
                .body("provider_id", is(GATEWAY_TRANSACTION_ID))
                .body("refund_summary.status", is("pending"))
                .body("refund_summary.amount_submitted", is(50))
                .body("refund_summary.amount_available", is(100))
                .body("_links.self.href", is(paymentLocationFor(configuration.getBaseUrl(), CHARGE_ID)))
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
                .body("metadata", nullValue())
                .extract().body().asString();

        JsonAssert.with(responseBody)
                .assertNotDefined("_links.self.type")
                .assertNotDefined("_links.self.params")
                .assertNotDefined("_links.next_url.type")
                .assertNotDefined("_links.next_url.params")
                .assertNotDefined("_links.events.type")
                .assertNotDefined("_links.events.params");

        connectorMockClient.verifyCreateChargeConnectorRequest(GATEWAY_ACCOUNT_ID, SUCCESS_PAYLOAD);
    }

    @Test
    public void createPayment_withMinimumAmount() {
        int minimumAmount = 1;

        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);

        connectorMockClient.respondOk_whenCreateCharge(CHARGE_TOKEN_ID, GATEWAY_ACCOUNT_ID, aCreateOrGetChargeResponseFromConnector()
                .withAmount(minimumAmount)
                .withChargeId(CHARGE_ID)
                .withState(CREATED)
                .withReturnUrl(RETURN_URL)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withPaymentProvider(PAYMENT_PROVIDER)
                .withGatewayTransactionId(GATEWAY_TRANSACTION_ID)
                .withCreatedDate(CREATED_DATE)
                .withLanguage(SupportedLanguage.ENGLISH)
                .withDelayedCapture(false)
                .withRefundSummary(REFUND_SUMMARY)
                .withCardDetails(CARD_DETAILS)
                .build());

        CreateChargeRequestParams params = aCreateChargeRequestParams()
                .withAmount(minimumAmount)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withReturnUrl(RETURN_URL)
                .build();

        postPaymentResponse(paymentPayload(params))
                .statusCode(201)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("amount", is(minimumAmount))
                .body("reference", is(REFERENCE))
                .body("description", is(DESCRIPTION))
                .body("return_url", is(RETURN_URL))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("created_date", is(CREATED_DATE));

        connectorMockClient.verifyCreateChargeConnectorRequest(GATEWAY_ACCOUNT_ID, params);
    }

    @Test
    public void createPayment_withAllFieldsUpToMaxLengthBoundaries_shouldBeAccepted() {
        int amount = 10000000;
        String reference = randomAlphanumeric(255);
        String description = randomAlphanumeric(255);
        String email = randomAlphanumeric(242) + "@example.org";
        String return_url = "https://govdemopay.gov.uk?data=" + randomAlphanumeric(1969);

        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);

        connectorMockClient.respondOk_whenCreateCharge(CHARGE_TOKEN_ID, GATEWAY_ACCOUNT_ID, aCreateOrGetChargeResponseFromConnector()
                .withAmount(amount)
                .withChargeId(CHARGE_ID)
                .withState(CREATED)
                .withReturnUrl(return_url)
                .withDescription(description)
                .withReference(reference)
                .withEmail(email)
                .withPaymentProvider(PAYMENT_PROVIDER)
                .withGatewayTransactionId(GATEWAY_TRANSACTION_ID)
                .withCreatedDate(CREATED_DATE)
                .withLanguage(SupportedLanguage.ENGLISH)
                .withDelayedCapture(false)
                .withRefundSummary(REFUND_SUMMARY)
                .withCardDetails(CARD_DETAILS)
                .build());

        String body = new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("email", email)
                .add("card_brand", CARD_BRAND_LABEL)
                .add("description", description)
                .add("return_url", return_url)
                .build();

        postPaymentResponse(body)
                .statusCode(201)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("amount", is(amount))
                .body("reference", is(reference))
                .body("email", is(email))
                .body("description", is(description))
                .body("return_url", is(return_url))
                .body("payment_provider", is(PAYMENT_PROVIDER))
                .body("card_brand", is(CARD_BRAND_LABEL))
                .body("created_date", is(CREATED_DATE));
    }

    @Test
    public void createPayment_responseWith500_whenConnectorResponseIsAnUnrecognisedError() throws Exception {
        String gatewayAccountId = "1234567";
        String errorMessage = "something went wrong";

        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, gatewayAccountId);

        connectorMockClient.respondBadRequest_whenCreateCharge(gatewayAccountId, errorMessage);

        InputStream body = postPaymentResponse(SUCCESS_PAYLOAD)
                .statusCode(500)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0198"))
                .assertThat("$.description", is("Downstream system error"));

        connectorMockClient.verifyCreateChargeConnectorRequest(gatewayAccountId, SUCCESS_PAYLOAD);
    }

    @Test
    public void createPayment_responseWith500_whenInvalidDirectDebitAgreementType() throws Exception {
        String gatewayAccountId = "1234567";
        String errorMessage = "something went wrong";

        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, gatewayAccountId);

        connectorMockClient.respondMandateTypeInvalid_whenCreateCharge(gatewayAccountId, errorMessage);

        InputStream body = postPaymentResponse(SUCCESS_PAYLOAD)
                .statusCode(500)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0140"))
                .assertThat("$.description", is("Can't collect payment from this type of agreement"));

        connectorMockClient.verifyCreateChargeConnectorRequest(gatewayAccountId, SUCCESS_PAYLOAD);
    }

    @Test
    public void createPayment_responseWith500_whenTokenForGatewayAccountIsValidButConnectorResponseIsNotFound() {
        String notFoundGatewayAccountId = "9876545";
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, notFoundGatewayAccountId);

        connectorMockClient.respondNotFound_whenCreateCharge(notFoundGatewayAccountId);

        postPaymentResponse(SUCCESS_PAYLOAD)
                .statusCode(500)
                .contentType(JSON)
                .body("code", is("P0199"))
                .body("description", is("There is an error with this account. Please contact support"));

        connectorMockClient.verifyCreateChargeConnectorRequest(notFoundGatewayAccountId, SUCCESS_PAYLOAD);
    }

    @Test
    public void createPayment_responseWith422_whenZeroAmountNotAllowed() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);

        connectorMockClient.respondZeroAmountNotAllowed(GATEWAY_ACCOUNT_ID);

        postPaymentResponse(SUCCESS_PAYLOAD)
                .statusCode(422)
                .contentType(JSON)
                .body("code", is("P0102"))
                .body("field", is("amount"))
                .body("description", is("Invalid attribute value: amount. Must be greater than or equal to 1"));

        connectorMockClient.verifyCreateChargeConnectorRequest(GATEWAY_ACCOUNT_ID, SUCCESS_PAYLOAD);
    }

    @Test
    public void createPayment_responseWith422_whenZeroAmountForDirectDebitAccount() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        String payload = new JsonStringBuilder()
                .add("amount", 0)
                .add("reference", REFERENCE)
                .add("description", DESCRIPTION)
                .add("return_url", RETURN_URL)
                .build();

        postPaymentResponse(payload)
                .statusCode(422)
                .contentType(JSON)
                .body("code", is("P0102"))
                .body("field", is("amount"))
                .body("description", is("Invalid attribute value: amount. Must be greater than or equal to 1"));
    }

    @Test
    public void createPayment_Returns401_WhenUnauthorised() {
        publicAuthMockClient.respondUnauthorised();
        postPaymentResponse(SUCCESS_PAYLOAD).statusCode(401);
    }

    @Test
    public void createPayment_Returns_WhenPublicAuthInaccessible() {
        publicAuthMockClient.respondWithError();
        postPaymentResponse(SUCCESS_PAYLOAD).statusCode(503);
    }

    public static String paymentPayload(CreateChargeRequestParams params) {
        JsonStringBuilder payload = new JsonStringBuilder()
                .add("amount", params.getAmount())
                .add("reference", params.getReference())
                .add("description", params.getDescription())
                .add("return_url", params.getReturnUrl());

        if (!params.getMetadata().isEmpty()) {
            payload.add("metadata", params.getMetadata());
        }

        if (params.getEmail() != null) {
            payload.add("email", params.getEmail());
        }

        if (params.getCardholderName().isPresent()) {
            payload.addToNestedMap("cardholder_name", params.getCardholderName().get(), "prefilled_cardholder_details");
        }

        if (params.getAddressLine1().isPresent()) {
            payload.addToNestedMap("line1", params.getAddressLine1().get(), "prefilled_cardholder_details", "billing_address");
        }

        if (params.getAddressLine2().isPresent()) {
            payload.addToNestedMap("line2", params.getAddressLine2().get(), "prefilled_cardholder_details", "billing_address");
        }

        if (params.getAddressPostcode().isPresent()) {
            payload.addToNestedMap("postcode", params.getAddressPostcode().get(), "prefilled_cardholder_details", "billing_address");
        }

        if (params.getAddressCity().isPresent()) {
            payload.addToNestedMap("city", params.getAddressCity().get(), "prefilled_cardholder_details", "billing_address");
        }

        if (params.getAddressCountry().isPresent()) {
            payload.addToNestedMap("country", params.getAddressCountry().get(), "prefilled_cardholder_details", "billing_address");
        }

        return payload.build();
    }


    protected ValidatableResponse postPaymentResponse(String payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .post(PAYMENTS_PATH)
                .then();
    }
}
