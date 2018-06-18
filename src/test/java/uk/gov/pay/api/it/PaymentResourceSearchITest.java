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
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardDetails;
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
    private static final String TEST_EMAIL = "alice.111@mail.fake";
    private static final String TEST_STATE = "created";
    private static final String TEST_CARD_BRAND = "master-card";
    private static final String TEST_CARD_BRAND_LABEL = "Mastercard";
    private static final String TEST_CARD_BRAND_MIXED_CASE = "Master-Card";
    private static final String TEST_FROM_DATE = "2016-01-28T00:00:00Z";
    private static final String TEST_TO_DATE = "2016-01-28T12:00:00Z";
    private static final String SEARCH_PATH = "/v1/payments";
    private static final Address BILLING_ADDRESS = new Address("line1", "line2", "NR2 5 6EG", "city", "UK");
    private static final CardDetails CARD_DETAILS = new CardDetails("1234", "Mr. Payment", "12/19", BILLING_ADDRESS, TEST_CARD_BRAND_LABEL);
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
                        .withMatchingCardBrand(TEST_CARD_BRAND_LABEL)
                        .withMatchingCardDetails(CARD_DETAILS)
                        .withNumberOfResults(1)
                        .getResults())
                .build();

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, null, null, null, null, null, payments);

        String responseBody = searchPayments(API_KEY, ImmutableMap.of("reference", TEST_REFERENCE))
                .statusCode(200)
                .contentType(JSON)
                .body("results[0].created_date", is(DEFAULT_CREATED_DATE))
                .body("results[0].reference", is(TEST_REFERENCE))
                .body("results[0].email", is(TEST_EMAIL))
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
                .body("results[0].settlement_summary.capture_submit_time", is(DEFAULT_CAPTURE_SUBMIT_TIME))
                .body("results[0].settlement_summary.captured_date", is(DEFAULT_CAPTURED_DATE))
                .body("results[0].card_details.card_brand", is(TEST_CARD_BRAND_LABEL))
                .body("results[0].card_details.cardholder_name", is(CARD_DETAILS.getCardHolderName()))
                .body("results[0].card_details.expiry_date", is(CARD_DETAILS.getExpiryDate()))
                .body("results[0].card_details.last_digits_card_number", is(CARD_DETAILS.getLastDigitsCardNumber()))
                .body("results[0].card_details.billing_address.line1", is(CARD_DETAILS.getBillingAddress().getLine1()))
                .body("results[0].card_details.billing_address.line2", is(CARD_DETAILS.getBillingAddress().getLine2()))
                .body("results[0].card_details.billing_address.postcode", is(CARD_DETAILS.getBillingAddress().getPostcode()))
                .body("results[0].card_details.billing_address.country", is(CARD_DETAILS.getBillingAddress().getCountry()))
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

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, null, null, null, null, null,
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

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, null, null, null, null, null,
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

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null, null, TEST_STATE, null, null, null,
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

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null, null, TEST_STATE, null, null, null,
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
    public void searchPayments_filterByEmail() throws Exception {
        String payments = aPaginatedPaymentSearchResult()
                .withCount(10)
                .withPage(2)
                .withTotal(20)
                .withPayments(aSuccessfulSearchPayment()
                        .withMatchingEmail(TEST_EMAIL)
                        .getResults())
                .build();

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null, TEST_EMAIL, null, null, null, null, payments);

        ValidatableResponse response = searchPayments(API_KEY, ImmutableMap.of(
                "email", TEST_EMAIL))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("email", TEST_EMAIL));
    }

    @Test
    public void searchPayments_filterByPartialEmail() throws Exception {
        String payments = aPaginatedPaymentSearchResult()
                .withCount(10)
                .withPage(2)
                .withTotal(20)
                .withPayments(aSuccessfulSearchPayment()
                        .withMatchingEmail(TEST_EMAIL)
                        .getResults())
                .build();

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null, "alice", null, null, null, null, payments);

        ValidatableResponse response = searchPayments(API_KEY, ImmutableMap.of(
                "email", "alice"))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("email", TEST_EMAIL));
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

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null,  null, null, null, TEST_FROM_DATE, TEST_TO_DATE,
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
    public void searchPayments_filterByReferenceEmailStateAndFromToDates() throws Exception {
        String payments = aPaginatedPaymentSearchResult()
                .withCount(10)
                .withPage(2)
                .withTotal(20)
                .withPayments(aSuccessfulSearchPayment()
                        .withMatchingReference(TEST_REFERENCE)
                        .withMatchingInProgressState(TEST_STATE)
                        .withMatchingEmail(TEST_EMAIL)
                        .withCreatedDateBetween(TEST_FROM_DATE, TEST_TO_DATE).getResults())
                .build();

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, TEST_EMAIL, TEST_STATE, null, TEST_FROM_DATE, TEST_TO_DATE,
                payments
        );

        ValidatableResponse response = searchPayments(API_KEY, ImmutableMap.of(
                    "reference", TEST_REFERENCE,
                    "email", TEST_EMAIL,
                    "state", TEST_STATE,
                    "from_date", TEST_FROM_DATE,
                    "to_date", TEST_TO_DATE))
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("reference", TEST_REFERENCE));
        assertThat(results, matchesField("email", TEST_EMAIL));
        assertThat(results, matchesState(TEST_STATE));
        assertThat(results, matchesCreatedDateInBetween(TEST_FROM_DATE, TEST_TO_DATE));

    }

    @Test
    public void searchPayments_getsPaginatedResultsFromConnector() throws Exception {

        PaymentNavigationLinksFixture links = new PaymentNavigationLinksFixture()
                .withPrevLink("http://server:port/path?query=prev&from_date=2016-01-01T23:59:59Z")
                .withNextLink("http://server:port/path?query=next&from_date=2016-01-01T23:59:59Z")
                .withSelfLink("http://server:port/path?query=self&from_date=2016-01-01T23:59:59Z")
                .withFirstLink("http://server:port/path?query=first&from_date=2016-01-01T23:59:59Z")
                .withLastLink("http://server:port/path?query=last&from_date=2016-01-01T23:59:59Z");

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

        connectorMock.respondOk_whenSearchChargesWithPageAndSize(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, TEST_EMAIL, "2", "10",
                payments
        );
        ImmutableMap<String, String> queryParams = ImmutableMap.of(
                "reference", TEST_REFERENCE,
                "state", TEST_STATE,
                "email", TEST_EMAIL,
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
                .body("_links.next_page.href", Matchers.is(expectedChargesLocationFor("?query=next&from_date=2016-01-01T23%3A59%3A59Z")))
                .body("_links.prev_page.href", Matchers.is(expectedChargesLocationFor("?query=prev&from_date=2016-01-01T23%3A59%3A59Z")))
                .body("_links.first_page.href", Matchers.is(expectedChargesLocationFor("?query=first&from_date=2016-01-01T23%3A59%3A59Z")))
                .body("_links.last_page.href", Matchers.is(Matchers.is(expectedChargesLocationFor("?query=last&from_date=2016-01-01T23%3A59%3A59Z"))))
                .body("_links.self.href", Matchers.is(Matchers.is(expectedChargesLocationFor("?query=self&from_date=2016-01-01T23%3A59%3A59Z"))));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("reference", TEST_REFERENCE));
        assertThat(results, matchesField("email", TEST_EMAIL));
        assertThat(results, matchesState(TEST_STATE));
        assertThat(results, matchesCreatedDateInBetween(TEST_FROM_DATE, TEST_TO_DATE));

    }

    private String expectedChargesLocationFor(String queryParams) {
        return "http://publicapi.url" + SEARCH_PATH + queryParams;
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
        connectorMock.whenSearchCharges(GATEWAY_ACCOUNT_ID, TEST_REFERENCE, TEST_EMAIL, TEST_STATE, null, TEST_FROM_DATE, TEST_TO_DATE)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("wtf"));

        InputStream body = searchPayments(API_KEY,
                ImmutableMap.of(
                        "reference", TEST_REFERENCE,
                        "email", TEST_EMAIL,
                        "state", TEST_STATE,
                        "from_date", TEST_FROM_DATE,
                        "to_date", TEST_TO_DATE))
                .statusCode(500)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0498"))
                .assertThat("$.description", is("Downstream system error"));
    }

    @Test
    public void searchPayments_filterByCardBrand() throws Exception {
        ValidatableResponse response = getResultForSearchByCardBrand(TEST_CARD_BRAND);
        response
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("card_brand", TEST_CARD_BRAND_LABEL));
    }

    @Test
    public void searchPayments_filterByCardBrandMixedCase() throws Exception {

        ValidatableResponse response = getResultForSearchByCardBrand(TEST_CARD_BRAND_MIXED_CASE);
        response
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));

        List<Map<String, Object>> results = response.extract().body().jsonPath().getList("results");
        assertThat(results, matchesField("card_brand", TEST_CARD_BRAND_LABEL));
    }

    private ValidatableResponse getResultForSearchByCardBrand(String cardBrand){
        String payments = aPaginatedPaymentSearchResult()
                .withCount(1)
                .withPage(1)
                .withTotal(2)
                .withPayments(aSuccessfulSearchPayment()
                        .withMatchingCardBrand(TEST_CARD_BRAND_LABEL)
                        .getResults())
                .build();

        connectorMock.respondOk_whenSearchCharges(GATEWAY_ACCOUNT_ID, null, null, null, TEST_CARD_BRAND, null, null, payments);

        ValidatableResponse response = searchPayments(API_KEY, ImmutableMap.of(
                "card_brand", cardBrand));

        return response;
    }

    @Test
    public void searchPayments_filterByInvalidCardBrand() throws Exception {
        InputStream body = searchPayments(API_KEY,
                ImmutableMap.of("card_brand", "my_credit_card"))
                .statusCode(404)
                .contentType(JSON).extract()
                .body().asInputStream();

        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.description", is("Page not found"));

    }
    
    @Test
    public void searchPayments_withMandateId_whenCardPayment_shouldReturnABadRequestResponse() throws Exception{
        InputStream body = searchPayments(API_KEY,
                ImmutableMap.of("agreement", "my_agreement"))
                .statusCode(400)
                .contentType(JSON).extract()
                .body().asInputStream();
        JsonAssert.with(body)
                .assertThat("$.*", hasSize(2))
                .assertThat("$.code", is("P0401"))
                .assertThat("$.description", is("Invalid parameters: agreement. See Public API documentation for the correct data formats"));
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
