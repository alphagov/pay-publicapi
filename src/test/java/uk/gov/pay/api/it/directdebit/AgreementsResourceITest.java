package uk.gov.pay.api.it.directdebit;

import org.junit.Test;
import uk.gov.pay.api.model.directdebit.AgreementStatus;
import uk.gov.pay.api.model.directdebit.AgreementType;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.ws.rs.core.HttpHeaders;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;

public class AgreementsResourceITest extends DirectDebitPaymentsResourceITest {

    @Test
    public void createDirectDebitAgreement() {

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMock.respondOk_whenCreateAgreementRequest(
                "mock agreement",
                "agreement@example.com",
                AgreementType.ON_DEMAND,
                "agreementId",
                AgreementStatus.CREATED,
                GATEWAY_ACCOUNT_ID
        );

        String payload = agreementPayload("mock agreement", "agreement@example.com", AgreementType.ON_DEMAND);
        given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .post("/v1/agreements")
                .then()
                .statusCode(201)
                .contentType(JSON)
                .header(HttpHeaders.LOCATION, is("http://publicapi.url/v1/agreements/agreementId"))
                .body("name", is("mock agreement"))
                .body("email", is("agreement@example.com"))
                .body("type", is(AgreementType.ON_DEMAND.name()))
                .body("agreement_id", is("agreementId"))
                .body("status", is(AgreementStatus.CREATED.name()))
                .extract().body().asString();
    }

    private static String agreementPayload(String name, String email, AgreementType agreementType) {
        return new JsonStringBuilder()
                .add("name", name)
                .add("email", email)
                .add("type", agreementType)
                .build();
    }
}
