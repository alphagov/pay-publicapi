package uk.gov.pay.api.utils;

import org.mockserver.client.server.ForwardChainExpectation;
import org.mockserver.client.server.MockServerClient;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.once;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonString;
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
        return jsonString("amount", amount, "gateway_account", gatewayAccountId);
    }

    private String createChargeResponse(long amount, String chargeId) {
        return jsonStringBuilder()
                .add("charge_id", chargeId)
                .add("amount", amount)
                .add("status", "CREATED")
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
                        .withStatusCode(CREATED_201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, chargeLocation(chargeId))
                        .withBody(createChargeResponse(amount, chargeId)));
    }

    public void respondUnknownGateway_whenCreateCharge(long amount, String gatewayAccountId, String errorMsg) {
        whenCreateCharge(amount, gatewayAccountId)
                .respond(response()
                        .withStatusCode(BAD_REQUEST_400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(jsonString("message", errorMsg)));
    }

    public void respondOk_withEmptyBody(long amount, String gatewayAccountId, String chargeId) {
        whenCreateCharge(amount, gatewayAccountId)
                .respond(response()
                        .withStatusCode(CREATED_201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, chargeLocation(chargeId)));
    }

    public void respondWithChargeFound(long amount, String chargeId) {
        whenGetCharge(chargeId)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(createChargeResponse(amount, chargeId)));
    }

    public void respondChargeNotFound(String chargeId, String errorMsg) {
        whenGetCharge(chargeId)
                .respond(response()
                        .withStatusCode(NOT_FOUND_404)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(jsonString("message", errorMsg)));
    }

    private ForwardChainExpectation whenCreateCharge(long amount, String gatewayAccountId) {
        return mockClient.when(request()
                        .withMethod(POST)
                        .withPath(CONNECTOR_MOCK_CHARGE_PATH)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(createChargePayload(amount, gatewayAccountId))
        );
    }

    private ForwardChainExpectation whenGetCharge(String chargeId) {
        return mockClient.when(request()
                        .withMethod(GET)
                        .withPath(CONNECTOR_MOCK_CHARGE_PATH + "/" + chargeId)
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
