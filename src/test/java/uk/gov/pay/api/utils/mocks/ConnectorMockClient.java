package uk.gov.pay.api.utils.mocks;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import org.mockserver.client.server.ForwardChainExpectation;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.Parameter;
import uk.gov.pay.api.it.fixtures.PaymentRefundJsonFixture;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.model.SettlementSummary;
import uk.gov.pay.api.model.links.Link;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.eclipse.jetty.http.HttpStatus.ACCEPTED_202;
import static org.eclipse.jetty.http.HttpStatus.BAD_REQUEST_400;
import static org.eclipse.jetty.http.HttpStatus.CREATED_201;
import static org.eclipse.jetty.http.HttpStatus.INTERNAL_SERVER_ERROR_500;
import static org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404;
import static org.eclipse.jetty.http.HttpStatus.NO_CONTENT_204;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.eclipse.jetty.http.HttpStatus.PRECONDITION_FAILED_412;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.once;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonString;

public class ConnectorMockClient extends BaseConnectorMockClient {

    private static final String CONNECTOR_MOCK_CHARGE_EVENTS_PATH = CONNECTOR_MOCK_CHARGE_PATH + "/events";
    private static final String CONNECTOR_MOCK_CHARGE_REFUNDS_PATH = CONNECTOR_MOCK_CHARGE_PATH + "/refunds";
    private static final String CONNECTOR_MOCK_CHARGE_REFUND_BY_ID_PATH = CONNECTOR_MOCK_CHARGE_REFUNDS_PATH + "/%s";
    private static final String REFERENCE_KEY = "reference";
    private static final String EMAIL_KEY = "email";
    private static final String STATE_KEY = "state";
    private static final String CARD_BRAND = "master-card";
    private static final String CARD_BRAND_KEY = "card_brand";
    private static final String CARDHOLDER_NAME_KEY = "cardholder_name";
    public static final String FIRST_DIGITS_CARD_NUMBER_KEY = "first_digits_card_number";
    public static final String LAST_DIGITS_CARD_NUMBER_KEY = "last_digits_card_number";
    private static final String CARD_BRAND_LABEL = "Mastercard";
    private static final String FROM_DATE_KEY = "from_date";
    private static final String TO_DATE_KEY = "to_date";

    public ConnectorMockClient(int port, String baseUrl) {
        super(port, baseUrl);
    }

    private String buildChargeResponse(long amount, String chargeId, PaymentState state, String returnUrl, String description,
                                       String reference, String email, String paymentProvider, String gatewayTransactionId, String createdDate,
                                       SupportedLanguage language, boolean delayedCapture, Long corporateCardSurcharge, Long totalAmount, RefundSummary refundSummary, SettlementSummary settlementSummary, CardDetails cardDetails,
                                       ImmutableMap<?, ?>... links) {
        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder()
                .add("charge_id", chargeId)
                .add("amount", amount)
                .add("reference", reference)
                .add("email", email)
                .add("description", description)
                .add("state", state)
                .add("return_url", returnUrl)
                .add("payment_provider", paymentProvider)
                .add("card_brand", CARD_BRAND_LABEL)
                .add("created_date", createdDate)
                .add("language", language.toString())
                .add("delayed_capture", delayedCapture)
                .add("links", asList(links))
                .add("refund_summary", refundSummary)
                .add("settlement_summary", settlementSummary)
                .add("card_details", cardDetails);

        if (gatewayTransactionId != null) {
            jsonStringBuilder.add("gateway_transaction_id", gatewayTransactionId);
        }
        
        if (corporateCardSurcharge != null) {
            jsonStringBuilder.add("corporate_card_surcharge", corporateCardSurcharge);
        }
        
        if (totalAmount != null) {
            jsonStringBuilder.add("total_amount", totalAmount);
        }

        return jsonStringBuilder.build();
    }

