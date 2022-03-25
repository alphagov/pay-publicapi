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
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;
import static uk.gov.pay.api.utils.mocks.CreateAgreementRequestParams.CreateAgreementRequestParamsBuilder.aCreateAgreementRequestParams;
import static uk.gov.service.payments.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class CreateAgreementIT extends PaymentResourceITestBase {

    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
 
    private static final String CARD_BRAND_LABEL = "Mastercard";
    private static final String CARD_TYPE = "credit";
  
    private static final String REFERENCE = "Some reference <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final Address BILLING_ADDRESS = new Address("line1", "line2", "NR2 5 6EG", "city", "UK");
  
    public static final String VALID_AGREEMENT_ID = "12345678901234567890123456";
    private static final String SUCCESS_PAYLOAD = agreementPayload(aCreateAgreementRequestParams()
            .withReference(REFERENCE)
           .build());
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
        connectorMockClient.verifyCreateAgreementConnectorRequest(GATEWAY_ACCOUNT_ID, createAgreementRequestParams);
    }

    @Test
    public void shouldReturn422WhenWhenReferenceIsEmptyString() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference("")
                .build();
        postAgreementResponse(agreementPayload(createAgreementRequestParams))
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("field", is("reference"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: reference. Field [reference] can have a size between 0 and 255"));
    }

    @Test
    public void shouldReturn422WhenWhenReferenceIsTooLong() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        
        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(random(256, true, true))
                .build();
        
        postAgreementResponse(agreementPayload(createAgreementRequestParams))
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("field", is("reference"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: reference. Field [reference] can have a size between 0 and 255"));
    }

   

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
    @Test
  public void createPayment_responseWith500_whenConnectorResponseIsAnUnrecognisedError() throws Exception {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(REFERENCE)
                .build();
        connectorMockClient.respondBadRequest_whenCreateAgreement(GATEWAY_ACCOUNT_ID, "Downstream error");

      var x = postAgreementResponse(agreementPayload(createAgreementRequestParams))
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .contentType(JSON)
                .body("message", is("Downstream error"));
        System.out.println(x.extract().asPrettyString());
        connectorMockClient.verifyCreateAgreementConnectorRequest(GATEWAY_ACCOUNT_ID, createAgreementRequestParams);

//        String gatewayAccountId = "1234567";
//        String errorMessage = "something went wrong";
//        //publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
////
//        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
//                .withReference(REFERENCE)
//                .build();
//       publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
//
//        connectorMockClient.respondBadRequest_whenCreateAgreement(GATEWAY_ACCOUNT_ID, errorMessage);
//
//        InputStream body = postAgreementResponse(agreementPayload(createAgreementRequestParams))
//                .statusCode(500)
//                .contentType(JSON).extract()
//                .body().asInputStream();
//
//        JsonAssert.with(body)
//                .assertThat("$.*", hasSize(2))
//                .assertThat("$.code", is("P0198"))
//                .assertThat("$.description", is("Downstream system error"));
//
     //   connectorMockClient.verifyCreateChargeConnectorRequest(gatewayAccountId, SUCCESS_PAYLOAD);
    
    }

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
