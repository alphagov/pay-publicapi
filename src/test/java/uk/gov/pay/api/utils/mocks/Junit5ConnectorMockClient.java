package uk.gov.pay.api.utils.mocks;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import uk.gov.service.payments.commons.model.ErrorIdentifier;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.lang.String.format;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.HttpHeaders.LOCATION;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static uk.gov.pay.api.utils.mocks.MockHelperFunctions.buildChargeResponse;
import static uk.gov.pay.api.utils.mocks.MockHelperFunctions.buildErrorResponse;
import static uk.gov.pay.api.utils.mocks.MockHelperFunctions.chargeLocation;
import static uk.gov.pay.api.utils.mocks.MockHelperFunctions.fromCreateChargeRequestParams;

public class Junit5ConnectorMockClient {
    
    private final WireMockExtension wireMockExtension;

    public Junit5ConnectorMockClient(WireMockExtension wireMockExtension) {
        this.wireMockExtension = wireMockExtension;
    }

    public void respondCreated_whenCreateCharge(String gatewayAccountId, CreateChargeRequestParams requestParams) {
        var responseFromConnector = fromCreateChargeRequestParams(gatewayAccountId, requestParams);
        var responseDefinitionBuilder = aResponse()
                .withStatus(CREATED_201)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withHeader(LOCATION, chargeLocation(gatewayAccountId, "chargeId"))
                .withBody(buildChargeResponse(responseFromConnector));

        wireMockExtension.stubFor(post(urlPathEqualTo(format("/v1/api/accounts/%s/charges", gatewayAccountId)))
                .withHeader(CONTENT_TYPE, matching(APPLICATION_JSON)).willReturn(responseDefinitionBuilder));
    }

    public void respondWithErrorIdentifier_whenCreateCharge(String accountId, int statusCode, ErrorIdentifier errorIdentifier) {
        ResponseDefinitionBuilder errorResponse = buildErrorResponse(statusCode, "", errorIdentifier, null);
        wireMockExtension.stubFor(post(urlPathEqualTo(format("/v1/api/accounts/%s/charges", accountId)))
                .withHeader(CONTENT_TYPE, matching(APPLICATION_JSON)).willReturn(errorResponse));
    }
}
