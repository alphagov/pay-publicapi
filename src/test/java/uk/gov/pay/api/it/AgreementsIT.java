package uk.gov.pay.api.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.utils.PublicAuthMockClient;
import uk.gov.pay.api.utils.mocks.LedgerMockClient;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.utils.mocks.AgreementFromLedgerFixture.AgreementFromLedgerFixtureBuilder.anAgreementFromLedgerFixture;

public class AgreementsIT extends PaymentResourceITestBase {
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);
    private LedgerMockClient ledgerMockClient = new LedgerMockClient(ledgerMock);

    private final String agreementId = "an-agreement-id";

    @Before
    public void setUp() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void getAgreementFromLedger() throws JsonProcessingException {
        var fixture = anAgreementFromLedgerFixture()
                .withExternalId(agreementId)
                .build();
        ledgerMockClient.respondWithAgreement(agreementId, fixture);

        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .get(AGREEMENTS_PATH + agreementId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", is(agreementId))
                .body("service_id", is(fixture.getServiceId()))
                .body("reference", is(fixture.getReference()))
                .body("description", is(fixture.getDescription()))
                .body("status", is(fixture.getStatus().toLowerCase()))
                .body("created_date", is(fixture.getCreatedDate()));
    }

    @Test
    public void getAgreement_MissingAgreementShouldMapException() {
        ledgerMockClient.respondAgreementNotFound(agreementId);
        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .get(AGREEMENTS_PATH + agreementId)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("code", is("P0200"))
                .body("description", is("Not found"));
    }
}
