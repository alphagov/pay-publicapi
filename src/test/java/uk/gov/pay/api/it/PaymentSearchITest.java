package uk.gov.pay.api.it;


import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.ValidatableResponse;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import uk.gov.pay.api.utils.DateTimeUtils;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.api.it.fixtures.PaymentSearchResultBuilder.aSuccessfulSearchResponse;

public class PaymentSearchITest extends PaymentResourceITestBase {

    private static final String TEST_REFERENCE = "test_reference";
    private static final String TEST_STATUS = "succeeded";
    private static final String TEST_FROM_DATE = "2016-01-28T00:00:00Z";
    private static final String TEST_TO_DATE = "2016-01-28T12:00:00Z";
    private static final String SEARCH_PATH = "/v1/payments";

    @Test
    public void searchPayments_filterByFullReference() throws Exception {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, null, null, null,
                aSuccessfulSearchResponse()
                        .withMatchingReference(TEST_REFERENCE)
                        .build()
        );

        ValidatableResponse response = searchPayments(BEARER_TOKEN, ImmutableMap.of("reference", TEST_REFERENCE))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("reference", TEST_REFERENCE));
    }


    @Test
    public void searchPayments_filterByStatus() throws Exception {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null, TEST_STATUS, null, null,
                aSuccessfulSearchResponse()
                        .withMatchingStatus(TEST_STATUS)
                        .build()
        );

        ValidatableResponse response = searchPayments(BEARER_TOKEN, ImmutableMap.of("status", TEST_STATUS))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("status", TEST_STATUS));
    }

    @Test
    public void searchPayments_filterByStatusLowercase() throws Exception {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null, TEST_STATUS, null, null,
                aSuccessfulSearchResponse()
                        .withMatchingStatus(TEST_STATUS)
                        .build()
        );

        ValidatableResponse response = searchPayments(BEARER_TOKEN, ImmutableMap.of("status", TEST_STATUS.toLowerCase()))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("status", TEST_STATUS));
    }

    @Test
    public void searchPayments_filterFromAndToDates() throws Exception {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null, null, TEST_FROM_DATE, TEST_TO_DATE,
                aSuccessfulSearchResponse()
                        .withCreatedDateBetween(TEST_FROM_DATE, TEST_TO_DATE)
                        .build()
        );

        ValidatableResponse response = searchPayments(BEARER_TOKEN, ImmutableMap.of("from_date", TEST_FROM_DATE, "to_date", TEST_TO_DATE))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");

        assertThat(results, matchesCreatedDateInBetween(TEST_FROM_DATE, TEST_TO_DATE));

    }

    @Test
    public void searchPayments_filterByReferenceStatusAndFromToDates() throws Exception {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, TEST_STATUS, TEST_FROM_DATE, TEST_TO_DATE,
                aSuccessfulSearchResponse()
                        .withMatchingReference(TEST_REFERENCE)
                        .withMatchingStatus(TEST_STATUS)
                        .withCreatedDateBetween(TEST_FROM_DATE, TEST_TO_DATE)
                        .build()
        );

        ValidatableResponse response = searchPayments(BEARER_TOKEN,
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
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);

        searchPayments(BEARER_TOKEN,
                ImmutableMap.of("reference", TEST_REFERENCE, "status", TEST_STATUS, "from_date", TEST_FROM_DATE, "to_date", TEST_TO_DATE))
                .statusCode(500)
                .contentType(JSON)
                .body("message", is("Search payments failed"));
    }

    @Test
    public void searchPayments_errorIfToDatesIsNotInLocalDateTimeFormat() throws Exception {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        searchPayments(BEARER_TOKEN,
                ImmutableMap.of("reference", TEST_REFERENCE, "status", TEST_STATUS, "from_date", TEST_FROM_DATE, "to_date", "2016-01-01 00:00"))
                .statusCode(422)
                .contentType(JSON)
                .body("message", is("fields [to_date] are not in correct format. see public api documentation for the correct data formats"));
    }

    @Test
    public void searchPayments_errorIfToDatesNotInLocalDateTimeFormaAndInvalidStatus() throws Exception {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        searchPayments(BEARER_TOKEN,
                ImmutableMap.of("reference", TEST_REFERENCE, "status", "invalid status", "from_date", TEST_FROM_DATE, "to_date", "2016-01-01 00:00"))
                .statusCode(422)
                .contentType(JSON)
                .body("message", is("fields [to_date, status] are not in correct format. see public api documentation for the correct data formats"));
    }

    @Test
    public void searchPayments_errorIfFromToDatesAreNotInLocalDateTimeFormat() throws Exception {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        searchPayments(BEARER_TOKEN,
                ImmutableMap.of("reference", TEST_REFERENCE, "status", TEST_STATUS, "from_date", "12345", "to_date", "2016-01-01 00:00"))
                .statusCode(422)
                .contentType(JSON)
                .body("message", is("fields [from_date, to_date] are not in correct format. see public api documentation for the correct data formats"));
    }

    @Test
    public void searchPayments_errorIfStatusNotMatchingWithExpectedExternalStatuses() throws Exception {
        publicAuthMock.mapBearerTokenToAccountId(BEARER_TOKEN, GATEWAY_ACCOUNT_ID);
        searchPayments(BEARER_TOKEN,
                ImmutableMap.of("reference", TEST_REFERENCE, "status", "invalid status", "from_date", TEST_FROM_DATE, "to_date", TEST_TO_DATE))
                .statusCode(422)
                .contentType(JSON)
                .body("message", is("fields [status] are not in correct format. see public api documentation for the correct data formats"));
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
