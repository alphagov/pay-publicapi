package uk.gov.pay.api.it;


import com.google.common.collect.ImmutableMap;
import com.jayway.jsonassert.JsonAssert;
import com.jayway.restassured.response.ValidatableResponse;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
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
import static uk.gov.pay.api.it.fixtures.PaymentSearchResultBuilder.*;

public class PaymentResourceSearchITest extends PaymentResourceITestBase {

    private static final String TEST_REFERENCE = "test_reference";
    private static final String TEST_STATUS = "created";
    private static final String TEST_FROM_DATE = "2016-01-28T00:00:00Z";
    private static final String TEST_TO_DATE = "2016-01-28T12:00:00Z";
    private static final String SEARCH_PATH = "/v1/payments";

    @Before
    public void mapBearerTokenToAccountId() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void searchPayments_shouldOnlyReturnAllowedProperties() {

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, null, null, null,
                aSuccessfulSearchResponse()
                        .withMatchingStatus(TEST_STATUS)
                        .withMatchingReference(TEST_REFERENCE)
                        .numberOfResults(1)
                        .build()
        );

        String responseBody = searchPayments(API_KEY, ImmutableMap.of("reference", TEST_REFERENCE))
                .statusCode(200)
                .contentType(JSON)
                .body("results[0].created_date", is(DEFAULT_CREATED_DATE))
                .body("results[0].reference", is(TEST_REFERENCE))
                .body("results[0].return_url", is(DEFAULT_RETURN_URL))
                .body("results[0].description", is("description-0"))
                .body("results[0].status", is(TEST_STATUS))
                .body("results[0].amount", is(DEFAULT_AMOUNT))
                .body("results[0].payment_provider", is(DEFAULT_PAYMENT_PROVIDER))
                .body("results[0].payment_id", is("0"))
                .body("results[0]._links.self.method", is("GET"))
                .body("results[0]._links.self.href", is(paymentLocationFor("0")))
                .body("results[0]._links.events.href", is(paymentEventsLocationFor("0")))
                .body("results[0]._links.events.method", is("GET"))
                .body("results[0]._links.cancel.href", is(paymentCancelLocationFor("0")))
                .body("results[0]._links.cancel.method", is("POST"))
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
        String SUCCEEDED_STATUS = "succeeded";
        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, null, null, null,
                aSuccessfulSearchResponse()
                        .withMatchingStatus(SUCCEEDED_STATUS)
                        .withMatchingReference(TEST_REFERENCE)
                        .numberOfResults(1)
                        .build()
        );

        searchPayments(API_KEY, ImmutableMap.of("reference", TEST_REFERENCE))
                .statusCode(200)
                .contentType(JSON)
                .body("results[0]._links.cancel", is(nullValue()));

    }

    @Test
    public void searchPayments_filterByFullReference() throws Exception {

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, null, null, null,
                aSuccessfulSearchResponse()
                        .withMatchingReference(TEST_REFERENCE)
                        .build()
        );

        ValidatableResponse response = searchPayments(API_KEY, ImmutableMap.of("reference", TEST_REFERENCE))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("reference", TEST_REFERENCE));
    }


    @Test
    public void searchPayments_filterByStatus() throws Exception {

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null, TEST_STATUS, null, null,
                aSuccessfulSearchResponse()
                        .withMatchingStatus(TEST_STATUS)
                        .build()
        );

        ValidatableResponse response = searchPayments(API_KEY, ImmutableMap.of("status", TEST_STATUS))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("status", TEST_STATUS));
    }

    @Test
    public void searchPayments_filterByStatusLowercase() throws Exception {

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null, TEST_STATUS, null, null,
                aSuccessfulSearchResponse()
                        .withMatchingStatus(TEST_STATUS)
                        .build()
        );

        ValidatableResponse response = searchPayments(API_KEY, ImmutableMap.of("status", TEST_STATUS.toLowerCase()))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("status", TEST_STATUS));
    }

    @Test
    public void searchPayments_filterFromAndToDates() throws Exception {

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null, null, TEST_FROM_DATE, TEST_TO_DATE,
                aSuccessfulSearchResponse()
                        .withCreatedDateBetween(TEST_FROM_DATE, TEST_TO_DATE)
                        .build()
        );

        ValidatableResponse response = searchPayments(API_KEY, ImmutableMap.of("from_date", TEST_FROM_DATE, "to_date", TEST_TO_DATE))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");

        assertThat(results, matchesCreatedDateInBetween(TEST_FROM_DATE, TEST_TO_DATE));

    }

    @Test
    public void searchPayments_filterByReferenceStatusAndFromToDates() throws Exception {

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, TEST_STATUS, TEST_FROM_DATE, TEST_TO_DATE,
                aSuccessfulSearchResponse()
                        .withMatchingReference(TEST_REFERENCE)
                        .withMatchingStatus(TEST_STATUS)
                        .withCreatedDateBetween(TEST_FROM_DATE, TEST_TO_DATE)
                        .build()
        );

        ValidatableResponse response = searchPayments(API_KEY,
                ImmutableMap.of("reference", TEST_REFERENCE, "status", TEST_STATUS, "from_date", TEST_FROM_DATE, "to_date", TEST_TO_DATE))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("reference", TEST_REFERENCE));
        assertThat(results, matchesField("status", TEST_STATUS));
        assertThat(results, matchesCreatedDateInBetween(TEST_FROM_DATE, TEST_TO_DATE));

    }

    @Test
    public void searchPayments_errorIfConnectorResponseFails() throws Exception {

        InputStream body = searchPayments(API_KEY,
                ImmutableMap.of("reference", TEST_REFERENCE, "status", TEST_STATUS, "from_date", TEST_FROM_DATE, "to_date", TEST_TO_DATE))
                .statusCode(500)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0498"))
                .assertThat("$.description", is("Downstream system error"));
    }

    @Test
    public void searchPayments_errorIfConnectorResponseIsInvalid() throws Exception {

        connectorMock.whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, TEST_STATUS, TEST_FROM_DATE, TEST_TO_DATE)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("wtf"));

        InputStream body = searchPayments(API_KEY,
                ImmutableMap.of("reference", TEST_REFERENCE, "status", TEST_STATUS, "from_date", TEST_FROM_DATE, "to_date", TEST_TO_DATE))
                .statusCode(500)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0498"))
                .assertThat("$.description", is("Downstream system error"));
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
