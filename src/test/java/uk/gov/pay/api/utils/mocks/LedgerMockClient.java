package uk.gov.pay.api.utils.mocks;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.lang.String.format;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
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
}
