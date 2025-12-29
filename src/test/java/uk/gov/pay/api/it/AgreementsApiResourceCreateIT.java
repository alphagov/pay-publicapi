package uk.gov.pay.api.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import uk.gov.pay.api.agreement.model.CreateAgreementRequest;
import uk.gov.pay.api.model.CreateAgreementRequestBuilder;
import uk.gov.pay.api.utils.PublicAuthMockClientJUnit5;
import uk.gov.pay.api.utils.mocks.ConnectorMockClientJUnit5;
import uk.gov.pay.api.utils.mocks.CreateAgreementRequestParams;
import uk.gov.pay.api.utils.mocks.LedgerMockClientJUnit5;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;
import static uk.gov.pay.api.utils.Payloads.agreementPayload;
import static uk.gov.pay.api.utils.mocks.AgreementFromLedgerFixture.AgreementFromLedgerFixtureBuilder.anAgreementFromLedgerWithoutPaymentInstrumentFixture;
import static uk.gov.pay.api.utils.mocks.CreateAgreementRequestParams.CreateAgreementRequestParamsBuilder.aCreateAgreementRequestParams;

class AgreementsApiResourceCreateIT extends PaymentResourceITestBase {

    private static final String ILLEGAL_REFERENCE = "Some reference <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String REFERENCE = "A valid reference";
    private static final String ILLEGAL_DESCRIPTION = "Some description <script> alert('This is a ?{simple} XSS attack.')</script>";
    private static final String DESCRIPTION = "A valid description";
    private static final String USER_IDENTIFIER = "a-valid-user-identifier";
    private static final String VALID_AGREEMENT_ID = "12345678901234567890123456";
    private final ConnectorMockClientJUnit5 connectorMockClient = new ConnectorMockClientJUnit5(connectorServer);
    private final PublicAuthMockClientJUnit5 publicAuthMockClient = new PublicAuthMockClientJUnit5(publicAuthServer);
    private final LedgerMockClientJUnit5 ledgerMockClient = new LedgerMockClientJUnit5(ledgerServer);

    @Test
    void shouldCreateAgreement() throws JsonProcessingException {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(REFERENCE)
                .withDescription(DESCRIPTION)
                .withUserIdentifier(USER_IDENTIFIER)
                .build();
        var agreementFixture = anAgreementFromLedgerWithoutPaymentInstrumentFixture()
                .withExternalId(VALID_AGREEMENT_ID)
                .withPaymentInstrument(null)
                .build();
        connectorMockClient.respondCreated_whenCreateAgreement(GATEWAY_ACCOUNT_ID, createAgreementRequestParams);
        ledgerMockClient.respondWithAgreement(VALID_AGREEMENT_ID, agreementFixture);
        postAgreementRequest(agreementPayload(createAgreementRequestParams))
                .statusCode(HttpStatus.SC_CREATED)
                .contentType(JSON)
                .body("agreement_id", is(VALID_AGREEMENT_ID))
                .body("reference", is("valid-reference"))
                .body("description", is("An agreement description"));
        connectorMockClient.verifyCreateAgreementConnectorRequest(GATEWAY_ACCOUNT_ID, createAgreementRequestParams);
    }

