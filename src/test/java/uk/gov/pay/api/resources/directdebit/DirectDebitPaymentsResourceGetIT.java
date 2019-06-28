package uk.gov.pay.api.resources.directdebit;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.ValidatableResponse;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.api.it.fixtures.PaymentNavigationLinksFixture;
import uk.gov.pay.api.it.fixtures.TestDirectDebitPaymentSearchResult;
import uk.gov.pay.api.model.DirectDebitPaymentState;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.it.fixtures.PaginatedPaymentSearchResultFixture.aPaginatedPaymentSearchResult;
import static uk.gov.pay.api.it.fixtures.TestDirectDebitPaymentSearchResult.TestDirectDebitPaymentSearchResultBuilder.aTestDirectDebitPaymentSearchResult;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;
import static uk.gov.pay.api.resources.directdebit.DirectDebitPaymentsResourceGetIT.SearchDirectDebitPaymentsValidationParameters.CreatePaymentRequestValidationParametersBuilder.someParameters;

@RunWith(JUnitParamsRunner.class)
public class DirectDebitPaymentsResourceGetIT extends DirectDebitResourceITBase {

    private static final String GET_PATH = "/v1/directdebit/payments";

    @Test
    public void getPayment_ReturnsDirectDebitPayment() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, DIRECT_DEBIT);

        connectorDDMockClient.respondWithChargeFound("mandate2000", 1000, GATEWAY_ACCOUNT_ID, "ch_ab2341da231434l", new DirectDebitPaymentState("created", false, "example details"), "http://example.com",
                "a description", "a reference", "gocardless", "2018-06-11T19:40:56Z", "token_1234567asdf");

        given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .get("/v1/directdebit/payments/ch_ab2341da231434l")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("payment_id", is("ch_ab2341da231434l"))
                .body("reference", is("a reference"))
                .body("description", is("a description"))
                .body("amount", is(1000))
                .body("state.status", is("created"))
                .body("state.finished", is(false))
                .body("state.details", is("example details"))
                .body("payment_provider", is("gocardless"))
                .body("created_date", is("2018-06-11T19:40:56Z"))
                .body("_links.self.href", is(paymentLocationFor("ch_ab2341da231434l")))
                .body("_links.self.method", is("GET"))
                .body("_links.events.href", is(paymentEventsLocationFor("ch_ab2341da231434l")))
                .body("_links.events.method", is("GET"))
                .body("_links.mandate.href", is(mandateLocationFor("mandate2000")))
                .body("_links.mandate.method", is("GET"));
    }
    
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

    @Test
    @Parameters(method = "parametersForValidation")
    public void validationFailures(SearchDirectDebitPaymentsValidationParameters parameters) {
        given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .get(GET_PATH + parameters.queryString)
                .then()
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .body("size()", is(3))
                .body("field", is(parameters.expectedErrorField))
                .body("code", is(parameters.expectedErrorCode))
                .body("description", is(parameters.expectedErrorMessage));
    }
    
    private SearchDirectDebitPaymentsValidationParameters[] parametersForValidation() {
        return new SearchDirectDebitPaymentsValidationParameters[] {
                someParameters()
                        .withQueryString("?reference=" + RandomStringUtils.random(256))
                        .withErrorField("reference")
                        .withErrorMessage("Invalid attribute value: reference. Must be less than or equal to 255 characters length")
                        .build(),
                someParameters()
                        .withQueryString("?state=fake_state")
                        .withErrorField("state")
                        .withErrorMessage("Invalid attribute value: state. Must be one of pending, success, failed, cancelled or expired")
                        .build(),
                someParameters()
                        .withQueryString("?from_date=not_a_date")
                        .withErrorField("from_date")
                        .withErrorMessage("Invalid attribute value: from_date. Must be a valid date")
                        .build(),
                someParameters()
                        .withQueryString("?to_date=not_a_date")
                        .withErrorField("to_date")
                        .withErrorMessage("Invalid attribute value: to_date. Must be a valid date")
                        .build(),
                someParameters()
                        .withQueryString("?page=0")
                        .withErrorField("page")
                        .withErrorMessage("Invalid attribute value: page. Must be greater than or equal to 1")
                        .build(),
                someParameters()
                        .withQueryString("?display_size=0")
                        .withErrorField("display_size")
                        .withErrorMessage("Invalid attribute value: display_size. Must be greater than or equal to 1")
                        .build(),
                someParameters()
                        .withQueryString("?display_size=501")
                        .withErrorField("display_size")
                        .withErrorMessage("Invalid attribute value: display_size. Must be less than or equal to 500")
                        .build(),
        };
    }

    private String expectedPaginationLink(String queryParams) {
        return "http://publicapi.url" + GET_PATH + queryParams;
    }

    private ValidatableResponse searchPayments(Map<String, String> queryParams) {
        return given().port(app.getLocalPort())
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + API_KEY)
                .queryParams(queryParams)
                .get(GET_PATH)
                .then();
    }
    
    static class SearchDirectDebitPaymentsValidationParameters {
        String queryString;
        String expectedErrorCode;
        String expectedErrorField;
        String expectedErrorMessage;

        private SearchDirectDebitPaymentsValidationParameters(CreatePaymentRequestValidationParametersBuilder builder) {
            this.queryString = builder.queryString;
            this.expectedErrorCode = builder.expectedErrorCode;
            this.expectedErrorField = builder.expectedErrorField;
            this.expectedErrorMessage = builder.expectedErrorMessage;
        }

        @Override
        public String toString() {
            return "SearchDirectDebitPaymentsValidationParameters{" +
                    "queryString='" + queryString + '\'' +
                    ", expectedErrorCode='" + expectedErrorCode + '\'' +
                    ", expectedErrorField='" + expectedErrorField + '\'' +
                    ", expectedErrorMessage='" + expectedErrorMessage + '\'' +
                    '}';
        }

        static class CreatePaymentRequestValidationParametersBuilder {
            public String queryString;
            public String expectedErrorCode = "P0102";
            public String expectedErrorField;
            public String expectedErrorMessage;

            static CreatePaymentRequestValidationParametersBuilder someParameters() {
                return new CreatePaymentRequestValidationParametersBuilder();
            }

            CreatePaymentRequestValidationParametersBuilder withQueryString(String queryString) {
                this.queryString = queryString;
                return this;
            }

            CreatePaymentRequestValidationParametersBuilder withErrorCode(String errorCode) {
                this.expectedErrorCode= errorCode;
                return this;
            }

            CreatePaymentRequestValidationParametersBuilder withErrorField(String errorField) {
                this.expectedErrorField= errorField;
                return this;
            }

            CreatePaymentRequestValidationParametersBuilder withErrorMessage(String errorMessage) {
                this.expectedErrorMessage= errorMessage;
                return this;
            }

            SearchDirectDebitPaymentsValidationParameters build() {
                return new SearchDirectDebitPaymentsValidationParameters(this);
            }
        }
    }
}
