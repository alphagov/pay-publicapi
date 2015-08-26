package uk.gov.pay.api.utils;

import org.mockserver.client.server.ForwardChainExpectation;
import org.mockserver.client.server.MockServerClient;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.once;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonStringBuilder;

public class ConnectorMockClient {
    public static final String CONNECTOR_MOCK_CHARGE_PATH = "/tv1/charges";
    private final MockServerClient mockClient;
    private final String baseUrl;

    public ConnectorMockClient(MockServerClient mockClient, String baseUrl) {
        this.mockClient = mockClient;
        this.baseUrl = baseUrl;
    }

    private String createChargePayload(long amount, String gatewayAccountId) {
        return jsonStringBuilder()
                .add("amount", amount)
                .add("gateway_account", gatewayAccountId)
                .build();
    }

    private String createChargeResponse(String chargeId) {
        return jsonStringBuilder()
                .add("charge_id", chargeId)
                .addToMap("links", "href", chargeLocation(chargeId))
                .addToMap("links", "rel", "self")
                .addToMap("links", "method", GET)
                .build();
    }

    private String chargeLocation(String chargeId) {
        return baseUrl + CONNECTOR_MOCK_CHARGE_PATH + "/" + chargeId;
    }

    public void respondOk_whenCreateCharge(long amount, String gatewayAccountId, String chargeId) {
        whenCreateCharge(amount, gatewayAccountId)
                .respond(response()
                        .withStatusCode(201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, chargeLocation(chargeId))
                        .withBody(createChargeResponse(chargeId)));
    }

    public void respondUnknownGateway_whenCreateCharge(long amount, String gatewayAccountId, String errorMsg) {
        whenCreateCharge(amount, gatewayAccountId)
                .respond(response()
                        .withStatusCode(400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(jsonStringBuilder().add("message", errorMsg).build()));
    }


    public void respondOk_withEmptyBody(long amount, String gatewayAccountId, String chargeId) {
        whenCreateCharge(amount, gatewayAccountId)
                .respond(response()
                        .withStatusCode(201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, chargeLocation(chargeId)));
    }
    private ForwardChainExpectation whenCreateCharge(long amount, String gatewayAccountId) {
        return mockClient.when(request()
                        .withMethod(POST)
                        .withPath(CONNECTOR_MOCK_CHARGE_PATH)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(createChargePayload(amount, gatewayAccountId))
        );
    }

    public void verifyCreateCharge(long amount, String gatewayAccountId) {
        mockClient.verify(request()
                        .withMethod(POST)
                        .withPath(CONNECTOR_MOCK_CHARGE_PATH)
                        .withBody(createChargePayload(amount, gatewayAccountId)),
                once()
        );
    }
}
