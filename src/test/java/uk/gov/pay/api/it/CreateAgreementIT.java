package uk.gov.pay.api.it;

import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.junit.Test;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.api.utils.PublicAuthMockClient;
import uk.gov.pay.api.utils.mocks.ConnectorMockClient;
import uk.gov.pay.api.utils.mocks.CreateAgreementRequestParams;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;
import static uk.gov.pay.api.utils.mocks.CreateAgreementRequestParams.CreateAgreementRequestParamsBuilder.aCreateAgreementRequestParams;

public class CreateAgreementIT extends PaymentResourceITestBase {
   
    private static final String REFERENCE = "Some reference <script> alert('This is a ?{simple} XSS attack.')</script>";
    public static final String VALID_AGREEMENT_ID = "12345678901234567890123456";
    private ConnectorMockClient connectorMockClient = new ConnectorMockClient(connectorMock);
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);

    @Test
    public void shouldCreateAgreement() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(REFERENCE)
                .build();
        connectorMockClient.respondOk_whenCreateAgreement(GATEWAY_ACCOUNT_ID, createAgreementRequestParams);

        postAgreementRequest(agreementPayload(createAgreementRequestParams))
                .statusCode(HttpStatus.SC_CREATED)
                .contentType(JSON)
                .body("agreement_id", is(VALID_AGREEMENT_ID))
                .body("reference", is(REFERENCE));
        connectorMockClient.verifyCreateAgreementConnectorRequest(GATEWAY_ACCOUNT_ID, createAgreementRequestParams);
    }

    @Test
    public void shouldReturn422WhenWhenReferenceIsEmptyString() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference("")
                .build();
        postAgreementRequest(agreementPayload(createAgreementRequestParams))
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("field", is("reference"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: reference. Must be less than or equal to 255 characters length"));
    }

    @Test
    public void shouldReturn422WhenWhenReferenceIsTooLong() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
        String tooLongReference = random(256, true, true);
        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(tooLongReference)
                .build();
        
        postAgreementRequest(agreementPayload(createAgreementRequestParams))
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("field", is("reference"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: reference. Must be less than or equal to 255 characters length"));
    }
 
    @Test
  public void createPayment_responseWith500_whenConnectorResponseIsAnUnrecognisedError() throws Exception {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);

        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(REFERENCE)
                .build();
        connectorMockClient.respondBadRequest_whenCreateAgreement(GATEWAY_ACCOUNT_ID, "Downstream system error");

       postAgreementRequest(agreementPayload(createAgreementRequestParams))
               .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .contentType(JSON)
               .body("code",is("P0198"))
               .body("description", is("Downstream system error"));
      
        connectorMockClient.verifyCreateAgreementConnectorRequest(GATEWAY_ACCOUNT_ID, createAgreementRequestParams);
    }

    @Test
    public void createAgreement_Returns401_WhenUnauthorised() {
        publicAuthMockClient.respondUnauthorised();
        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(REFERENCE)
                .build();
        postAgreementRequest(agreementPayload(createAgreementRequestParams)).statusCode(401);
    }
    
    @Test
    public void createAgreement_Returns_WhenPublicAuthInaccessible() {
        publicAuthMockClient.respondWithError();
        CreateAgreementRequestParams createAgreementRequestParams = aCreateAgreementRequestParams()
                .withReference(REFERENCE)
                .build();
        postAgreementRequest(agreementPayload(createAgreementRequestParams)).statusCode(503);
    }
    
    public static String agreementPayload(CreateAgreementRequestParams params) {
        return  new JsonStringBuilder().add("reference", params.getReference()).build();
    }

    protected ValidatableResponse postAgreementRequest(String payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .post("/v1/agreements")
                .then();
    }
}
