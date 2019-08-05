package uk.gov.pay.api.it.directdebit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.model.PaymentConnectorResponseLink;
import uk.gov.pay.api.model.directdebit.mandates.MandateConnectorResponse;
import uk.gov.pay.api.model.directdebit.mandates.MandateState;
import uk.gov.pay.api.model.directdebit.mandates.Payer;
import uk.gov.pay.api.model.links.SearchNavigationLinks;
import uk.gov.pay.api.model.search.directdebit.MandateSearchConnectorResponse;
import uk.gov.pay.api.utils.PublicAuthMockClient;
import uk.gov.pay.api.utils.mocks.ConnectorDDMockClient;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;
import static uk.gov.pay.api.model.directdebit.mandates.MandateConnectorResponse.MandateConnectorResponseBuilder.aMandateConnectorResponse;
import static uk.gov.pay.api.model.search.directdebit.MandateSearchConnectorResponse.SearchMandateConnectorResponseBuilder.aSearchMandateConnectorResponse;
import static uk.gov.pay.api.utils.Urls.mandateLocationFor;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class MandateResourceSearchMandateIT extends PaymentResourceITestBase {

    private ConnectorDDMockClient connectorDDMockClient = new ConnectorDDMockClient(connectorDDMock);
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);

    private static final String MANDATE_ID = "mandateId";
    private static final String MANDATE_REFERENCE = "test_mandate_reference";
    private static final String SERVICE_REFERENCE = "test_service_reference";
    private static final String RETURN_URL = "https://service-name.gov.uk/transactions/12345";
    private static final String PROVIDER_ID = "MD1234";
    private static final String CREATED_DATE = ISO_INSTANT_MILLISECOND_PRECISION.format(ZonedDateTime.parse("2016-01-01T12:00:00Z"));
    private static final String PAYER_NAME = "payer";
    private static final String PAYER_EMAIL = "payer@example.com";
    public static final String DD_CONNECTOR_BASE_SEARCH_URL = "https://connector/v1/api/accounts/%s/mandates?page=1&display_size=500";

    @Before
    public void setup() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);
    }

    @Test
    public void shouldReturnErrorForInvalidPageNumber() {
        given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .queryParam("page", -1)
                .get("/v1/directdebit/mandates/")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("field", is("page"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: page. Must be greater than or equal to 1"));
    }

    @Test
    public void shouldReturnErrorForInvalidFromDate() {
        given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .queryParam("from_date", "this isn't a date")
                .get("/v1/directdebit/mandates/")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("field", is("from_date"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: from_date. Must be a valid date"));
    }

    @Test
    public void shouldReturnErrorForInvalidToDate() {
        given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .queryParam("to_date", "this isn't a date")
                .get("/v1/directdebit/mandates/")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("field", is("to_date"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: to_date. Must be a valid date"));
    }

    @Test
    public void shouldReturnErrorForDisplaySizeTooGreat() {
        given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .queryParam("display_size", 501)
                .get("/v1/directdebit/mandates/")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("field", is("display_size"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: display_size. Must be less than or equal to 500"));
    }

    @Test
    public void shouldReturnErrorForDisplaySizeTooSmall() {
        given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .queryParam("display_size", 0)
                .get("/v1/directdebit/mandates/")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("field", is("display_size"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: display_size. Must be greater than or equal to 1"));
    }

    @Test
    public void shouldReturnErroForInvalidMandateState() {
        given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .queryParam("state", "doesn't exist")
                .get("/v1/directdebit/mandates/")
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("field", is("state"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: state. Must be one of created, started, pending, active, inactive, cancelled, failed, abandoned or error"));
    }

    @Test
    public void shouldSuccessfullySearchMandate() throws JsonProcessingException {
        var searchNavigationLinksFromConnector = new SearchNavigationLinks()
                .withFirstLink(DD_CONNECTOR_BASE_SEARCH_URL + "&firstLink")
                .withLastLink(DD_CONNECTOR_BASE_SEARCH_URL + "&lastLink")
                .withNextLink(DD_CONNECTOR_BASE_SEARCH_URL + "&nextLink")
                .withSelfLink(DD_CONNECTOR_BASE_SEARCH_URL + "&selfLink");

        var selfLink = new PaymentConnectorResponseLink("self", "https://connector", "GET", null, null);

        MandateConnectorResponse mandate = aMandateConnectorResponse()
                .withMandateId(MANDATE_ID)
                .withMandateReference(MANDATE_REFERENCE)
                .withServiceReference(SERVICE_REFERENCE)
                .withReturnUrl(RETURN_URL)
                .withState(new MandateState("created", false, "mandate_state_details"))
                .withProviderId(PROVIDER_ID)
                .withCreatedDate(CREATED_DATE)
                .withPayer(new Payer(PAYER_NAME, PAYER_EMAIL))
                .withLinks(Collections.singletonList(selfLink))
                .build();

        MandateSearchConnectorResponse connectorResponse = aSearchMandateConnectorResponse()
                .withCount(1)
                .withPage(1)
                .withTotal(1)
                .withMandates(Collections.singletonList(mandate))
                .withLinks(searchNavigationLinksFromConnector)
                .build();

        String toDate = ISO_INSTANT_MILLISECOND_PRECISION.format(ZonedDateTime.now());
        String fromDate = ISO_INSTANT_MILLISECOND_PRECISION.format(ZonedDateTime.now().minusDays(1));

        Map<String, StringValuePattern>  searchParams = Map.of(
                "reference", equalTo(SERVICE_REFERENCE),
                "page", equalTo("1"),
                "display_size", equalTo("500"),
                "state", equalTo("created"),
                "to_date", equalTo(toDate),
                "from_date", equalTo(fromDate)
        );

        connectorDDMockClient.respondOk_whenSearchMandatesRequest(searchParams, connectorResponse, GATEWAY_ACCOUNT_ID);

        given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .queryParam("reference", SERVICE_REFERENCE)
                .queryParam("state", "created")
                .queryParam("to_date", toDate)
                .queryParam("from_date", fromDate)
                .get("/v1/directdebit/mandates/")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("total", is(1))
                .body("count", is(1))
                .body("page", is(1))
                .body("results[0].mandate_id", is(MANDATE_ID))
                .body("results[0].provider_id", is(PROVIDER_ID))
                .body("results[0].reference", is(SERVICE_REFERENCE))
                .body("results[0].return_url", is(RETURN_URL))
                .body("results[0].state.status", is("created"))
                .body("results[0].state.details", is("mandate_state_details"))
                .body("results[0].created_date", is(CREATED_DATE))
                .body("results[0].payer.name", is(PAYER_NAME))
                .body("results[0].payer.email", is(PAYER_EMAIL))
                .body("results[0]._links.self.href", is(mandateLocationFor(MANDATE_ID)))
                .body("results[0]._links.self.method", is("GET"))
                .body("results[0]._links.payments.href", is("http://publicapi.url/v1/directdebit/payments?mandate_id=" + MANDATE_ID))
                .body("results[0]._links.payments.method", is("GET"))
                .body("_links.self.href", is("http://publicapi.url/v1/directdebit/mandates?page=1&display_size=500&selfLink"))
                .body("_links.first_page.href", is("http://publicapi.url/v1/directdebit/mandates?page=1&display_size=500&firstLink"))
                .body("_links.last_page.href", is("http://publicapi.url/v1/directdebit/mandates?page=1&display_size=500&lastLink"))
                .body("_links.next_page.href", is("http://publicapi.url/v1/directdebit/mandates?page=1&display_size=500&nextLink"));
    }
}
