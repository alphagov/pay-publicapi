package uk.gov.pay.api.it.directdebit;

import org.junit.Test;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.model.directdebit.mandates.MandateState;
import uk.gov.pay.api.model.directdebit.mandates.Payer;
import uk.gov.pay.api.utils.PublicAuthMockClient;
import uk.gov.pay.api.utils.mocks.ConnectorDDMockClient;
import uk.gov.pay.api.utils.mocks.DDConnectorResponseToGetMandateParams;
import uk.gov.pay.commons.validation.DateTimeUtils;

import java.time.ZonedDateTime;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;
import static uk.gov.pay.api.utils.Urls.directDebitFrontendSecureUrl;
import static uk.gov.pay.api.utils.Urls.mandateLocationFor;
import static uk.gov.pay.api.utils.mocks.DDConnectorResponseToGetMandateParams.DDConnectorResponseToGetMandateParamsBuilder.aDDConnectorResponseToGetMandateParams;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class MandateResourceGetMandateIT extends PaymentResourceITestBase {

    private ConnectorDDMockClient connectorDDMockClient = new ConnectorDDMockClient(connectorDDMock);
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);

    private static final String CHARGE_TOKEN_ID = "token_1234567asdf";
    private static final String MANDATE_ID = "mandateId";
    private static final String MANDATE_REFERENCE = "test_mandate_reference";
    private static final String SERVICE_REFERENCE = "test_service_reference";
    private static final String RETURN_URL = "https://service-name.gov.uk/transactions/12345";
    private static final String PROVIDER_ID = "MD1234";
    private static final ZonedDateTime TIMESTAMP = DateTimeUtils.toUTCZonedDateTime("2016-01-01T12:00:00Z").get();
    private static final String CREATED_DATE = ISO_INSTANT_MILLISECOND_PRECISION.format(TIMESTAMP);

    @Test
    public void getMandate() {

        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        DDConnectorResponseToGetMandateParams params = aDDConnectorResponseToGetMandateParams()
                .withMandateId(MANDATE_ID)
                .withMandateReference(MANDATE_REFERENCE)
                .withServiceReference(SERVICE_REFERENCE)
                .withReturnUrl(RETURN_URL)
                .withState(new MandateState("created", false, "mandate_state_details"))
                .withGatewayAccountId(GATEWAY_ACCOUNT_ID)
                .withChargeTokenId(CHARGE_TOKEN_ID)
                .withProviderId(PROVIDER_ID)
                .withCreatedDate(CREATED_DATE)
                .withPayer(new Payer("Jack", "i.died@titanic.com"))
                .build();

        connectorDDMockClient.respondOk_whenGetMandateRequest(params);

        given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .get("/v1/directdebit/mandates/" + MANDATE_ID)
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("mandate_id", is(MANDATE_ID))
                .body("bank_statement_reference", is(MANDATE_REFERENCE))
                .body("provider_id", is(PROVIDER_ID))
                .body("reference", is(SERVICE_REFERENCE))
                .body("return_url", is(RETURN_URL))
                .body("state.status", is("created"))
                .body("state.details", is("mandate_state_details"))
                .body("created_date", is(CREATED_DATE))
                .body("payer.name", is("Jack"))
                .body("payer.email", is("i.died@titanic.com"))
                .body("_links.self.href", is(mandateLocationFor(MANDATE_ID)))
                .body("_links.self.method", is("GET"))
                .body("_links.next_url.href", is(directDebitFrontendSecureUrl() + CHARGE_TOKEN_ID))
                .body("_links.next_url.method", is("GET"))
                .body("_links.next_url_post.href", is(directDebitFrontendSecureUrl()))
                .body("_links.next_url_post.method", is("POST"))
                .body("_links.next_url_post.type", is("application/x-www-form-urlencoded"))
                .body("_links.next_url_post.params.chargeTokenId", is(CHARGE_TOKEN_ID))
                .body("_links.payments.href", is("http://publicapi.url/v1/directdebit/payments?mandate_id=" + MANDATE_ID))
                .body("_links.payments.method", is("GET"))
                .body("_links.events.href", is(format("http://publicapi.url/v1/directdebit/mandates/%s/events", MANDATE_ID)))
                .body("_links.events.method", is("GET"));
    }
}
