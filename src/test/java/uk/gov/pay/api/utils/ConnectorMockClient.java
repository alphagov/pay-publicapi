package uk.gov.pay.api.utils;

import com.google.common.collect.ImmutableMap;
import org.mockserver.client.server.ForwardChainExpectation;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.Parameter;
import uk.gov.pay.api.it.fixtures.PaymentRefundJsonFixture;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.model.links.Link;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.HttpHeaders.*;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.eclipse.jetty.http.HttpStatus.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.once;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonString;

public class ConnectorMockClient {

    private static final String CONNECTOR_MOCK_ACCOUNTS_PATH = "/v1/api/accounts/%s";
    private static final String CONNECTOR_MOCK_CHARGES_PATH = CONNECTOR_MOCK_ACCOUNTS_PATH + "/charges";
    private static final String CONNECTOR_MOCK_CHARGE_PATH = CONNECTOR_MOCK_CHARGES_PATH + "/%s";
    private static final String CONNECTOR_MOCK_CHARGE_EVENTS_PATH = CONNECTOR_MOCK_CHARGE_PATH + "/events";
    private static final String CONNECTOR_MOCK_CHARGE_REFUNDS_PATH = CONNECTOR_MOCK_CHARGE_PATH + "/refunds";
    private static final String CONNECTOR_MOCK_CHARGE_REFUND_BY_ID_PATH = CONNECTOR_MOCK_CHARGE_REFUNDS_PATH + "/%s";
    private static final String REFERENCE_KEY = "reference";
    private static final String EMAIL_KEY = "email";
    private static final String STATE_KEY = "state";
    private static final String FROM_DATE_KEY = "from_date";
    private static final String TO_DATE_KEY = "to_date";

    private final MockServerClient mockClient;
    private final String baseUrl;

    public ConnectorMockClient(int port, String baseUrl) {
        this.mockClient = new MockServerClient("localhost", port);
        this.baseUrl = baseUrl;
    }

    private String createChargePayload(long amount, String returnUrl, String description, String reference) {
        return new JsonStringBuilder()
                .add("amount", amount)
                .add("reference", reference)
                .add("description", description)
                .add("return_url", returnUrl)
                .build();
    }

    private String buildChargeResponse(long amount, String chargeId, PaymentState state, String returnUrl, String description,
                                       String reference, String email, String paymentProvider, String gatewayTransactionId, String createdDate, RefundSummary refundSummary, ImmutableMap<?, ?>... links) {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder()
                .add("charge_id", chargeId)
                .add("amount", amount)
                .add("reference", reference)
                .add("email", email)
                .add("description", description)
                .add("state", state)
                .add("return_url", returnUrl)
                .add("payment_provider", paymentProvider)
                .add("created_date", createdDate)
                .add("links", asList(links))
                .add("refund_summary", refundSummary);

        if (gatewayTransactionId != null) {
            jsonStringBuilder.add("gateway_transaction_id", gatewayTransactionId);
        }
        return jsonStringBuilder.build();
    }

    private String buildGetRefundResponse(String refundId, int amount, String status, String createdDate) {
        List<Map<String, Link>> links = new ArrayList<>();
        links.add(ImmutableMap.of("self", new Link("http://server:port/self-link")));
        links.add(ImmutableMap.of("payment", new Link("http://server:port/payment-link")));

        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder()
                .add("refund_id", refundId)
                .add("amount", amount)
                .add("status", status)
                .add("created_date", createdDate)
                .add("_links", links);

        return jsonStringBuilder.build();
    }

    private String buildChargeEventsResponse(String chargeId, List<Map<String, String>> events, ImmutableMap<?, ?>... links) {
        return new JsonStringBuilder()
                .add("charge_id", chargeId)
                .add("events", events)
                .add("links", asList(links))
                .build();
    }

    private ImmutableMap<String, String> validGetLink(String href, String rel) {
        return ImmutableMap.of(
                "href", href,
                "rel", rel,
                "method", GET);
    }

    private ImmutableMap<String, Object> validPostLink(String href, String rel, String type, Map<String, String> params) {
        return ImmutableMap.of(
                "href", href,
                "rel", rel,
                "type", type,
                "params", params,
                "method", POST);
    }

    private String nextUrlPost() {
        return "http://Frontend/charge/";
    }

    private String nextUrl(String tokenId) {
        return nextUrlPost() + tokenId;
    }

