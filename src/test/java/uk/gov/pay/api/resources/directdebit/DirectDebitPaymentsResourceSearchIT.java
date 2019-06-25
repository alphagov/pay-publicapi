package uk.gov.pay.api.resources.directdebit;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import junitparams.JUnitParamsRunner;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.api.it.fixtures.PaymentNavigationLinksFixture;
import uk.gov.pay.api.it.fixtures.TestDirectDebitPaymentSearchResult;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.it.fixtures.PaginatedPaymentSearchResultFixture.aPaginatedPaymentSearchResult;
import static uk.gov.pay.api.it.fixtures.TestDirectDebitPaymentSearchResult.TestDirectDebitPaymentSearchResultBuilder.aTestDirectDebitPaymentSearchResult;

@RunWith(JUnitParamsRunner.class)
public class DirectDebitPaymentsResourceSearchIT extends DirectDebitResourceITBase {

    protected static final String SEARCH_PATH = "/v1/directdebit/payments";

    @Test
    public void searchPayments_success() {
        PaymentNavigationLinksFixture links = new PaymentNavigationLinksFixture()
                .withPrevLink("http://server:port/path?query=prev&from_date=2016-01-01T23:59:59Z")
                .withNextLink("http://server:port/path?query=next&from_date=2016-01-01T23:59:59Z")
                .withSelfLink("http://server:port/path?query=self&from_date=2016-01-01T23:59:59Z")
                .withFirstLink("http://server:port/path?query=first&from_date=2016-01-01T23:59:59Z")
                .withLastLink("http://server:port/path?query=last&from_date=2016-01-01T23:59:59Z");

        List<TestDirectDebitPaymentSearchResult> payments = aTestDirectDebitPaymentSearchResult()
                .buildMultiple(3);
        
        String ddConnectorSearchResult = aPaginatedPaymentSearchResult()
                .withCount(3)
                .withPage(2)
                .withTotal(40)
                .withPayments(payments)
                .withLinks(links)
                .build();

        connectorDDMockClient.respondOk_whenSearchPayments(GATEWAY_ACCOUNT_ID, ddConnectorSearchResult);
        ImmutableMap<String, String> queryParams = ImmutableMap.of(
                "reference", "ref",
                "state", "pending",
                "mandate_id", "mandate-id",
                "page", "2",
                "display_size", "3"
        );
        
        searchPayments(queryParams)
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3))
                .body("total", is(40))
                .body("count", is(3))
                .body("page", is(2))
                .body("results[0].payment_id", Matchers.is(payments.get(0).getPayment_id()))
                .body("results[0].amount", Matchers.is(payments.get(0).getAmount().intValue()))
                .body("results[0].payment_provider", Matchers.is(payments.get(0).getPayment_provider()))
                .body("results[0].created_date", Matchers.is(payments.get(0).getCreated_date()))
                .body("results[0].description", Matchers.is(payments.get(0).getDescription()))
                .body("results[0].reference", Matchers.is(payments.get(0).getReference()))
                .body("results[0].state.status", Matchers.is(payments.get(0).getState().getStatus()))
                .body("results[0].state.finished", Matchers.is(payments.get(0).getState().isFinished()))
                .body("results[0].mandate_id", Matchers.is(payments.get(0).getMandate_id()))
                .body("results[0].provider_id", Matchers.is(payments.get(0).getProvider_id()))
                .body("results[0]._links.events.href", Matchers.is(paymentEventsLocationFor(payments.get(0).getPayment_id())))
                .body("results[0]._links.events.method", Matchers.is("GET"))
                .body("results[0]._links.self.href", Matchers.is(paymentLocationFor(payments.get(0).getPayment_id())))
                .body("results[0]._links.self.method", Matchers.is("GET"))
                .body("results[0]._links.mandate.href", Matchers.is(mandateLocationFor(payments.get(0).getMandate_id())))
                .body("results[0]._links.mandate.method", Matchers.is("GET"))
                .body("_links.next_page.href", is(expectedPaginationLink("?query=next&from_date=2016-01-01T23%3A59%3A59Z")))
                .body("_links.prev_page.href", is(expectedPaginationLink("?query=prev&from_date=2016-01-01T23%3A59%3A59Z")))
                .body("_links.first_page.href", is(expectedPaginationLink("?query=first&from_date=2016-01-01T23%3A59%3A59Z")))
                .body("_links.last_page.href", is(expectedPaginationLink("?query=last&from_date=2016-01-01T23%3A59%3A59Z")))
                .body("_links.self.href", is(expectedPaginationLink("?query=self&from_date=2016-01-01T23%3A59%3A59Z")));
    }

    @Test
    public void searchPayments_validationSuccess() {
        String payments = aPaginatedPaymentSearchResult()
                .withCount(10)
                .withPage(2)
                .withTotal(20)
                .withPayments(aTestDirectDebitPaymentSearchResult()
                        .buildMultiple(3))
                .build();

        connectorDDMockClient.respondOk_whenSearchPayments(GATEWAY_ACCOUNT_ID, payments);

        Map<String, String> queryParams = Map.of(
                "reference", "a-ref",
                "state", "pending",
                "mandate_id", "a-mandate-id",
                "from_date", "2016-01-01T23:59:59Z",
                "to_date", "2016-01-01T23:59:59Z",
                "page", "1",
                "display_size", "500");
        
        searchPayments(queryParams)
                .statusCode(200)
                .contentType(JSON)
                .body("results.size()", equalTo(3));
    }

    private String expectedPaginationLink(String queryParams) {
        return "http://publicapi.url" + SEARCH_PATH + queryParams;
    }

    private ValidatableResponse searchPayments(Map<String, String> queryParams) {
        return given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .queryParams(queryParams)
                .get(SEARCH_PATH)
                .then();
    }
}
