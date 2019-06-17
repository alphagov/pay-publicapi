package uk.gov.pay.api.it.directdebit;

import com.google.gson.Gson;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.model.directdebit.agreement.AgreementStatus;
import uk.gov.pay.api.model.directdebit.agreement.MandateState;
import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.api.utils.PublicAuthMockClient;
import uk.gov.pay.api.utils.mocks.ConnectorDDMockClient;

import javax.ws.rs.core.HttpHeaders;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;
import static uk.gov.pay.api.utils.Urls.directDebitFrontendSecureUrl;
import static uk.gov.pay.api.utils.mocks.CreateMandateRequestParams.CreateMandateRequestParamsBuilder.aCreateMandateRequestParams;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;
import static uk.gov.pay.commons.testing.matchers.HamcrestMatchers.optionalMatcher;

@RunWith(JUnitParamsRunner.class)
public class AgreementsResourceIT extends PaymentResourceITestBase {

    private ConnectorDDMockClient connectorDDMockClient = new ConnectorDDMockClient(connectorDDMock);
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);
    
    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final String CREATED_DATE = ISO_INSTANT_MILLISECOND_PRECISION.format(TIMESTAMP);
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final String MANDATE_ID = "mandateId";
    private static final String MANDATE_REFERENCE = "test_mandate_reference";
    private static final String SERVICE_REFERENCE = "test_service_reference";
    private static final String RETURN_URL = "https://service-name.gov.uk/transactions/12345";

    @Test
    @Parameters({
            "null, test-service-ref, Field [return_url] cannot be null",
            " , test-service-ref, Field [return_url] must have a size between 1 and 255",
            "http://example, null, Field [service_reference] cannot be null",
            "http://example, , Field [service_reference] must have a size between 1 and 255"
    })
    public void createMandateValidationFailures(@Nullable String returnUrl,
                                                @Nullable String serviceReference,
                                                String expectedErrorMessage) throws Exception {
        //TODO
    }
    
    @Test
    @Parameters({"I'ma need space I'ma I'ma need space (N-A-S-A)", "null"})
    public void createMandate(@Nullable String description) {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);
        
        connectorDDMockClient.respondOk_whenCreateMandateRequest(aCreateMandateRequestParams()
                .withMandateId(MANDATE_ID)
                .withProviderId(MANDATE_REFERENCE)
                .withServiceReference(SERVICE_REFERENCE)
                .withReturnUrl(RETURN_URL)
                .withCreatedDate(CREATED_DATE)
                .withState(new MandateState("created", false))
                .withGatewayAccountId(GATEWAY_ACCOUNT_ID)
                .withDescription(description)
                .withChargeTokenId(CHARGE_TOKEN_ID).build());

        Map<String, String> payload = new HashMap<>();
        payload.put("return_url", RETURN_URL);
        payload.put("reference", "test service reference");
        Optional.ofNullable(description).ifPresent(x -> payload.put("description", x));
        
        given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post("/v1/directdebit/mandates")
                .then().log().body()
                .statusCode(201)
                .contentType(JSON)
                .header(HttpHeaders.LOCATION, is("http://publicapi.url/v1/agreements/mandateId"))
                .body("mandate_id", is(MANDATE_ID))
                .body("reference", is(SERVICE_REFERENCE))
                .body("description", optionalMatcher(description))
                .body("provider_id", is(MANDATE_REFERENCE))
                .body("return_url", is(RETURN_URL))
                .body("created_date", is(CREATED_DATE))
                .body("payment_provider", is("gocardless"))
                .body("state.status", is(AgreementStatus.CREATED.getStatus()))
                .body("_links.self.href", is(mandateLocationFor(MANDATE_ID)))
                .body("_links.self.method", is("GET"))
                .body("_links.next_url.href", is(directDebitFrontendSecureUrl() + CHARGE_TOKEN_ID))
                .body("_links.next_url.method", is("GET"))
                .body("_links.next_url_post.href", is(directDebitFrontendSecureUrl()))
                .body("_links.next_url_post.method", is("POST"))
                .body("_links.next_url_post.type", is("application/x-www-form-urlencoded"))
                .body("_links.next_url_post.params.chargeTokenId", is(CHARGE_TOKEN_ID))
                //TODOs
