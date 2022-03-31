package uk.gov.pay.api.it;

import com.jayway.jsonassert.JsonAssert;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.Test;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.api.utils.PublicAuthMockClient;
import uk.gov.pay.api.utils.mocks.ConnectorMockClient;
import uk.gov.pay.api.utils.mocks.CreateChargeRequestParams;
import uk.gov.service.payments.commons.model.SupportedLanguage;
import uk.gov.service.payments.commons.validation.DateTimeUtils;

import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
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
import static uk.gov.pay.api.utils.Urls.paymentLocationFor;
import static uk.gov.pay.api.utils.mocks.ChargeResponseFromConnector.ChargeResponseFromConnectorBuilder.aCreateOrGetChargeResponseFromConnector;
import static uk.gov.pay.api.utils.mocks.CreateChargeRequestParams.CreateChargeRequestParamsBuilder.aCreateChargeRequestParams;
import static uk.gov.service.payments.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;
import static uk.gov.service.payments.commons.model.Source.CARD_PAYMENT_LINK;

public class CreatePaymentIT extends PaymentResourceITestBase {

    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final int AMOUNT = 9999999;
    private static final String CHARGE_ID = "ch_ab2341da231434l";
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final PaymentState CREATED = new PaymentState("created", false, null, null);
    private static final RefundSummary REFUND_SUMMARY = new RefundSummary("pending", 100L, 50L);
    private static final String PAYMENT_PROVIDER = "Sandbox";
    private static final String CARD_BRAND_LABEL = "Mastercard";
    private static final String CARD_TYPE = "credit";
    private static final String RETURN_URL = "https://somewhere.gov.uk/rainbow/1";
    private static final String REFERENCE = "Some reference <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String DESCRIPTION = "Some description <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String CREATED_DATE = ISO_INSTANT_MILLISECOND_PRECISION.format(TIMESTAMP);
    private static final Address BILLING_ADDRESS = new Address("line1", "line2", "NR2 5 6EG", "city", "UK");
    private static final CardDetails CARD_DETAILS = new CardDetails("1234", "123456", "Mr. Payment", "12/19", BILLING_ADDRESS, CARD_BRAND_LABEL, CARD_TYPE);
    public static final String VALID_AGREEMENT_ID = "12345678901234567890123456";
    public static final String TOO_SHORT_AGREEMENT_ID = "1234567890";
    public static final String TOO_LONG_AGREEMENT_ID = "1234567890123456789012345699999";
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
                .contentType(JSON)
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
    public void createAChargeWithAgreementIdAndSaveAgreement() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        CreateChargeRequestParams createChargeRequestParams = aCreateChargeRequestParams()
                .withAmount(100)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withReturnUrl(RETURN_URL)
                .withSetUpAgreement(VALID_AGREEMENT_ID)
                .build();
        connectorMockClient.respondOk_whenCreateCharge(GATEWAY_ACCOUNT_ID, createChargeRequestParams);

