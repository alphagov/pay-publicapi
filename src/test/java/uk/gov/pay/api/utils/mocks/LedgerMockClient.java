package uk.gov.pay.api.utils.mocks;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.google.gson.GsonBuilder;
import org.bouncycastle.cert.ocsp.RespData;

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

public class LedgerMockClient  {

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
}
