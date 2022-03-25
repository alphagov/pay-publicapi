package uk.gov.pay.api.it;

import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.junit.Test;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.api.utils.PublicAuthMockClient;
import uk.gov.pay.api.utils.mocks.ConnectorMockClient;
import uk.gov.pay.api.utils.mocks.CreateAgreementRequestParams;
import uk.gov.service.payments.commons.validation.DateTimeUtils;

import java.time.ZonedDateTime;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;
import static uk.gov.pay.api.utils.mocks.CreateAgreementRequestParams.CreateAgreementRequestParamsBuilder.aCreateAgreementRequestParams;
import static uk.gov.service.payments.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class CreateAgreementIT extends PaymentResourceITestBase {

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
    private static final String SUCCESS_PAYLOAD = agreementPayload(aCreateAgreementRequestParams()
            .withReference(REFERENCE)
           .build());


    private static final String GATEWAY_TRANSACTION_ID = "gateway-tx-123456";

    private ConnectorMockClient connectorMockClient = new ConnectorMockClient(connectorMock);
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);


    @Test
    public void createAgreement() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(REFERENCE)
                .build();
        connectorMockClient.respondOk_whenCreateAgreement(GATEWAY_ACCOUNT_ID, createAgreementRequestParams);

        postAgreementResponse(agreementPayload(createAgreementRequestParams))
                .statusCode(HttpStatus.SC_CREATED)
                .contentType(JSON)
                .body("agreement_id", is(VALID_AGREEMENT_ID));
             //   .body("save_payment_instrument_to_agreement", is(true));

        connectorMockClient.verifyCreateAgreementConnectorRequest(GATEWAY_ACCOUNT_ID, createAgreementRequestParams);
    }

//    @Test
//    public void shouldReturn422WhencreateAChargeIsCalledWithTooShortAgreementId() {
//        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
//
//        CreateChargeRequestParams createChargeRequestParams = aCreateChargeRequestParams()
//                .withAmount(100)
//                .withDescription(DESCRIPTION)
//                .withReference(REFERENCE)
//                .withReturnUrl(RETURN_URL)
//                .withSetUpAgreement(TOO_SHORT_AGREEMENT_ID)
//                .build();
//
//        postAgreementResponse(agreementPayload(createChargeRequestParams))
//                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
//                .contentType(JSON)
//                .body("field",  is("set_up_agreement"))
//                .body("code",  is("P0102"))
//                .body("description", is("Invalid attribute value: set_up_agreement. Field [set_up_agreement] length must be 26"));
//    }
//
//    @Test
//    public void shouldReturn422WhencreateAChargeIsCalledWithTooLongAgreementId() {
//        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
//
//        CreateChargeRequestParams createChargeRequestParams = aCreateChargeRequestParams()
//                .withAmount(100)
//                .withDescription(DESCRIPTION)
//                .withReference(REFERENCE)
//                .withReturnUrl(RETURN_URL)
//                .withSetUpAgreement(TOO_LONG_AGREEMENT_ID)
//                .build();
//
//        postAgreementResponse(agreementPayload(createChargeRequestParams))
//                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
//                .contentType(JSON)
//                .body("field",  is("set_up_agreement"))
//                .body("code",  is("P0102"))
//                .body("description", is("Invalid attribute value: set_up_agreement. Field [set_up_agreement] length must be 26"));
//    }
//
//    @Test
//    public void createAChargeWithMetadataAgreementNotFound() throws IOException {
//        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
//
//        CreateChargeRequestParams createChargeRequestParams = aCreateChargeRequestParams()
//                .withAmount(100)
//                .withDescription(DESCRIPTION)
//                .withReference(REFERENCE)
//                .withReturnUrl(RETURN_URL)
//                .withSetUpAgreement(VALID_AGREEMENT_ID)
//                .build();
//        connectorMockClient.respondBadRequest_whenCreateChargeWithAgreementNotFound(GATEWAY_ACCOUNT_ID, VALID_AGREEMENT_ID, "Agreement with ID [%s] not found.");
//
//        InputStream body = postAgreementResponse(agreementPayload(createChargeRequestParams))
//                .statusCode(HttpStatus.SC_BAD_REQUEST)
//                .contentType(JSON).extract()
//                .body().asInputStream();
//
//        JsonAssert.with(body)
//                .assertThat("$.*", hasSize(3))
//                .assertThat("$.field", is("set_up_agreement"))
//                .assertThat("$.code", is("P0103"))
//                .assertThat("$.description", is("Invalid attribute value: set_up_agreement. Agreement ID does not exist"));
//
//        connectorMockClient.verifyCreateChargeConnectorRequest(GATEWAY_ACCOUNT_ID, createChargeRequestParams);
//    }

    
//    @Test
//    public void createAgreement() {
//        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
//        connectorMockClient.respondOk_whenCreateAgreement(CHARGE_TOKEN_ID, GATEWAY_ACCOUNT_ID, aCreateAgreementResponseFromConnector()
//                .withReference(REFERENCE)
//                        .withAgreementId(VALID_AGREEMENT_ID)
//                        .withServiceId("SERVICE-ID")
//                .withCreatedDate(CREATED_DATE)
//                        .withLive(true)
//                .build());
//
//        String responseBody = postAgreementResponse(SUCCESS_PAYLOAD)
//                .statusCode(HttpStatus.SC_CREATED)
//                .contentType(JSON)
//                .header(HttpHeaders.LOCATION, is(paymentLocationFor(configuration.getBaseUrl(), CHARGE_ID)))
//                .body("payment_id", is(CHARGE_ID))
//                .body("amount", is(9999999))
//                .body("reference", is(REFERENCE))
//                .body("email", nullValue())
//                .body("description", is(DESCRIPTION))
//                .body("state.status", is(CREATED.getStatus()))
//                .body("return_url", is(RETURN_URL))
//                .body("payment_provider", is(PAYMENT_PROVIDER))
//                .body("card_brand", is(CARD_BRAND_LABEL))
//                .body("created_date", is(CREATED_DATE))
//                .body("delayed_capture", is(true))
//                .body("provider_id", is(GATEWAY_TRANSACTION_ID))
//                .body("refund_summary.status", is("pending"))
//                .body("refund_summary.amount_submitted", is(50))
//                .body("refund_summary.amount_available", is(100))
//                .body("_links.self.href", is(paymentLocationFor(configuration.getBaseUrl(), CHARGE_ID)))
//                .body("_links.self.method", is("GET"))
//                .body("_links.next_url.href", is(frontendUrlFor(CARD) + CHARGE_TOKEN_ID))
//                .body("_links.next_url.method", is("GET"))
//                .body("_links.next_url_post.href", is(frontendUrlFor(CARD)))
//                .body("_links.next_url_post.method", is("POST"))
//                .body("_links.next_url_post.type", is("application/x-www-form-urlencoded"))
//                .body("_links.next_url_post.params.chargeTokenId", is(CHARGE_TOKEN_ID))
//                .body("_links.events.href", is(paymentEventsLocationFor(CHARGE_ID)))
//                .body("_links.events.method", is("GET"))
//                .body("_links.cancel.href", is(paymentCancelLocationFor(CHARGE_ID)))
//                .body("_links.cancel.method", is("POST"))
//                .body("_links.refunds.href", is(paymentRefundsLocationFor(CHARGE_ID)))
//                .body("_links.refunds.method", is("GET"))
//                .body("metadata", nullValue())
//                .extract().body().asString();
//
//        JsonAssert.with(responseBody)
//                .assertNotDefined("_links.self.type")
//                .assertNotDefined("_links.self.params")
//                .assertNotDefined("_links.next_url.type")
//                .assertNotDefined("_links.next_url.params")
//                .assertNotDefined("_links.events.type")
//                .assertNotDefined("_links.events.params");
//
//        connectorMockClient.verifyCreateChargeConnectorRequest(GATEWAY_ACCOUNT_ID, CHARGE_REQUEST_PARAMS);
//    }
//
//    @Test
//    public void createPayment_responseWith500_whenConnectorResponseIsAnUnrecognisedError() throws Exception {
//        String gatewayAccountId = "1234567";
//        String errorMessage = "something went wrong";
//
//        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, gatewayAccountId);
//
//        connectorMockClient.respondBadRequest_whenCreateCharge(gatewayAccountId, errorMessage);
//
//        InputStream body = postAgreementResponse(SUCCESS_PAYLOAD)
//                .statusCode(500)
//                .contentType(JSON).extract()
//                .body().asInputStream();
//
//        JsonAssert.with(body)
//                .assertThat("$.*", hasSize(2))
//                .assertThat("$.code", is("P0198"))
//                .assertThat("$.description", is("Downstream system error"));
//
//        connectorMockClient.verifyCreateChargeConnectorRequest(gatewayAccountId, CHARGE_REQUEST_PARAMS);
//    }