        postPaymentResponse(paymentPayload(createChargeRequestParams))
                .statusCode(HttpStatus.SC_CREATED)
                .contentType(JSON)
                .body("agreement_id", is(VALID_AGREEMENT_ID));
        connectorMockClient.verifyCreateChargeConnectorRequest(GATEWAY_ACCOUNT_ID, createChargeRequestParams);
    }

    @Test
    public void shouldReturn422WhencreateAChargeIsCalledWithTooShortAgreementId() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        CreateChargeRequestParams createChargeRequestParams = aCreateChargeRequestParams()
                .withAmount(100)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withReturnUrl(RETURN_URL)
                .withSetUpAgreement(TOO_SHORT_AGREEMENT_ID)
                .build();

        postPaymentResponse(paymentPayload(createChargeRequestParams))
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("field",  is("set_up_agreement"))
                .body("code",  is("P0102"))
                .body("description", is("Invalid attribute value: set_up_agreement. Field [set_up_agreement] length must be 26"));
    }

    @Test
    public void shouldReturn422WhencreateAChargeIsCalledWithTooLongAgreementId() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        CreateChargeRequestParams createChargeRequestParams = aCreateChargeRequestParams()
                .withAmount(100)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withReturnUrl(RETURN_URL)
                .withSetUpAgreement(TOO_LONG_AGREEMENT_ID)
                .build();

        postPaymentResponse(paymentPayload(createChargeRequestParams))
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("field",  is("set_up_agreement"))
                .body("code",  is("P0102"))
                .body("description", is("Invalid attribute value: set_up_agreement. Field [set_up_agreement] length must be 26"));
    }
    
    @Test
    public void createAChargeWithMetadataAgreementNotFound() throws IOException {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        CreateChargeRequestParams createChargeRequestParams = aCreateChargeRequestParams()
                .withAmount(100)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withReturnUrl(RETURN_URL)
                .withSetUpAgreement(VALID_AGREEMENT_ID)
                .build();
        connectorMockClient.respondBadRequest_whenCreateChargeWithAgreementNotFound(GATEWAY_ACCOUNT_ID, VALID_AGREEMENT_ID, "Agreement with ID [%s] not found.");
        
        InputStream body = postPaymentResponse(paymentPayload(createChargeRequestParams))
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(3))
                .assertThat("$.field", is("set_up_agreement"))
                .assertThat("$.code", is("P0103"))
                .assertThat("$.description", is("Invalid attribute value: set_up_agreement. Agreement ID does not exist"));

        connectorMockClient.verifyCreateChargeConnectorRequest(GATEWAY_ACCOUNT_ID, createChargeRequestParams);
    }

    @Test
    public void createCardPaymentWithMetadataAsNull_shouldReturn422() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        var payload = new JSONObject()
                .put("amount", 100)
                .put("reference", "my reference")
                .put("description", "my description")
                .put("metadata", JSONObject.NULL)
                .put("return_url", "https://test.test")
                .toString();

        postPaymentResponse(payload)
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("field", is("metadata"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: metadata. Value must not be null"));
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
                .statusCode(HttpStatus.SC_BAD_REQUEST)
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
                .statusCode(HttpStatus.SC_CREATED)
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
                .statusCode(HttpStatus.SC_CREATED)
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
                .statusCode(201).log().body()
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
    public void createMOTOPayment() {
        int amount = 1;

        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);

        connectorMockClient.respondOk_whenCreateCharge(CHARGE_TOKEN_ID, GATEWAY_ACCOUNT_ID, aCreateOrGetChargeResponseFromConnector()
                .withAmount(amount)
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
                .withMoto(true)
                .withRefundSummary(REFUND_SUMMARY)
                .withCardDetails(CARD_DETAILS)
                .build());

        CreateChargeRequestParams params = aCreateChargeRequestParams()
                .withAmount(amount)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withReturnUrl(RETURN_URL)
                .withMoto(true)
                .build();
        
        postPaymentResponse(paymentPayload(params))
                .statusCode(201)
                .contentType(JSON)
                .body("payment_id", is(CHARGE_ID))
                .body("amount", is(amount))
                .body("reference", is(REFERENCE))
                .body("description", is(DESCRIPTION))
                .body("return_url", is(RETURN_URL))
                .body("moto", is(true))
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
    public void createPayment_responseWith422_whenMototNotAllowed() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);

        connectorMockClient.respondMotoPaymentNotAllowed(GATEWAY_ACCOUNT_ID);

        String createMotoPaymentPayload = paymentPayload(aCreateChargeRequestParams()
                .withAmount(AMOUNT)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withReturnUrl(RETURN_URL)
                .withMoto(true)
                .build());
        
        postPaymentResponse(createMotoPaymentPayload)
                .statusCode(422)
                .contentType(JSON)
                .body("code", is("P0196"))
                .body("description", is("MOTO payments are not enabled for this account. Please contact support if you would like to process MOTO payments"));

        connectorMockClient.verifyCreateChargeConnectorRequest(GATEWAY_ACCOUNT_ID, createMotoPaymentPayload);
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
    public void createPayment_Returns401_WhenUnauthorised() {
        publicAuthMockClient.respondUnauthorised();
        postPaymentResponse(SUCCESS_PAYLOAD).statusCode(401);
    }

    @Test
    public void createPayment_Returns403_WhenGatewayAccountCredentialsNotFullyConfigured() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        connectorMockClient.respondGatewayAccountCredentialNotConfigured(GATEWAY_ACCOUNT_ID);
        postPaymentResponse(SUCCESS_PAYLOAD)
                .statusCode(403)
                .contentType(JSON)
                .body("code", is("P0940"))
                .body("description", is("Account is not fully configured. Please refer to documentation to setup your account or contact support."));

        connectorMockClient.verifyCreateChargeConnectorRequest(GATEWAY_ACCOUNT_ID, SUCCESS_PAYLOAD);
    }

    @Test
    public void createPayment_Returns_WhenPublicAuthInaccessible() {
        publicAuthMockClient.respondWithError();
        postPaymentResponse(SUCCESS_PAYLOAD).statusCode(503);
    }

    @Test
    public void createCardPaymentWithSource() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        CreateChargeRequestParams createChargeRequestParams = aCreateChargeRequestParams()
                .withAmount(100)
                .withDescription(DESCRIPTION)
                .withReference(REFERENCE)
                .withReturnUrl(RETURN_URL)
                .withReturnUrl(RETURN_URL)
                .withSource(CARD_PAYMENT_LINK)
                .build();
        connectorMockClient.respondOk_whenCreateCharge(GATEWAY_ACCOUNT_ID, createChargeRequestParams);

        postPaymentResponse(paymentPayload(createChargeRequestParams))
                .statusCode(201)
                .contentType(JSON);

        connectorMockClient.verifyCreateChargeConnectorRequest(GATEWAY_ACCOUNT_ID, createChargeRequestParams);
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
        
        if (params.isMoto() != null) {
            payload.add("moto", params.isMoto());
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

        params.getSource().ifPresent(source ->  {
            payload.addToNestedMap("source", source, "internal");
        });

        if (params.getSetUpAgreement() != null) {
            payload.add("set_up_agreement", params.getSetUpAgreement());
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
