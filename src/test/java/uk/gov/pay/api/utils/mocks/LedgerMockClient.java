package uk.gov.pay.api.utils.mocks;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import uk.gov.pay.api.model.links.Link;
import uk.gov.pay.api.utils.JsonStringBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;
import static org.eclipse.jetty.http.HttpStatus.OK_200;

public class LedgerMockClient {

    private final WireMockClassRule ledgerMock;

    public LedgerMockClient(WireMockClassRule ledgerMock) {
        this.ledgerMock = ledgerMock;
    }

    public void respondOk_whenSearchCharges(String expectedResponse) {
        whenSearchTransactions(aResponse()
                .withStatus(OK_200)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(expectedResponse));
    }

    public void whenSearchTransactions(ResponseDefinitionBuilder response) {
        ledgerMock.stubFor(get(urlPathEqualTo(format("/v1/transaction")))
                .withHeader(ACCEPT, matching(APPLICATION_JSON)).willReturn(response));
    }

    public void respondTransactionNotFound(String paymentId, String errorMessage) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("message", List.of(errorMessage));

        ResponseDefinitionBuilder response = aResponse()
                .withStatus(NOT_FOUND_404)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(new GsonBuilder().create().toJson(payload));

        ledgerMock.stubFor(get(urlPathEqualTo(format("/v1/transaction/%s", paymentId)))
                .willReturn(response));
    }

    public void respondWithGetAllRefunds(String transactionId,
                                         RefundTransactionFromLedgerFixture... refunds) {

        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder()
                .add("parent_transaction_id", transactionId)
                .add("transactions", refunds);

        ledgerMock.stubFor(get(urlPathEqualTo(format("/v1/transaction/%s/transaction", transactionId)))
                .willReturn(aResponse()
                        .withStatus(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(jsonStringBuilder.build())));
    }

    public void respondWithSearchRefunds(RefundTransactionFromLedgerFixture... refunds) {

        Map<String, Link> links = (ImmutableMap.of("first_page", new Link("http://server:port/first-link?page=1"),
                "prev_page", new Link("http://server:port/prev-link?page=2"),
                "self", new Link("http://server:port/self-link?page=3"),
                "last_page", new Link("http://server:port/last-link?page=5"),
                "next_page", new Link("http://server:port/next-link?page=4")));

        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder()
                .add("total", 1)
                .add("count", 1)
                .add("page", 1)
                .add("results", Arrays.asList(refunds))
                .add("_links", links);

        ledgerMock.stubFor(get(urlPathEqualTo("/v1/transaction"))
                .willReturn(aResponse()
                        .withStatus(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(jsonStringBuilder.build())));
    }
}
