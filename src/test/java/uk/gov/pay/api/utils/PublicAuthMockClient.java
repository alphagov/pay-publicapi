package uk.gov.pay.api.utils;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import uk.gov.pay.api.model.TokenPaymentType;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class PublicAuthMockClient {

    private WireMockClassRule publicAuthClassRule;

    public PublicAuthMockClient(WireMockClassRule publicAuthClassRule) {
        this.publicAuthClassRule = publicAuthClassRule;
    }

    public void respondUnauthorised() {
        publicAuthClassRule.stubFor(get("/v1/auth").withHeader(ACCEPT, matching(APPLICATION_JSON))
                .willReturn(aResponse().withStatus(401)));
    }

    public void mapBearerTokenToAccountId(String bearerToken, String gatewayAccountId) {
        mapBearerTokenToAccountId(bearerToken, gatewayAccountId, TokenPaymentType.CARD);
    }

    public void mapBearerTokenToAccountId(String bearerToken, String gatewayAccountId, TokenPaymentType tokenType) {
        publicAuthClassRule.stubFor(get("/v1/auth")
                .withHeader(ACCEPT, matching(APPLICATION_JSON))
                .withHeader(AUTHORIZATION, matching("Bearer " + bearerToken))
                .willReturn(aResponse().withStatus(200).withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("{\"account_id\" : \"" + gatewayAccountId + "\", \"token_type\" : \"" + tokenType.toString() + "\"}")));
    }

    public void respondWithError() {
        publicAuthClassRule.stubFor(get("").withHeader(ACCEPT, matching(APPLICATION_JSON))
                .willReturn(aResponse().withStatus(500)));
    }
}
