package uk.gov.pay.api.it.directdebit;

import org.junit.Test;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.model.directdebit.agreement.AgreementStatus;
import uk.gov.pay.api.model.directdebit.agreement.AgreementType;
import uk.gov.pay.api.model.directdebit.agreement.MandateState;
import uk.gov.pay.api.model.directdebit.agreement.MandateType;
import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.ws.rs.core.HttpHeaders;
import java.time.ZonedDateTime;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;
import static uk.gov.pay.api.utils.Urls.directDebitFrontendSecureUrl;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class AgreementsResourceITest extends PaymentResourceITestBase {

    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final String CREATED_DATE = ISO_INSTANT_MILLISECOND_PRECISION.format(TIMESTAMP);
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final String MANDATE_ID = "mandateId";
    private static final String MANDATE_REFERENCE = "test_mandate_reference";
    private static final String SERVICE_REFERENCE = "test_service_reference";
    private static final String RETURN_URL = "https://service-name.gov.uk/transactions/12345";

    @Test
    public void createDirectDebitAgreement_withReference() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);
        
        connectorDDMock.respondOk_whenCreateAgreementRequest(
                MANDATE_ID,
                MandateType.ON_DEMAND,
                MANDATE_REFERENCE,
                SERVICE_REFERENCE,
                RETURN_URL,
                CREATED_DATE,
                new MandateState("created", false),
                GATEWAY_ACCOUNT_ID,
                CHARGE_TOKEN_ID
        );

        String payload = agreementPayload(RETURN_URL, AgreementType.ON_DEMAND);
        given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post("/v1/agreements")
                .then()
                .statusCode(201)
                .contentType(JSON)
                .header(HttpHeaders.LOCATION, is("http://publicapi.url/v1/agreements/mandateId"))
                .body("agreement_id", is(MANDATE_ID))
                .body("agreement_type", is(AgreementType.ON_DEMAND.toString()))
                .body("provider_id", is(MANDATE_REFERENCE))
                .body("reference", is(SERVICE_REFERENCE))
                .body("return_url", is(RETURN_URL))
                .body("created_date", is(CREATED_DATE))
                .body("state", is(AgreementStatus.CREATED.toString()))
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

    @Test
    public void createPayment_respondsWith500_whenConnectorResponseIsAnUnrecognisedError() {

        String errorMessage = "something went wrong";

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMock.respondBadRequest_whenCreateAgreementRequest(
                MandateType.ON_DEMAND,
                "https://service-name.gov.uk/transactions/12345",
                GATEWAY_ACCOUNT_ID,
                errorMessage
        );

        String payload = agreementPayload("https://service-name.gov.uk/transactions/12345", AgreementType.ON_DEMAND);
        given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post("/v1/agreements")
                .then()
                .statusCode(500)
                .contentType(JSON)
                .body("code", is("P0198"))
                .body("description", is("Downstream system error"))
                .extract().body().asString();
    }

    @Test
    public void shouldGetADirectDebitAgreement_withReference() {

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMock.respondOk_whenGetAgreementRequest(
                MANDATE_ID,
                MandateType.ON_DEMAND,
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
                .body("agreement_type", is(AgreementType.ON_DEMAND.toString()))
                .body("provider_id", is(MANDATE_REFERENCE))
                .body("reference", is(SERVICE_REFERENCE))
                .body("return_url", is(RETURN_URL))
                .body("state", is(AgreementStatus.CREATED.toString()))
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
        return "http://publicapi.url/v1/agreements/" + mandateId;
    }

    private static String agreementPayload(String returnUrl, AgreementType agreementType) {
        return new JsonStringBuilder()
                .add("return_url", returnUrl)
                .add("agreement_type", agreementType)
                .build();
    }
}