//                .body("_links.events.href", is("http://publicapi.url/v1/directdebit/mandates/" + MANDATE_ID)) 
//                .body("_links.events.method", is("GET"))
//                .body("_links.events.href", is("http://publicapi.url/v1/directdebit/payments?mandate_id=" + MANDATE_ID))
//                .body("_links.events.method", is("GET"))
                ;
    }

    @Test
    public void createPayment_respondsWith500_whenConnectorResponseIsAnUnrecognisedError() {

        String errorMessage = "something went wrong";

        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMockClient.respondBadRequest_whenCreateAgreementRequest(GATEWAY_ACCOUNT_ID, errorMessage);

        String payload = createMandatePayload("https://service-name.gov.uk/transactions/12345");
        given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post("/v1/directdebit/mandates")
                .then()
                .statusCode(500)
                .contentType(JSON)
                .body("code", is("P0198"))
                .body("description", is("Downstream system error"))
                .extract().body().asString();
    }

    @Test
    public void createPayment_respondsWith500_whenConnectorResponseIsAgreementTypeInvalid() {

        String errorMessage = "something went wrong";

        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMockClient.respondWithMandateTypeInvalid_whenCreateAgreementRequest(
                GATEWAY_ACCOUNT_ID,
                errorMessage
        );

        String payload = createMandatePayload("https://service-name.gov.uk/transactions/12345");
        given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post("/v1/directdebit/mandates")
                .then()
                .statusCode(500)
                .contentType(JSON)
                .body("code", is("P0197"))
                .body("description", is("It is not possible to create an agreement of this type"))
                .extract().body().asString();
    }


    @Test
    public void createPayment_respondsWith500_whenConnectorResponseIsGCAccountNotLinked() {

        String errorMessage = "something went wrong";

        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMockClient.respondWithGCAccountNotLinked_whenCreateAgreementRequest(
                GATEWAY_ACCOUNT_ID,
                errorMessage
        );

        String payload = createMandatePayload("https://service-name.gov.uk/transactions/12345");
        given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post("/v1/directdebit/mandates")
                .then()
                .statusCode(500)
                .contentType(JSON)
                .body("code", is("P0199"))
                .body("description", is("There is an error with this account. Please contact support"))
                .extract().body().asString();
    }
    
    @Test
    public void shouldGetADirectDebitAgreement_withReference() {

        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMockClient.respondOk_whenGetAgreementRequest(
                MANDATE_ID,
                MANDATE_REFERENCE,
                SERVICE_REFERENCE,
                RETURN_URL,
                new MandateState("created", false),
                GATEWAY_ACCOUNT_ID,
                CHARGE_TOKEN_ID
        );

        given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .get("/v1/agreements/" + MANDATE_ID)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("agreement_id", is(MANDATE_ID))
                .body("provider_id", is(MANDATE_REFERENCE))
                .body("reference", is(SERVICE_REFERENCE))
                .body("return_url", is(RETURN_URL))
                .body("state.status", is(AgreementStatus.CREATED.getStatus()))
                .body("_links.self.href", is(mandateLocationFor(MANDATE_ID)))
                .body("_links.self.method", is("GET"))
                .body("_links.next_url.href", is(directDebitFrontendSecureUrl() + CHARGE_TOKEN_ID))
                .body("_links.next_url.method", is("GET"))
                .body("_links.next_url_post.href", is(directDebitFrontendSecureUrl()))
                .body("_links.next_url_post.method", is("POST"))
                .body("_links.next_url_post.type", is("application/x-www-form-urlencoded"))
                .body("_links.next_url_post.params.chargeTokenId", is(CHARGE_TOKEN_ID))
                .extract().body().asString();
    }

    private String mandateLocationFor(String mandateId) {
        return "http://publicapi.url/v1/agreements/" + mandateId; //TODO change this for PP-5299
    }

    private static String createMandatePayload(String returnUrl) {
        return new Gson().toJson(Map.of("return_url", returnUrl, "reference", "test service reference"));
    }
}
