package uk.gov.pay.api.it;


import com.google.common.collect.ImmutableMap;
import com.jayway.jsonassert.JsonAssert;
import com.jayway.restassured.response.ValidatableResponse;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.it.fixtures.PaymentNavigationLinksFixture;
import uk.gov.pay.api.utils.DateTimeUtils;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockserver.model.HttpResponse.response;
import static uk.gov.pay.api.it.fixtures.PaginatedPaymentSearchResultFixture.aPaginatedPaymentSearchResult;
import static uk.gov.pay.api.it.fixtures.PaymentSearchResultBuilder.*;

public class PaymentResourceSearchITest extends PaymentResourceITestBase {

    private static final String TEST_REFERENCE = "test_reference";
    private static final String TEST_STATE = "created";
    private static final String TEST_FROM_DATE = "2016-01-28T00:00:00Z";
    private static final String TEST_TO_DATE = "2016-01-28T12:00:00Z";
    private static final String SEARCH_PATH = "/v1/payments";

    @Before
    public void mapBearerTokenToAccountId() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void searchPayments_shouldOnlyReturnAllowedProperties() {
        String payments = aPaginatedPaymentSearchResult()
                .withCount(10)
                .withPage(2)
                .withTotal(20)
                .withPayments(aSuccessfulSearchPayment()
                        .withMatchingInProgressState(TEST_STATE)
                        .withMatchingReference(TEST_REFERENCE)
                        .withNumberOfResults(1)
                        .getResults())
                .build();

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, null, null, null, payments);

        String responseBody = searchPayments(API_KEY, ImmutableMap.of("reference", TEST_REFERENCE))
                .statusCode(200)
                .contentType(JSON)
                .body("results[0].created_date", is(DEFAULT_CREATED_DATE))
                .body("results[0].reference", is(TEST_REFERENCE))
                .body("results[0].return_url", is(DEFAULT_RETURN_URL))
                .body("results[0].description", is("description-0"))
                .body("results[0].state.status", is(TEST_STATE))
                .body("results[0].amount", is(DEFAULT_AMOUNT))
                .body("results[0].payment_provider", is(DEFAULT_PAYMENT_PROVIDER))
                .body("results[0].payment_id", is("0"))
                .body("results[0]._links.self.method", is("GET"))
                .body("results[0]._links.self.href", is(paymentLocationFor("0")))
                .body("results[0]._links.events.href", is(paymentEventsLocationFor("0")))
                .body("results[0]._links.events.method", is("GET"))
                .body("results[0]._links.cancel.href", is(paymentCancelLocationFor("0")))
                .body("results[0]._links.cancel.method", is("POST"))
                .body("results[0]._links.refunds.href", is(paymentRefundsLocationFor("0")))
                .body("results[0]._links.refunds.method", is("GET"))
                .body("results[0].refund_summary.status", is("available"))
                .body("results[0].refund_summary.amount_available", is(100))
                .body("results[0].refund_summary.amount_submitted", is(300))
                .extract().asString();

