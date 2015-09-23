package uk.gov.pay.api.utils;

import com.google.common.collect.ImmutableMap;
import org.mockserver.client.server.ForwardChainExpectation;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.HttpResponse;

import static java.util.Arrays.asList;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;
import static org.eclipse.jetty.http.HttpStatus.NO_CONTENT_204;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.once;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonString;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonStringBuilder;

public class ConnectorMockClient {
    public static final String CONNECTOR_MOCK_CHARGE_PATH = "/tv1/charges";
    public static final String CONNECTOR_MOCK_CANCEL_PATH_SUFFIX = "/cancel";
    private final MockServerClient mockClient;
    private final String baseUrl;

    public ConnectorMockClient(MockServerClient mockClient, String baseUrl) {
        this.mockClient = mockClient;
        this.baseUrl = baseUrl;
    }

    private String createChargePayload(long amount, String gatewayAccountId, String returnUrl) {
        return jsonStringBuilder()
                .add("amount", amount)
                .add("gateway_account_id", gatewayAccountId)
                .add("return_url", returnUrl)
                .build();
    }

    private String createChargeResponse(long amount, String chargeId, String status, String returnUrl, ImmutableMap<?, ?>... links) {
        return jsonStringBuilder()
                .add("charge_id", chargeId)
                .add("amount", amount)
                .add("status", status)
                .add("return_url", returnUrl)
                .add("links", asList(links))
                .build();
    }

    private ImmutableMap<String, String> validLink(String href, String rel) {
        return ImmutableMap.of(
                "href", href,
                "rel", rel,
                "method", GET);
    }


    private String nextUrl(String chargeId) {
        return "http://Frontend/charge/" + chargeId;
    }

    private String chargeLocation(String chargeId) {
        return baseUrl + CONNECTOR_MOCK_CHARGE_PATH + "/" + chargeId;
    }

    public void respondOk_whenCreateCharge(long amount, String gatewayAccountId, String chargeId, String status, String returnUrl) {
        whenCreateCharge(amount, gatewayAccountId, returnUrl)
                .respond(response()
                        .withStatusCode(CREATED_201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, chargeLocation(chargeId))
                        .withBody(createChargeResponse(amount, chargeId, status, returnUrl,
                                validLink(chargeLocation(chargeId), "self"),
                                validLink(nextUrl(chargeId), "next_url"))));
    }

    public void respondOk_whenCreateChargeWithoutNextUrl(long amount, String gatewayAccountId, String chargeId, String status, String returnUrl) {
        whenCreateCharge(amount, gatewayAccountId, returnUrl)
                .respond(response()
                        .withStatusCode(CREATED_201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, chargeLocation(chargeId))
                        .withBody(createChargeResponse(amount, chargeId, status, returnUrl,
                                validLink(chargeLocation(chargeId), "self"))));
    }

    public void respondUnknownGateway_whenCreateCharge(long amount, String gatewayAccountId, String errorMsg, String returnUrl) {
        whenCreateCharge(amount, gatewayAccountId, returnUrl)
                .respond(withStatusAndErrorMessage(BAD_REQUEST_400, errorMsg));
    }

    public void respondOk_withEmptyBody(long amount, String gatewayAccountId, String chargeId, String returnUrl) {
        whenCreateCharge(amount, gatewayAccountId, returnUrl)
                .respond(response()
                        .withStatusCode(CREATED_201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, chargeLocation(chargeId)));
    }

    public void respondWithChargeFound(long amount, String chargeId, String status, String returnUrl) {
        whenGetCharge(chargeId)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(createChargeResponse(amount, chargeId, status, returnUrl,
                                validLink(chargeLocation(chargeId), "self"),
                                validLink(nextUrl(chargeId), "next_url"))));
    }

    public void respondChargeNotFound(String chargeId, String errorMsg) {
        whenGetCharge(chargeId)
                .respond(withStatusAndErrorMessage(NOT_FOUND_404, errorMsg));
    }

    public void respondOk_whenCancelCharge(String paymentId) {
        whenCancelCharge(paymentId)
                .respond(response()
                        .withStatusCode(NO_CONTENT_204));
    }

    public void respondChargeNotFound_WhenCancelCharge(String paymentId, String errorMsg) {
        whenCancelCharge(paymentId)
                .respond(withStatusAndErrorMessage(NOT_FOUND_404, errorMsg));
    }

    public void respondBadRequest_WhenCancelChargeNotAllowed(String paymentId, String errorMsg) {
        whenCancelCharge(paymentId)
                .respond(withStatusAndErrorMessage(BAD_REQUEST_400, errorMsg));
    }

    private ForwardChainExpectation whenCreateCharge(long amount, String gatewayAccountId, String returnUrl) {
        return mockClient.when(request()
                        .withMethod(POST)
                        .withPath(CONNECTOR_MOCK_CHARGE_PATH)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(createChargePayload(amount, gatewayAccountId, returnUrl))
        );
    }

    private ForwardChainExpectation whenGetCharge(String chargeId) {
        return mockClient.when(request()
                        .withMethod(GET)
                        .withPath(CONNECTOR_MOCK_CHARGE_PATH + "/" + chargeId)
        );
    }

    private ForwardChainExpectation whenCancelCharge(String paymentId) {
        return mockClient.when(request()
                .withMethod(POST)
                .withPath(CONNECTOR_MOCK_CHARGE_PATH + "/" + paymentId + CONNECTOR_MOCK_CANCEL_PATH_SUFFIX));
    }

    private HttpResponse withStatusAndErrorMessage(int statusCode, String errorMsg) {
        return response()
                .withStatusCode(statusCode)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(jsonString("message", errorMsg));
    }

    public void verifyCreateCharge(long amount, String gatewayAccountId, String returnUrl) {
        mockClient.verify(request()
                        .withMethod(POST)
                        .withPath(CONNECTOR_MOCK_CHARGE_PATH)
                        .withBody(createChargePayload(amount, gatewayAccountId, returnUrl)),
                once()
        );
    }

    public void verifyCancelCharge(String paymentId) {
        mockClient.verify(request()
                        .withMethod(POST)
                        .withPath(CONNECTOR_MOCK_CHARGE_PATH + "/" + paymentId + CONNECTOR_MOCK_CANCEL_PATH_SUFFIX),
                once());
    }
}
