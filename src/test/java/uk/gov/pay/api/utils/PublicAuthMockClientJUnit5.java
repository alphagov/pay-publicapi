package uk.gov.pay.api.utils;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import uk.gov.pay.api.model.TokenPaymentType;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.service.payments.commons.model.ErrorIdentifier.AUTH_TOKEN_INVALID;

public class PublicAuthMockClientJUnit5 {

    private WireMockExtension publicAuthClassRule;

    public PublicAuthMockClientJUnit5(WireMockExtension publicAuthClassRule) {
        this.publicAuthClassRule = publicAuthClassRule;
    }

    public void respondUnauthorised() {
        publicAuthClassRule.stubFor(get("/v1/auth").withHeader(ACCEPT, matching(APPLICATION_JSON))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("{\"error_identifier\" : \"" + AUTH_TOKEN_INVALID + "\"}")));
    }

    public void mapBearerTokenToAccountId(String bearerToken, String gatewayAccountId) {
        mapBearerTokenToAccountId(bearerToken, gatewayAccountId, TokenPaymentType.CARD);
    }

    public void mapBearerTokenToAccountId(String bearerToken, String gatewayAccountId, TokenPaymentType tokenType) {
        publicAuthClassRule.stubFor(get("/v1/auth")
                .withHeader(ACCEPT, matching(APPLICATION_JSON))
                .withHeader(AUTHORIZATION, matching("Bearer " + bearerToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("{\"account_id\" : \"" + gatewayAccountId + "\", " +
                                "\"token_link\" : \"a-token-link\", " +
                                "\"token_type\" : \"" + tokenType.toString() + "\"}")));
    }

    public void respondWithInvalidTokenType(String bearerToken, String gatewayAccountId) {
        publicAuthClassRule.stubFor(get("/v1/auth")
                .withHeader(ACCEPT, matching(APPLICATION_JSON))
                .withHeader(AUTHORIZATION, matching("Bearer " + bearerToken))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody("{\"account_id\" : \"" + gatewayAccountId + "\", " +
                                "\"token_link\" : \"a-token-link\", " +
                                "\"token_type\" : \"AN_UNKNOWN_TYPE\"}")));
    }

    public void respondWithError() {
        publicAuthClassRule.stubFor(get("").withHeader(ACCEPT, matching(APPLICATION_JSON))
                .willReturn(aResponse().withStatus(500)));
    }
}