    private String chargeLocation(String accountId, String chargeId) {
        return baseUrl + format(CONNECTOR_MOCK_CHARGE_PATH, accountId, chargeId);
    }

    private String chargeEventsLocation(String accountId, String chargeId) {
        return baseUrl + format(CONNECTOR_MOCK_CHARGE_EVENTS_PATH, accountId, chargeId);
    }

    public void respondOk_whenCreateCharge(int amount, String gatewayAccountId, String chargeId, String chargeTokenId, PaymentState state, String returnUrl,
                                           String description, String reference, String email, String paymentProvider, String createdDate, RefundSummary refundSummary) {

        whenCreateCharge(amount, gatewayAccountId, returnUrl, description, reference)
                .respond(response()
                        .withStatusCode(CREATED_201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, chargeLocation(gatewayAccountId, chargeId))
                        .withBody(buildChargeResponse(
                                amount,
                                chargeId,
                                state,
                                returnUrl,
                                description,
                                reference,
                                email,
                                paymentProvider,
                                null,
                                createdDate,
                                refundSummary,
                                validGetLink(chargeLocation(gatewayAccountId, chargeId), "self"),
                                validGetLink(nextUrl(chargeTokenId), "next_url"), validPostLink(nextUrlPost(), "next_url_post", "application/x-www-form-urlencoded",
                                        new HashMap<String, String>() {{
                                            put("chargeTokenId", chargeTokenId);
                                        }}))));
    }


    public void respondAccepted_whenCreateARefund(int amount, String gatewayAccountId, String chargeId, String refundId, String status, String createdDate) {
        whenCreateRefund(amount, gatewayAccountId, chargeId)
                .respond(response()
                        .withStatusCode(ACCEPTED_202)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(buildGetRefundResponse(refundId, amount, status, createdDate))
                );
    }