    private String buildGetRefundResponse(String refundId, int amount, int refundAmountAvailable, String status, String createdDate) {
        List<Map<String, Link>> links = new ArrayList<>();
        links.add(ImmutableMap.of("self", new Link("http://server:port/self-link")));
        links.add(ImmutableMap.of("payment", new Link("http://server:port/payment-link")));

        JsonStringBuilder jsonStringBuilder = new JsonStringBuilder()
                .add("refund_id", refundId)
                .add("amount", amount)
                .add("refund_amount_available", refundAmountAvailable)
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

    @Override
    String nextUrlPost() {
        return "http://frontend_card/charge/";
    }
    
    String captureUrlPost() {
        return "http:///";
    }

    private String chargeEventsLocation(String accountId, String chargeId) {
        return baseUrl + format(CONNECTOR_MOCK_CHARGE_EVENTS_PATH, accountId, chargeId);
    }

    public void respondOk_whenCreateCharge(int amount, String gatewayAccountId, String chargeId, String chargeTokenId, PaymentState state, String returnUrl,
                                           String description, String reference, String email, String paymentProvider, String createdDate,
                                           SupportedLanguage language, boolean delayedCapture, RefundSummary refundSummary, SettlementSummary settlementSummary,
                                           CardDetails cardDetails) {

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
                                language,
                                delayedCapture,
                                null,
                                null,
                                refundSummary,
                                settlementSummary,
                                cardDetails,
                                validGetLink(chargeLocation(gatewayAccountId, chargeId), "self"),
                                validGetLink(nextUrl(chargeTokenId), "next_url"), validPostLink(nextUrlPost(), "next_url_post", "application/x-www-form-urlencoded",
                                        new HashMap<String, String>() {{
                                            put("chargeTokenId", chargeTokenId);
                                        }}))));
    }


    public void respondAccepted_whenCreateARefund(int amount, int refundAmountAvailable, String gatewayAccountId, String chargeId, String refundId, String status, String createdDate) {
        whenCreateRefund(amount, refundAmountAvailable, gatewayAccountId, chargeId)
                .respond(response()
                        .withStatusCode(ACCEPTED_202)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(buildGetRefundResponse(refundId, amount, refundAmountAvailable, status, createdDate))
                );
    }

    public void respondOk_whenSearchCharges(String accountId, String reference, String email, String state, String cardBrand, String cardHolderName, String firstDigitsCardNumber, String lastDigitsCardNumber, String fromDate, String toDate, String expectedResponse) {
        whenSearchCharges(accountId, reference, email, state, cardBrand, cardHolderName, firstDigitsCardNumber, lastDigitsCardNumber, fromDate, toDate)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(expectedResponse)
                );

    }

    public void respondOk_whenSearchChargesWithPageAndSize(String accountId, String reference, String email, String page, String displaySize, String expectedResponse) {
        whenSearchCharges(accountId, reference, email, null, null, null, null, null, null, null, page, displaySize)
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

    public void respondPreconditionFailed_whenCreateRefund(int amount, int refundAmountAvailable, String gatewayAccountId, String errorMsg, String chargeId) {
        whenCreateRefund(amount, refundAmountAvailable, gatewayAccountId, chargeId)
                .respond(withStatusAndErrorMessage(PRECONDITION_FAILED_412, errorMsg));
    }

    public void respondWithChargeFound(long amount, String gatewayAccountId, String chargeId, PaymentState state, String returnUrl,
                                       String description, String reference, String email, String paymentProvider, String createdDate,
                                       SupportedLanguage language, boolean delayedCapture, String chargeTokenId, RefundSummary refundSummary, SettlementSummary settlementSummary,
                                       CardDetails cardDetails) {
        respondWithChargeFound(amount, gatewayAccountId, chargeId, state, returnUrl,
                description, reference, email, paymentProvider, createdDate,
                language, delayedCapture, chargeTokenId, refundSummary, settlementSummary,
                cardDetails, null, null);
    }

    public void respondWithChargeFound(long amount, String gatewayAccountId, String chargeId, PaymentState state, String returnUrl,
                                       String description, String reference, String email, String paymentProvider, String createdDate,
                                       SupportedLanguage language, boolean delayedCapture, String chargeTokenId, RefundSummary refundSummary, SettlementSummary settlementSummary,
                                       CardDetails cardDetails, Long corporateCardSurcharge, Long totalAmount) {
        String chargeResponseBody = buildChargeResponse(amount, chargeId, state, returnUrl,
                description, reference, email, paymentProvider, gatewayAccountId, createdDate, language, delayedCapture,
                corporateCardSurcharge, totalAmount, refundSummary, settlementSummary, cardDetails,
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

    public void respondWithChargeFoundAndCaptureUrl(long amount, String gatewayAccountId, String chargeId, PaymentState state, String returnUrl,
                                       String description, String reference, String email, String paymentProvider, String createdDate,
                                       SupportedLanguage language, boolean delayedCapture, RefundSummary refundSummary, SettlementSummary settlementSummary,
                                       CardDetails cardDetails, Long corporateCardSurcharge, Long totalAmount) {
        String chargeResponseBody = buildChargeResponse(amount, chargeId, state, returnUrl,
                    description, reference, email, paymentProvider, gatewayAccountId, createdDate, language, delayedCapture,
                    corporateCardSurcharge, totalAmount, refundSummary, settlementSummary, cardDetails,
                    validGetLink(chargeLocation(gatewayAccountId, chargeId), "self"),
                    validGetLink(chargeLocation(gatewayAccountId, chargeId) + "/refunds", "refunds"),
                    validPostLink(chargeLocation(gatewayAccountId, chargeId) + "/capture", "capture", "application/x-www-form-urlencoded",
                           new HashMap<>()));
        
        whenGetCharge(gatewayAccountId, chargeId)
                .respond(response()
                        .withStatusCode(OK_200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(chargeResponseBody));
    }

    public void respondWithGetRefundById(String gatewayAccountId, String chargeId, String refundId, int amount, int totalRefundAmountAvailable, String refundStatus, String createdDate) {
        String refundResponse = buildGetRefundResponse(refundId, amount, totalRefundAmountAvailable, refundStatus, createdDate);
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

    public void respondOk_whenCaptureCharge(String paymentId, String accountId) {
        whenCaptureCharge(paymentId, accountId)
                .respond(response()
                        .withStatusCode(NO_CONTENT_204));
    }

    public void respondChargeNotFound_WhenCancelCharge(String paymentId, String accountId, String errorMsg) {
        respond_WhenCancelCharge(paymentId, accountId, errorMsg, NOT_FOUND_404);
    }

    public void respondChargeNotFound_WhenCaptureCharge(String paymentId, String accountId, String errorMsg) {
        respond_WhenCaptureCharge(paymentId, accountId, errorMsg, NOT_FOUND_404);
    }

    public void respondBadRequest_WhenCancelCharge(String paymentId, String accountId, String errorMessage) {
        respond_WhenCancelCharge(paymentId, accountId, errorMessage, BAD_REQUEST_400);
    }

    public void respondBadRequest_WhenCaptureCharge(String paymentId, String accountId, String errorMessage) {
        respond_WhenCaptureCharge(paymentId, accountId, errorMessage, BAD_REQUEST_400);
    }

    public void respond_WhenCancelCharge(String paymentId, String accountId, String errorMessage, int status) {
        whenCancelCharge(paymentId, accountId)
                .respond(withStatusAndErrorMessage(status, errorMessage));
    }

    public void respond_WhenCaptureCharge(String paymentId, String accountId, String errorMessage, int status) {
        whenCaptureCharge(paymentId, accountId)
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

    private ForwardChainExpectation whenCreateRefund(int amount, int refundAmountAvailable, String gatewayAccountId, String chargeId) {
        String payload = new GsonBuilder().create().toJson(
                ImmutableMap.of("amount", amount, "refund_amount_available", refundAmountAvailable));
        return mockClient.when(request()
                .withMethod(POST)
                .withPath(format(CONNECTOR_MOCK_CHARGE_REFUNDS_PATH, gatewayAccountId, chargeId))
                .withBody(payload)
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

    public ForwardChainExpectation whenSearchCharges(String gatewayAccountId, String reference, String email, String state, String cardBrand, String cardHolderName, String firstDigitsCardNumber, String lastDigitsCardNumber, String fromDate, String toDate) {
        return whenSearchCharges(gatewayAccountId, reference, email, state, cardBrand, cardHolderName, firstDigitsCardNumber, lastDigitsCardNumber, fromDate, toDate, null, null);
    }

    public ForwardChainExpectation whenSearchCharges(String gatewayAccountId, String reference, String email, String state, String cardBrand, String cardHolderName, String firstDigitsCardNumber, String lastDigitsCardNumber, String fromDate, String toDate, String page, String displaySize) {
        return mockClient.when(request()
                .withMethod(GET)
                .withPath(format(CONNECTOR_MOCK_CHARGES_PATH, gatewayAccountId))
                .withHeader(ACCEPT, APPLICATION_JSON)
                .withQueryStringParameters(notNullQueryParamsFrom(reference, email, state, cardBrand, cardHolderName, firstDigitsCardNumber, lastDigitsCardNumber, fromDate, toDate, page, displaySize))
        );
    }

    private Parameter[] notNullQueryParamsFrom(String reference, String email, String state, String cardBrand, String cardHolderName, String firstDigitsCardNumber, String lastDigitsCardNumber, String fromDate, String toDate, String page, String displaySize) {
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
        if (isNotBlank(cardBrand)) {
            params.add(Parameter.param(CARD_BRAND_KEY, CARD_BRAND.toLowerCase()));
        }
        if (isNotBlank(cardHolderName)) {
            params.add(Parameter.param(CARDHOLDER_NAME_KEY, cardHolderName));
        }
        if (isNotBlank(firstDigitsCardNumber)) {
            params.add(Parameter.param(FIRST_DIGITS_CARD_NUMBER_KEY, firstDigitsCardNumber));
        }
        if (isNotBlank(lastDigitsCardNumber)) {
            params.add(Parameter.param(LAST_DIGITS_CARD_NUMBER_KEY, lastDigitsCardNumber));
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

    private ForwardChainExpectation whenCaptureCharge(String paymentId, String accountId) {
        return mockClient.when(request()
                .withMethod(POST)
                .withPath(connectorCaptureChargePathFor(paymentId, accountId)));
    }

    private String connectorCancelChargePathFor(String paymentId, String accountId) {
        return format(CONNECTOR_MOCK_CHARGE_PATH + "/cancel", accountId, paymentId);
    }

    private String connectorCaptureChargePathFor(String paymentId, String accountId) {
        return format(CONNECTOR_MOCK_CHARGE_PATH + "/capture", accountId, paymentId);
    }

    private HttpResponse withStatusAndErrorMessage(int statusCode, String errorMsg) {
        return response()
                .withStatusCode(statusCode)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(jsonString("message", errorMsg));
    }

    public void verifyCancelCharge(String paymentId, String accountId) {
        mockClient.verify(request()
                        .withMethod(POST)
                        .withPath(connectorCancelChargePathFor(paymentId, accountId)),
                once());
    }

    public void verifyCaptureCharge(String paymentId, String accountId) {
        mockClient.verify(request()
                        .withMethod(POST)
                        .withPath(connectorCaptureChargePathFor(paymentId, accountId)),
                once());
    }

    public void respondBadRequest_whenCreateARefund(String reason, int amount, int refundAmountAvailable, String gatewayAccountId, String chargeId) {
        whenCreateRefund(amount, refundAmountAvailable, gatewayAccountId, chargeId)
                .respond(response()
                        .withStatusCode(BAD_REQUEST_400)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(new JsonStringBuilder()
                                .add("reason", reason)
                                .add("message", "A message that should be completely ignored (only log)").build()));
    }
}
