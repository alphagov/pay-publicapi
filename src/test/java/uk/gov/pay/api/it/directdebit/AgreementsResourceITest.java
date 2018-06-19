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

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;

public class AgreementsResourceITest extends PaymentResourceITestBase {

    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final String CREATED_DATE = DateTimeUtils.toUTCDateString(TIMESTAMP);
    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";

    @Test
    public void createDirectDebitAgreement() {

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMock.respondOk_whenCreateAgreementRequest(
                "mandateId",
                MandateType.ON_DEMAND,
                "https://service-name.gov.uk/transactions/12345",
                CREATED_DATE,
                new MandateState("created", false),
                GATEWAY_ACCOUNT_ID,
                CHARGE_TOKEN_ID
        );

        String payload = agreementPayload("https://service-name.gov.uk/transactions/12345", AgreementType.ON_DEMAND);
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
                .body("agreement_id", is("mandateId"))
                .body("agreement_type", is(AgreementType.ON_DEMAND.toString()))
                .body("return_url", is("https://service-name.gov.uk/transactions/12345"))
                .body("created_date", is(CREATED_DATE))
                .body("state", is(AgreementStatus.CREATED.toString()))
                .body("_links.self.href", is(connectorDDMock.mandateLocation(GATEWAY_ACCOUNT_ID, "mandateId")))
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
    public void createPayment_respondsWith500_whenConnectorResponseIsAnUnrecognisedError() throws Exception {

        String errorMessage = "something went wrong";

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMock.respondBadRequest_whenCreateAgreementRequest(
                "mandateId",
                MandateType.ON_DEMAND,
                "https://service-name.gov.uk/transactions/12345",
                CREATED_DATE,
                new MandateState("created", false),
                GATEWAY_ACCOUNT_ID,
                CHARGE_TOKEN_ID,
                errorMessage
        );

        String payload = agreementPayload("https://service-name.gov.uk/transactions/12345", AgreementType.ON_DEMAND);
        String s = given().port(app.getLocalPort())
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

    private static String agreementPayload(String returnUrl, AgreementType agreementType) {
        return new JsonStringBuilder()
                .add("return_url", returnUrl)
                .add("agreement_type", agreementType)
                .build();
    }
}
