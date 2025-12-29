package uk.gov.pay.api.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.pay.api.utils.PublicAuthMockClientJUnit5;
import uk.gov.pay.api.utils.mocks.LedgerMockClientJUnit5;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.utils.mocks.AgreementFromLedgerFixture.AgreementFromLedgerFixtureBuilder.anAgreementFromLedgerWithoutPaymentInstrumentFixture;

class AgreementsApiResourceGetOneIT extends PaymentResourceITestBase {

    private final PublicAuthMockClientJUnit5 publicAuthMockClient = new PublicAuthMockClientJUnit5(publicAuthServer);
    private final LedgerMockClientJUnit5 ledgerMockClient = new LedgerMockClientJUnit5(ledgerServer);
    private final String agreementId = "an-agreement-id";

    @BeforeEach
    void setUp() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    void getAgreement() throws JsonProcessingException {
        var fixture = anAgreementFromLedgerWithoutPaymentInstrumentFixture()
                .withExternalId(agreementId)
                .build();
        ledgerMockClient.respondWithAgreement(agreementId, fixture);

        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .get(AGREEMENTS_PATH + agreementId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("agreement_id", is(agreementId))
                .body("$", not(hasKey("service_id")))
                .body("reference", is(fixture.getReference()))
                .body("description", is(fixture.getDescription()))
                .body("status", is(fixture.getStatus().toLowerCase()))
                .body("created_date", is(fixture.getCreatedDate()))
                .body("$", not(hasKey("user_identifier")))
                .body("$", not(hasKey("cancelled_date")))
                .body("$", not(hasKey("cancelled_by_user_email")));
    }

    @Test
    void getCancelledAgreement() throws JsonProcessingException {
        var fixture = anAgreementFromLedgerWithoutPaymentInstrumentFixture()
                .withExternalId(agreementId)
                .withStatus("CANCELLED")
                .withServiceId("service-id")
                .withCancelledDate("2023-06-14T12:40:00.000Z")
                .build();
        ledgerMockClient.respondWithAgreement(agreementId, fixture);

        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .get(AGREEMENTS_PATH + agreementId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("agreement_id", is(agreementId))
                .body("$", not(hasKey("service_id")))
                .body("reference", is(fixture.getReference()))
                .body("description", is(fixture.getDescription()))
                .body("status", is(fixture.getStatus().toLowerCase()))
                .body("created_date", is(fixture.getCreatedDate()))
                .body("$", not(hasKey("user_identifier")))
                .body("cancelled_date", is(fixture.getCancelledDate()));
    }

    @Test
    void getAgreement_MissingAgreementShouldMapException() {
        ledgerMockClient.respondAgreementNotFound(agreementId);
        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .get(AGREEMENTS_PATH + agreementId)
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("code", is("P2200"))
                .body("description", is("Not found"));
    }
}