    @Test
    void shouldReturn400WhenWhenReferenceIsEmptyString() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference("")
                .build();
        postAgreementRequest(agreementPayload(createAgreementRequestParams))
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(JSON)
                .body("field", is("reference"))
                .body("code", is("P2101"))
                .body("description", is("Missing mandatory attribute: reference"));
    }

    @Test
    void shouldReturn400WhenWhenReferenceIsNull() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        CreateAgreementRequest agreementRequest = new CreateAgreementRequest(CreateAgreementRequestBuilder.builder().reference(null).description(DESCRIPTION));
        postAgreementRequest(agreementRequest.toConnectorPayload())
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(JSON)
                .body("field", is("reference"))
                .body("code", is("P2101"))
                .body("description", is("Missing mandatory attribute: reference"));
    }

    @Test
    void shouldReturn400WhenWhenDescriptionIsNull() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        CreateAgreementRequest agreementRequest = new CreateAgreementRequest(CreateAgreementRequestBuilder.builder().reference(REFERENCE).description(null));
        postAgreementRequest(agreementRequest.toConnectorPayload())
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(JSON)
                .body("field", is("description"))
                .body("code", is("P2101"))
                .body("description", is("Missing mandatory attribute: description"));
    }

    @Test
    void shouldReturn400WhenWhenDescriptionIsEmptyString() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        CreateAgreementRequest agreementRequest = new CreateAgreementRequest(CreateAgreementRequestBuilder.builder().reference(REFERENCE).description(""));
        postAgreementRequest(agreementRequest.toConnectorPayload())
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(JSON)
                .body("field", is("description"))
                .body("code", is("P2101"))
                .body("description", is("Missing mandatory attribute: description"));
    }

    @Test
    void shouldReturn422WhenWhenDescriptionIsTooLong() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        CreateAgreementRequest agreementRequest = new CreateAgreementRequest(CreateAgreementRequestBuilder.builder().reference(REFERENCE).description(random(256, true, true)));
        postAgreementRequest(agreementRequest.toConnectorPayload())
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("field", is("description"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: description. Must be less than or equal to 255 characters length"));
    }

    @Test
    void shouldReturn201WhenWhenOptionalUserIdentifierIsNull() throws JsonProcessingException {
        var agreementFixture = anAgreementFromLedgerWithoutPaymentInstrumentFixture()
                .withExternalId(VALID_AGREEMENT_ID)
                .withPaymentInstrument(null)
                .build();
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        var params = aCreateAgreementRequestParams()
                .withReference(REFERENCE)
                .withDescription(DESCRIPTION)
                .withUserIdentifier(null)
                .build();
        connectorMockClient.respondCreated_whenCreateAgreement(GATEWAY_ACCOUNT_ID, params);
        ledgerMockClient.respondWithAgreement(VALID_AGREEMENT_ID, agreementFixture);
        postAgreementRequest(agreementPayload(params))
                .statusCode(HttpStatus.SC_CREATED)
                .contentType(JSON);
    }

    @Test
    void shouldReturn422WhenWhenUserIdentifierIsEmptyString() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        CreateAgreementRequest agreementRequest = new CreateAgreementRequest(CreateAgreementRequestBuilder.builder().reference(REFERENCE).description(DESCRIPTION).userIdentifier(""));
        postAgreementRequest(agreementRequest.toConnectorPayload())
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("field", is("user_identifier"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: user_identifier. Must be less than or equal to 255 characters length"));
    }

    @Test
    void shouldReturn422WhenWhenUserIdentifierIsTooLong() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        CreateAgreementRequest agreementRequest = new CreateAgreementRequest(CreateAgreementRequestBuilder.builder().reference(REFERENCE).description(DESCRIPTION).userIdentifier(random(256, true, true)));
        postAgreementRequest(agreementRequest.toConnectorPayload())
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("field", is("user_identifier"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: user_identifier. Must be less than or equal to 255 characters length"));
    }

    @Test
    void shouldReturn422WhenWhenReferenceIsTooLong() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        String tooLongReference = random(256, true, true);
        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(tooLongReference)
                .withDescription(DESCRIPTION)
                .build();

        postAgreementRequest(agreementPayload(createAgreementRequestParams))
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("field", is("reference"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: reference. Must be less than or equal to 255 characters length"));
    }

    @Test
    void createPayment_responseWith500_whenConnectorResponseIsAnUnrecognisedError() throws Exception {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(REFERENCE)
                .withDescription(DESCRIPTION)
                .build();
        connectorMockClient.respondBadRequest_whenCreateAgreement(GATEWAY_ACCOUNT_ID, "Downstream system error");

        postAgreementRequest(agreementPayload(createAgreementRequestParams))
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .contentType(JSON)
                .body("code", is("P2198"))
                .body("description", is("Downstream system error"));

        connectorMockClient.verifyCreateAgreementConnectorRequest(GATEWAY_ACCOUNT_ID, createAgreementRequestParams);
    }

    @Test
    void createAgreement_Returns401_WhenUnauthorised() {
        publicAuthMockClient.respondUnauthorised();
        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(REFERENCE)
                .withDescription(DESCRIPTION)
                .build();
        postAgreementRequest(agreementPayload(createAgreementRequestParams)).statusCode(401);
    }

    @Test
    void createAgreement_Returns_WhenPublicAuthInaccessible() {
        publicAuthMockClient.respondWithError();
        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(REFERENCE)
                .build();
        postAgreementRequest(agreementPayload(createAgreementRequestParams)).statusCode(503);
    }

    @Test
    void shouldReturnBadRequestWhenAgreementReferenceContainsIllegalCharacters() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(ILLEGAL_REFERENCE)
                .withDescription(DESCRIPTION)
                .build();

        postAgreementRequest(agreementPayload(createAgreementRequestParams))
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(JSON)
                .body("field", is("reference"))
                .body("code", is("P2102"))
                .body("description", is("Invalid attribute value: reference. Must be a valid string format"));
    }

    @Test
    void shouldReturnBadRequestWhenAgreementDescriptionContainsIllegalCharacters() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(REFERENCE)
                .withDescription(ILLEGAL_DESCRIPTION)
                .build();

        postAgreementRequest(agreementPayload(createAgreementRequestParams))
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(JSON)
                .body("field", is("description"))
                .body("code", is("P2102"))
                .body("description", is("Invalid attribute value: description. Must be a valid string format"));
    }

    private ValidatableResponse postAgreementRequest(String payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post("/v1/agreements")
                .then();
    }
}