//    @Test
//    public void createPayment_responseWith500_whenTokenForGatewayAccountIsValidButConnectorResponseIsNotFound() {
//        String notFoundGatewayAccountId = "9876545";
//        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, notFoundGatewayAccountId);
//
//        connectorMockClient.respondNotFound_whenCreateCharge(notFoundGatewayAccountId);
//
//        postAgreementResponse(SUCCESS_PAYLOAD)
//                .statusCode(500)
//                .contentType(JSON)
//                .body("code", is("P0199"))
//                .body("description", is("There is an error with this account. Please contact support"));
//
//        connectorMockClient.verifyCreateChargeConnectorRequest(notFoundGatewayAccountId, CHARGE_REQUEST_PARAMS);
//    }

    @Test
    public void createPayment_Returns401_WhenUnauthorised() {
        publicAuthMockClient.respondUnauthorised();
        postAgreementResponse(SUCCESS_PAYLOAD).statusCode(401);
    }

//    @Test
//    public void createPayment_Returns403_WhenGatewayAccountCredentialsNotFullyConfigured() {
//        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
//        connectorMockClient.respondGatewayAccountCredentialNotConfigured(GATEWAY_ACCOUNT_ID);
//        postAgreementResponse(SUCCESS_PAYLOAD)
//                .statusCode(403)
//                .contentType(JSON)
//                .body("code", is("P0940"))
//                .body("description", is("Account is not fully configured. Please refer to documentation to setup your account or contact support."));
//
//        connectorMockClient.verifyCreateChargeConnectorRequest(GATEWAY_ACCOUNT_ID, CHARGE_REQUEST_PARAMS);
//    }

    @Test
    public void createPayment_Returns_WhenPublicAuthInaccessible() {
        publicAuthMockClient.respondWithError();
        postAgreementResponse(SUCCESS_PAYLOAD).statusCode(503);
    }
    
    public static String agreementPayload(CreateAgreementRequestParams params) {
        return  new JsonStringBuilder().add("reference", params.getReference()).build();
    }

    protected ValidatableResponse postAgreementResponse(String payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .post("/v1/api/accounts/GATEWAY_ACCOUNT_ID/agreements")
                .then();
    }
}