    public void respondOk_whenSearchCharges(String accountId, String reference, String email, String state, String fromDate, String toDate, String expectedResponse) {
        whenSearchCharges(accountId, reference, email, state, fromDate, toDate)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(expectedResponse)
                );

    }

    public void respondOk_whenSearchChargesWithPageAndSize(String accountId, String reference, String email, String page, String displaySize, String expectedResponse) {
        whenSearchCharges(accountId, reference, email, null, null, null, page, displaySize)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(expectedResponse)
                );

    }

    public void respondNotFound_whenCreateCharge(long amount, String gatewayAccountId, String returnUrl, String description, String reference) {
        whenCreateCharge(amount, gatewayAccountId, returnUrl, description, reference)
                .respond(response().withStatusCode(NOT_FOUND_404));
    }

    public void respondBadRequest_whenCreateCharge(long amount, String gatewayAccountId, String errorMsg, String returnUrl, String description, String reference) {
        whenCreateCharge(amount, gatewayAccountId, returnUrl, description, reference)
                .respond(withStatusAndErrorMessage(BAD_REQUEST_400, errorMsg));
    }

    public void respondWithChargeFound(long amount, String gatewayAccountId, String chargeId, PaymentState state, String returnUrl,
                                       String description, String reference, String email, String paymentProvider, String createdDate, String chargeTokenId, RefundSummary refundSummary) {

        String chargeResponseBody = buildChargeResponse(amount, chargeId, state, returnUrl,
                description, reference, email, paymentProvider, gatewayAccountId, createdDate, refundSummary,
                validGetLink(chargeLocation(gatewayAccountId, chargeId), "self"),
                validGetLink(chargeLocation(gatewayAccountId, chargeId) + "/refunds", "refunds"),
                validGetLink(nextUrl(chargeId), "next_url"), validPostLink(nextUrlPost(), "next_url_post", "application/x-www-form-urlencoded",
                        new HashMap<String, String>() {{
                            put("chargeTokenId", chargeTokenId);
                        }}));

        whenGetCharge(gatewayAccountId, chargeId)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(chargeResponseBody));
    }

    public void respondWithGetRefundById(String gatewayAccountId, String chargeId, String refundId, int amount, String refundStatus, String createdDate) {
        String refundResponse = buildGetRefundResponse(refundId, amount, refundStatus, createdDate);
        whenGetRefundById(gatewayAccountId, chargeId, refundId)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(refundResponse));
    }

    public void respondWithGetAllRefunds(String gatewayAccountId, String chargeId, PaymentRefundJsonFixture... refunds) {

        Map<String, List<PaymentRefundJsonFixture>> refundList = new HashMap<>();
        refundList.put("refunds", Arrays.asList(refunds));

        List<Map<String, Link>> links = new ArrayList<>();
        links.add(ImmutableMap.of("self", new Link("http://server:port/self-link")));
        links.add(ImmutableMap.of("payment", new Link("http://server:port/payment-link")));

        JsonStringBuilder embedded = new JsonStringBuilder().noPrettyPrint();
        embedded.add("refunds", refundList);

        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder()
                .add("payment_id", chargeId)
                .add("_links", links)
                .add("_embedded", refundList);

        whenGetAllRefunds(gatewayAccountId, chargeId)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(jsonStringBuilder.build()));
    }

    public void respondRefundNotFound(String gatewayAccountId, String chargeId, String refundId) {
        whenGetRefundById(gatewayAccountId, chargeId, refundId)
                .respond(withStatusAndErrorMessage(BAD_REQUEST_400, String.format("Refund with id [%s] not found.", refundId)));

    }

    public void respondRefundWithError(String gatewayAccountId, String chargeId, String refundId) {
        whenGetRefundById(gatewayAccountId, chargeId, refundId)
                .respond(withStatusAndErrorMessage(INTERNAL_SERVER_ERROR_500, String.format("server error", refundId)));

    }

    public void respondWithChargeEventsFound(String gatewayAccountId, String chargeId, List<Map<String, String>> events) {
        whenGetChargeEvents(gatewayAccountId, chargeId)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(buildChargeEventsResponse(chargeId, events, validGetLink(chargeEventsLocation(gatewayAccountId, chargeId), "self"))));
    }


    public void respondChargeNotFound(String gatewayAccountId, String chargeId, String errorMsg) {
        respondWhenGetCharge(gatewayAccountId, chargeId, errorMsg, NOT_FOUND_404);
    }

    public void respondWhenGetCharge(String gatewayAccountId, String chargeId, String errorMsg, int status) {
        whenGetCharge(gatewayAccountId, chargeId)
                .respond(withStatusAndErrorMessage(status, errorMsg));
    }

    public void respondChargeEventsNotFound(String gatewayAccountId, String chargeId, String errorMsg) {
        respondWhenGetChargeEvents(gatewayAccountId, chargeId, errorMsg, NOT_FOUND_404);
    }

    public void respondWhenGetChargeEvents(String gatewayAccountId, String chargeId, String errorMsg, int status) {
        whenGetChargeEvents(gatewayAccountId, chargeId)
                .respond(withStatusAndErrorMessage(status, errorMsg));
    }

    public void respondOk_whenCancelCharge(String paymentId, String accountId) {
        whenCancelCharge(paymentId, accountId)
                .respond(response()
                        .withStatusCode(NO_CONTENT_204));
    }

    public void respondChargeNotFound_WhenCancelCharge(String paymentId, String accountId, String errorMsg) {
        respond_WhenCancelCharge(paymentId, accountId, errorMsg, NOT_FOUND_404);
    }

    public void respondBadRequest_WhenCancelCharge(String paymentId, String accountId, String errorMessage) {
        respond_WhenCancelCharge(paymentId, accountId, errorMessage, BAD_REQUEST_400);
    }

    public void respond_WhenCancelCharge(String paymentId, String accountId, String errorMessage, int status) {
        whenCancelCharge(paymentId, accountId)
                .respond(withStatusAndErrorMessage(status, errorMessage));
    }

    public ForwardChainExpectation whenCreateCharge(long amount, String gatewayAccountId, String returnUrl, String description, String reference) {
        return mockClient.when(request()
                .withMethod(POST)
                .withPath(format(CONNECTOR_MOCK_CHARGES_PATH, gatewayAccountId))
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(createChargePayload(amount, returnUrl, description, reference))
        );
    }

    private ForwardChainExpectation whenGetCharge(String gatewayAccountId, String chargeId) {
        return mockClient.when(request()
                .withMethod(GET)
                .withPath(format(CONNECTOR_MOCK_CHARGE_PATH, gatewayAccountId, chargeId))
        );
    }

    public ForwardChainExpectation whenCreateRefund(long amount, String gatewayAccountId, String chargeId) {
        return mockClient.when(request()
                .withMethod(POST)
                .withPath(format(CONNECTOR_MOCK_CHARGE_REFUNDS_PATH, gatewayAccountId, chargeId))
                .withBody("{\"amount\":" + amount + "}")
        );
    }

    private ForwardChainExpectation whenGetRefundById(String gatewayAccountId, String chargeId, String refundId) {
        return mockClient.when(request()
                .withMethod(GET)
                .withPath(format(CONNECTOR_MOCK_CHARGE_REFUND_BY_ID_PATH, gatewayAccountId, chargeId, refundId))
        );
    }

    private ForwardChainExpectation whenGetAllRefunds(String gatewayAccountId, String chargeId) {
        return mockClient.when(request()
                .withMethod(GET)
                .withPath(format(CONNECTOR_MOCK_CHARGE_REFUNDS_PATH, gatewayAccountId, chargeId))
        );
    }

    private ForwardChainExpectation whenGetChargeEvents(String gatewayAccountId, String chargeId) {
        return mockClient.when(request()
                .withMethod(GET)
                .withPath(format(CONNECTOR_MOCK_CHARGE_EVENTS_PATH, gatewayAccountId, chargeId))
        );
    }

    public ForwardChainExpectation whenSearchCharges(String gatewayAccountId, String reference, String email, String state, String fromDate, String toDate) {
        return whenSearchCharges(gatewayAccountId, reference, email, state, fromDate, toDate, null, null);
    }

    public ForwardChainExpectation whenSearchCharges(String gatewayAccountId, String reference, String email, String state, String fromDate, String toDate, String page, String displaySize) {
        return mockClient.when(request()
                .withMethod(GET)
                .withPath(format(CONNECTOR_MOCK_CHARGES_PATH, gatewayAccountId))
                .withHeader(ACCEPT, APPLICATION_JSON)
                .withQueryStringParameters(notNullQueryParamsFrom(reference, email, state, fromDate, toDate, page, displaySize))
        );
    }

    private Parameter[] notNullQueryParamsFrom(String reference, String email, String state, String fromDate, String toDate, String page, String displaySize) {
        List<Parameter> params = newArrayList();
        if (isNotBlank(reference)) {
            params.add(Parameter.param(REFERENCE_KEY, reference));
        }
        if (isNotBlank(email)) {
            params.add(Parameter.param(EMAIL_KEY, email));
        }
        if (isNotBlank(state)) {
            params.add(Parameter.param(STATE_KEY, state));
        }
        if (isNotBlank(fromDate)) {
            params.add(Parameter.param(FROM_DATE_KEY, fromDate));
        }
        if (isNotBlank(toDate)) {
            params.add(Parameter.param(TO_DATE_KEY, toDate));
        }
        if (isNotBlank(page)) {
            params.add(Parameter.param("page", page));
        }
        if (isNotBlank(displaySize)) {
            params.add(Parameter.param("display_size", displaySize));
        }
        return params.toArray(new Parameter[0]);
    }

    private ForwardChainExpectation whenCancelCharge(String paymentId, String accountId) {
        return mockClient.when(request()
                .withMethod(POST)
                .withPath(connectorCancelChargePathFor(paymentId, accountId)));
    }

    private String connectorCancelChargePathFor(String paymentId, String accountId) {
        return format(CONNECTOR_MOCK_CHARGE_PATH + "/cancel", accountId, paymentId);
    }

    private HttpResponse withStatusAndErrorMessage(int statusCode, String errorMsg) {
        return response()
                .withStatusCode(statusCode)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(jsonString("message", errorMsg));
    }

    public void verifyCreateChargeConnectorRequest(int amount, String gatewayAccountId, String returnUrl, String description, String reference) {
        mockClient.verify(request()
                        .withMethod(POST)
                        .withPath(format(CONNECTOR_MOCK_CHARGES_PATH, gatewayAccountId))
                        .withBody(createChargePayload(amount, returnUrl, description, reference)),
                once()
        );
    }

    public void verifyCancelCharge(String paymentId, String accountId) {
        mockClient.verify(request()
                        .withMethod(POST)
                        .withPath(connectorCancelChargePathFor(paymentId, accountId)),
                once());
    }

    public void respondBadRequest_whenCreateARefund(String reason, int amount, String gatewayAccountId, String chargeId) {
        whenCreateRefund(amount, gatewayAccountId, chargeId)
                .respond(response()
                        .withStatusCode(BAD_REQUEST_400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(new JsonStringBuilder()
                                .add("reason", reason)
                                .add("message", "A message that should be completely ignored (only log)").build()));
    }
}
