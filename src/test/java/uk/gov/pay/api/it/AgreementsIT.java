package uk.gov.pay.api.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.utils.PublicAuthMockClient;
import uk.gov.pay.api.utils.mocks.ConnectorMockClient;
import uk.gov.pay.api.utils.mocks.LedgerMockClient;

import static io.restassured.RestAssured.given;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.utils.mocks.AgreementFromLedgerFixture.AgreementFromLedgerFixtureBuilder.anAgreementFromLedgerWithPaymentInstrumentFixture;
import static uk.gov.pay.api.utils.mocks.AgreementFromLedgerFixture.AgreementFromLedgerFixtureBuilder.anAgreementFromLedgerWithoutPaymentInstrumentFixture;

public class AgreementsIT extends PaymentResourceITestBase {

    private final PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);
    private final LedgerMockClient ledgerMockClient = new LedgerMockClient(ledgerMock);
    private final ConnectorMockClient connectorMockClient = new ConnectorMockClient(connectorMock);

    private final String agreementId = "an-agreement-id";

    @Before
    public void setUp() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void getAgreementFromLedger() throws JsonProcessingException {
        var fixture = anAgreementFromLedgerWithoutPaymentInstrumentFixture()
                .withExternalId(agreementId)
                .build();
        ledgerMockClient.respondWithAgreement(agreementId, fixture);

        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .get(AGREEMENTS_PATH + agreementId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("agreement_id", is(agreementId))
                .body("$", not(hasKey("service_id")))
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
                .body("code", is("P2200"))
                .body("description", is("Not found"));
    }

    @Test
    public void searchAgreementsFromLedger() throws JsonProcessingException {
        var agreementWithPaymentInstrument = anAgreementFromLedgerWithPaymentInstrumentFixture()
                .withExternalId(agreementId)
                .build();

        var agreementWithoutPaymentInstrument = anAgreementFromLedgerWithoutPaymentInstrumentFixture()
                .withExternalId("another-agreement-id")
                .withServiceId("service-2")
                .withReference("ref-2")
                .withDescription("Description 2")
                .withStatus("CREATED")
                .withCreatedDate("2022-07-27T12:30:00Z")
                .build();

        ledgerMockClient.respondWithSearchAgreements(GATEWAY_ACCOUNT_ID, agreementWithPaymentInstrument, agreementWithoutPaymentInstrument);

        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .basePath(AGREEMENTS_PATH)
                .queryParam("status", "created")
                .queryParam("page", "3")
                .get()
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("total", is(9))
                .body("count", is(2))
                .body("page", is(3))
                .body("results.size()", is(2))
                .body("results[0].agreement_id", is(agreementWithPaymentInstrument.getExternalId()))
                .body("results[0].reference", is(agreementWithPaymentInstrument.getReference()))
                .body("results[0].description", is(agreementWithPaymentInstrument.getDescription()))
                .body("results[0].status", is(agreementWithPaymentInstrument.getStatus().toLowerCase()))
                .body("results[0].created_date", is(agreementWithPaymentInstrument.getCreatedDate()))
                .body("results[0].payment_instrument.type", is(agreementWithPaymentInstrument.getPaymentInstrument().getType()))
                .body("results[0].payment_instrument.card_details.card_type",
                        is(agreementWithPaymentInstrument.getPaymentInstrument().getCardDetails().getCardType()))
                .body("results[0].payment_instrument.card_details.card_brand",
                        is(agreementWithPaymentInstrument.getPaymentInstrument().getCardDetails().getCardBrand()))
                .body("results[0].payment_instrument.card_details.cardholder_name",
                        is(agreementWithPaymentInstrument.getPaymentInstrument().getCardDetails().getCardHolderName()))
                .body("results[0].payment_instrument.card_details.billing_address.line1",
                        is(agreementWithPaymentInstrument.getPaymentInstrument().getCardDetails().getBillingAddress().get().getLine1()))
                .body("results[0].payment_instrument.card_details.billing_address.line2",
                        is(agreementWithPaymentInstrument.getPaymentInstrument().getCardDetails().getBillingAddress().get().getLine2()))
                .body("results[0].payment_instrument.card_details.billing_address.city",
                        is(agreementWithPaymentInstrument.getPaymentInstrument().getCardDetails().getBillingAddress().get().getCity()))
                .body("results[0].payment_instrument.card_details.billing_address.postcode",
                        is(agreementWithPaymentInstrument.getPaymentInstrument().getCardDetails().getBillingAddress().get().getPostcode()))
                .body("results[0].payment_instrument.card_details.billing_address.country",
                        is(agreementWithPaymentInstrument.getPaymentInstrument().getCardDetails().getBillingAddress().get().getCountry()))
                .body("results[0].payment_instrument.card_details.expiry_date",
                        is(agreementWithPaymentInstrument.getPaymentInstrument().getCardDetails().getExpiryDate()))
                .body("results[0].payment_instrument.card_details.first_digits_card_number",
                        is(agreementWithPaymentInstrument.getPaymentInstrument().getCardDetails().getFirstDigitsCardNumber()))
                .body("results[0].payment_instrument.card_details.last_digits_card_number",
                        is(agreementWithPaymentInstrument.getPaymentInstrument().getCardDetails().getLastDigitsCardNumber()))
                .body("results[0].payment_instrument.created_date", is(agreementWithPaymentInstrument.getPaymentInstrument().getCreatedDate()))
                .body("results[1].agreement_id", is(agreementWithoutPaymentInstrument.getExternalId()))
                .body("results[1].reference", is(agreementWithoutPaymentInstrument.getReference()))
                .body("results[1].description", is(agreementWithoutPaymentInstrument.getDescription()))
                .body("results[1].status", is(agreementWithoutPaymentInstrument.getStatus().toLowerCase()))
                .body("results[1].created_date", is(agreementWithoutPaymentInstrument.getCreatedDate()))
                .body("results[1]", not(hasKey("payment_instrument")));
    }

    @Test
    public void searchAgreementsReturnsErrorWhenValidationError() throws Exception {
        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .basePath(AGREEMENTS_PATH)
                .queryParam("status", "ethereal")
                .get()
                .then()
                .statusCode(422)
                .contentType(ContentType.JSON)
                .body("code", is("P2401"))
                .body("description", is("Invalid parameters: status. See Public API documentation for the correct data formats"));
    }

    @Test
    public void cancelAgreement() {
        connectorMockClient.respondOk_whenCancelAgreement(agreementId, GATEWAY_ACCOUNT_ID);
        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .post(AGREEMENTS_PATH + agreementId + "/cancel")
                .then()
                .statusCode(204);
    }

    @Test
    public void cancelAgreementReturnsErrorWhenConnectorRespondsAgreementNotFound() {
        connectorMockClient.respondAgreementNotFound_WhenCancelAgreement(agreementId, GATEWAY_ACCOUNT_ID, "error message");
        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .post(AGREEMENTS_PATH + agreementId + "/cancel")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("code", is("P2500"))
                .body("description", is("Not found"));
    }

    @Test
    public void cancelAgreementReturnsErrorWhenConnectorRespondsAgreementNotActive() {
        connectorMockClient.respondAgreementNotActive_WhenCancelAgreement(agreementId, GATEWAY_ACCOUNT_ID, "error message");
        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .post(AGREEMENTS_PATH + agreementId + "/cancel")
                .then()
                .statusCode(400)
                .contentType(ContentType.JSON)
                .body("code", is("P2501"))
                .body("description", is("Cancellation of agreement failed"));
    }

    @Test
    public void cancelAgreementReturnsErrorWhenConnectorRespondsWithError() {
        connectorMockClient.respondError_WhenCancelAgreement(agreementId, GATEWAY_ACCOUNT_ID, "error message");
        given().port(app.getLocalPort())
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .post(AGREEMENTS_PATH + agreementId + "/cancel")
                .then()
                .statusCode(500)
                .contentType(ContentType.JSON)
                .body("code", is("P2598"))
                .body("description", is("Downstream system error"));
    }

}