        JsonAssert.with(responseBody)
                .assertNotDefined("_links.self.type")
                .assertNotDefined("_links.self.params")
                .assertNotDefined("_links.next_url.type")
                .assertNotDefined("_links.next_url.params")
                .assertNotDefined("_links.events.type")
                .assertNotDefined("_links.events.params");

    }

    @Test
    public void searchPayments_ShouldNotIncludeCancelLinkIfThePaymentCannotBeCancelled() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        String SUCCEEDED_STATE = "success";

        String payments = aPaginatedPaymentSearchResult()
                .withCount(10)
                .withPage(2)
                .withTotal(20)
                .withPayments(aSuccessfulSearchPayment()
                        .withMatchingSuccessState(SUCCEEDED_STATE)
                        .withMatchingReference(TEST_REFERENCE)
                        .withNumberOfResults(1)
                        .getResults())
                .build();

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, null, null, null,
                payments
        );

        searchPayments(API_KEY, ImmutableMap.of("reference", TEST_REFERENCE))
                .statusCode(200)
                .contentType(JSON)
                .body("results[0]._links.cancel", is(nullValue()));

    }

    @Test
    public void searchPayments_filterByFullReference() throws Exception {
        String payments = aPaginatedPaymentSearchResult()
                .withCount(10)
                .withPage(2)
                .withTotal(20)
                .withPayments(aSuccessfulSearchPayment()
                        .withMatchingReference(TEST_REFERENCE)
                        .getResults())
                .build();

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, null, null, null,
                payments
        );

        ValidatableResponse response = searchPayments(API_KEY, ImmutableMap.of("reference", TEST_REFERENCE))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("reference", TEST_REFERENCE));
    }


    @Test
    public void searchPayments_filterByState() throws Exception {
        String payments = aPaginatedPaymentSearchResult()
                .withCount(10)
                .withPage(2)
                .withTotal(20)
                .withPayments(aSuccessfulSearchPayment()
                        .withMatchingInProgressState(TEST_STATE)
                        .getResults())
                .build();

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null, TEST_STATE, null, null,
                payments
        );

        ValidatableResponse response = searchPayments(API_KEY, ImmutableMap.of("state", TEST_STATE))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesState(TEST_STATE));
    }

    @Test
    public void searchPayments_filterByStateLowercase() throws Exception {
        String payments = aPaginatedPaymentSearchResult()
                .withCount(10)
                .withPage(2)
                .withTotal(20)
                .withPayments(aSuccessfulSearchPayment()
                        .withMatchingInProgressState(TEST_STATE)
                        .getResults())
                .build();

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null, TEST_STATE, null, null,
                payments
        );

        ValidatableResponse response = searchPayments(API_KEY, ImmutableMap.of("state", TEST_STATE.toLowerCase()))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesState(TEST_STATE));
    }

    @Test
    public void searchPayments_filterFromAndToDates() throws Exception {
        String payments = aPaginatedPaymentSearchResult()
                .withCount(10)
                .withPage(2)
                .withTotal(20)
                .withPayments(aSuccessfulSearchPayment()
                        .withCreatedDateBetween(TEST_FROM_DATE, TEST_TO_DATE).getResults())
                .build();

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null, null, TEST_FROM_DATE, TEST_TO_DATE,
                payments
        );

        ValidatableResponse response = searchPayments(API_KEY, ImmutableMap.of("from_date", TEST_FROM_DATE, "to_date", TEST_TO_DATE))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");

        assertThat(results, matchesCreatedDateInBetween(TEST_FROM_DATE, TEST_TO_DATE));

    }

    @Test
    public void searchPayments_filterByReferenceStateAndFromToDates() throws Exception {
        String payments = aPaginatedPaymentSearchResult()
                .withCount(10)
                .withPage(2)
                .withTotal(20)
                .withPayments(aSuccessfulSearchPayment()
                        .withMatchingReference(TEST_REFERENCE)
                        .withMatchingInProgressState(TEST_STATE)
                        .withCreatedDateBetween(TEST_FROM_DATE, TEST_TO_DATE).getResults())
                .build();

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, TEST_STATE, TEST_FROM_DATE, TEST_TO_DATE,
                payments
        );

        ValidatableResponse response = searchPayments(API_KEY, ImmutableMap.of("reference", TEST_REFERENCE, "state", TEST_STATE, "from_date", TEST_FROM_DATE, "to_date", TEST_TO_DATE))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("reference", TEST_REFERENCE));
        assertThat(results, matchesState(TEST_STATE));
        assertThat(results, matchesCreatedDateInBetween(TEST_FROM_DATE, TEST_TO_DATE));

    }

    @Test
    public void searchPayments_getsPaginatedResultsFromConnector() throws Exception {

        PaymentNavigationLinksFixture links = new PaymentNavigationLinksFixture()
                .withPrevLink("http://server:port/path?query=prev")
                .withNextLink("http://server:port/path?query=next")
                .withSelfLink("http://server:port/path?query=self")
                .withFirstLink("http://server:port/path?query=first")
                .withLastLink("http://server:port/path?query=last");

        String payments = aPaginatedPaymentSearchResult()
                .withCount(10)
                .withPage(2)
                .withTotal(40)
                .withPayments(aSuccessfulSearchPayment()
                        .withMatchingReference(TEST_REFERENCE)
                        .withMatchingInProgressState(TEST_STATE)
                        .withCreatedDateBetween(TEST_FROM_DATE, TEST_TO_DATE)
                        .withNumberOfResults(10)
                        .getResults())
                .withLinks(links)
                .build();

        connectorMock.respondOk_whenSearchChargesWithPageAndSize(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, "2", "10",
                payments
        );
        ImmutableMap<String, String> queryParams = ImmutableMap.of(
                "reference", TEST_REFERENCE,
                "state", TEST_STATE,
                "page", "2",
                "display_size", "10"
        );
        ValidatableResponse response = searchPayments(API_KEY, queryParams)
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(10))
                .body("total", is(40))
                .body("count", is(10))
                .body("page", is(2))
                .body("_links.next_page.href", Matchers.is(expectedChargesLocationFor("?query=next")))
                .body("_links.prev_page.href", Matchers.is(expectedChargesLocationFor("?query=prev")))
                .body("_links.first_page.href", Matchers.is(expectedChargesLocationFor("?query=first")))
                .body("_links.last_page.href", Matchers.is(Matchers.is(expectedChargesLocationFor("?query=last"))))
                .body("_links.self.href", Matchers.is(Matchers.is(expectedChargesLocationFor("?query=self"))));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("reference", TEST_REFERENCE));
        assertThat(results, matchesState(TEST_STATE));
        assertThat(results, matchesCreatedDateInBetween(TEST_FROM_DATE, TEST_TO_DATE));

    }

    private String expectedChargesLocationFor(String queryParams) {
        return "http://localhost:" + app.getLocalPort()
                + SEARCH_PATH
                + queryParams;
    }

    @Test
    public void searchPayments_errorIfConnectorRespondsWith404() throws Exception {
        InputStream body = searchPayments(API_KEY,
                ImmutableMap.of("reference", TEST_REFERENCE, "state", TEST_STATE, "from_date", TEST_FROM_DATE, "to_date", TEST_TO_DATE))
                .statusCode(404)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0402"))
                .assertThat("$.description", is("Page not found"));
    }

    @Test
    public void searchPayments_errorIfConnectorResponseIsInvalid() throws Exception {
        connectorMock.whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, TEST_STATE, TEST_FROM_DATE, TEST_TO_DATE)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("wtf"));

        InputStream body = searchPayments(API_KEY,
                ImmutableMap.of("reference", TEST_REFERENCE, "state", TEST_STATE, "from_date", TEST_FROM_DATE, "to_date", TEST_TO_DATE))
                .statusCode(500)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0498"))
                .assertThat("$.description", is("Downstream system error"));
    }

    private Matcher<? super List<Map<String, Object>>> matchesState(final String state) {
        return new TypeSafeMatcher<List<Map<String, Object>>>() {
            @Override
            protected boolean matchesSafely(List<Map<String, Object>> maps) {
                return maps.stream().allMatch(result -> state.equals(((Map<String, Object>) result.get("state")).get("status")));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("result state did not match %s", state));
            }
        };
    }

    private Matcher<? super List<Map<String, Object>>> matchesField(final String field, final String value) {
        return new TypeSafeMatcher<List<Map<String, Object>>>() {
            @Override
            protected boolean matchesSafely(List<Map<String, Object>> maps) {
                return maps.stream().allMatch(result -> value.equals(result.get(field)));
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("not all result %s match %s", field, value));
            }
        };
    }

    private Matcher<? super List<Map<String, Object>>> matchesCreatedDateInBetween(final String fromDate, final String toDate) {
        return new TypeSafeMatcher<List<Map<String, Object>>>() {
            @Override
            protected boolean matchesSafely(List<Map<String, Object>> results) {
                return results.stream().allMatch(result -> {
                            ZonedDateTime createdDate = zonedDateTimeOf(result.get("created_date").toString());
                            return createdDate.isAfter(zonedDateTimeOf(fromDate)) && createdDate.isBefore(zonedDateTimeOf(toDate));
                        }

                );
            }

            @Override
            public void describeTo(Description description) {
                description.appendText(String.format("created date of results does not fall in between %s and %s", fromDate, toDate));
            }
        };
    }

    private ZonedDateTime zonedDateTimeOf(String dateString) {
        return DateTimeUtils.toUTCZonedDateTime(dateString).get();
    }

    private ValidatableResponse searchPayments(String bearerToken, ImmutableMap<String, String> queryParams) {
        return given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .queryParameters(queryParams)
                .get(SEARCH_PATH)
                .then();
    }

}
