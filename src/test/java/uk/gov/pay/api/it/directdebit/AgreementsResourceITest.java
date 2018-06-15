package uk.gov.pay.api.it.directdebit;

import org.junit.Test;
import uk.gov.pay.api.it.PaymentsResourceITest;
import uk.gov.pay.api.model.directdebit.agreement.AgreementStatus;
import uk.gov.pay.api.model.directdebit.agreement.AgreementType;
import uk.gov.pay.api.utils.DateTimeUtils;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.ws.rs.core.HttpHeaders;
import java.time.ZonedDateTime;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;

public class AgreementsResourceITest extends PaymentsResourceITest {

    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final String CREATED_DATE = DateTimeUtils.toUTCDateString(TIMESTAMP);

    @Test
    public void createDirectDebitAgreement() {

        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMock.respondOk_whenCreateAgreementRequest(
                "mandateId",
                AgreementType.ON_DEMAND,
                "https://service-name.gov.uk/transactions/12345",
                CREATED_DATE,
                AgreementStatus.CREATED,
                GATEWAY_ACCOUNT_ID
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
                .body("agreement_type", is(AgreementType.ON_DEMAND.name()))
                .body("return_url", is("https://service-name.gov.uk/transactions/12345"))
                .body("created_date", is(CREATED_DATE))
                .body("state", is(AgreementStatus.CREATED.name()))
                .extract().body().asString();
    }

    private static String agreementPayload(String returnUrl, AgreementType agreementType) {
        return new JsonStringBuilder()
                .add("return_url", returnUrl)
                .add("agreement_type", agreementType)
                .build();
    }
}
