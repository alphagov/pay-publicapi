package uk.gov.pay.api.utils.mocks;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import org.mockserver.client.server.ForwardChainExpectation;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.Parameter;
import uk.gov.pay.api.it.fixtures.PaymentRefundJsonFixture;
import uk.gov.pay.api.it.fixtures.PaymentSingleResultBuilder;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CardDetails;
import uk.gov.pay.api.model.PaymentState;
import uk.gov.pay.api.model.links.Link;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.verify.VerificationTimes.once;
import static uk.gov.pay.api.it.GetPaymentITest.AWAITING_CAPTURE_REQUEST;
import static uk.gov.pay.api.it.fixtures.PaymentSingleResultBuilder.aSuccessfulSinglePayment;
import static uk.gov.pay.api.utils.JsonStringBuilder.jsonString;
import static uk.gov.pay.api.utils.mocks.ChargeResponseFromConnector.ChargeResponseFromConnectorBuilder.aCreateOrGetChargeResponseFromConnector;
import static uk.gov.pay.api.utils.mocks.CreateChargeRequestParams.CreateChargeRequestParamsBuilder.aCreateChargeRequestParams;

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
    private static final String FROM_DATE_KEY = "from_date";
    private static final String TO_DATE_KEY = "to_date";
    private static final DateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public ConnectorMockClient(int port, String baseUrl) {
        super(port, baseUrl);
    }

    private String buildChargeResponse(ChargeResponseFromConnector responseFromConnector) {
        PaymentSingleResultBuilder resultBuilder = aSuccessfulSinglePayment()
                .withChargeId(responseFromConnector.getChargeId())
                .withAmount(responseFromConnector.getAmount())
                .withMatchingReference(responseFromConnector.getReference())
                .withEmail(responseFromConnector.getEmail())
                .withDescription(responseFromConnector.getDescription())
                .withState(responseFromConnector.getState())
                .withReturnUrl(responseFromConnector.getReturnUrl())
                .withCreatedDate(responseFromConnector.getCreatedDate())
                .withLanguage(responseFromConnector.getLanguage())
                .withPaymentProvider(responseFromConnector.getPaymentProvider())
                .withDelayedCapture(responseFromConnector.isDelayedCapture())
                .withLinks(responseFromConnector.getLinks())
                .withSettlementSummary(responseFromConnector.getSettlementSummary())
                .withCardDetails(responseFromConnector.getCardDetails());

        if (responseFromConnector.getRefundSummary()!= null) {
            resultBuilder.withRefundSummary(responseFromConnector.getRefundSummary());
        }
        if (responseFromConnector.getGatewayTransactionId() != null) {
            resultBuilder.withGatewayTransactionId(responseFromConnector.getGatewayTransactionId());
        }
        if (responseFromConnector.getCorporateCardSurcharge() != null) {
            resultBuilder.withCorporateCardSurcharge(responseFromConnector.getCorporateCardSurcharge());
        }
        if (responseFromConnector.getTotalAmount() != null) {
            resultBuilder.withTotalAmount(responseFromConnector.getTotalAmount());
        }
        if(responseFromConnector.getFee() != null){
            resultBuilder.withFee(responseFromConnector.getFee());
        }
        if(responseFromConnector.getNetAmount() != null){
            resultBuilder.withNetAmount(responseFromConnector.getNetAmount());
        }
        System.out.println("net amount : " + responseFromConnector.getNetAmount());
        System.out.println(responseFromConnector.getFee());
        responseFromConnector.getMetadata().ifPresent(m -> resultBuilder.withMetadata(m));

        return resultBuilder.build();
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

    private String chargeEventsLocation(String accountId, String chargeId) {
        return baseUrl + format(CONNECTOR_MOCK_CHARGE_EVENTS_PATH, accountId, chargeId);
    }

    public void respondOk_whenCreateCharge(String gatewayAccountId, CreateChargeRequestParams requestParams) {

        var responseFromConnector = aCreateOrGetChargeResponseFromConnector()
                .withAmount(requestParams.getAmount())
                .withChargeId("chargeId")
                .withState(new PaymentState("created", false, null, null))
                .withReturnUrl(requestParams.getReturnUrl())
                .withDescription(requestParams.getDescription())
                .withReference(requestParams.getReference())
                .withPaymentProvider("Sandbox")
                .withGatewayTransactionId("gatewayTransactionId")
                .withCreatedDate(SDF.format(new Date()))
                .withLanguage(SupportedLanguage.ENGLISH)
                .withDelayedCapture(false)
                .withCardDetails(new CardDetails("1234", "123456", "Mr. Payment", "12/19", null, "Mastercard"))
                .withLink(validGetLink(chargeLocation(gatewayAccountId, "chargeId"), "self"))
                .withLink(validGetLink(nextUrl("chargeTokenId"), "next_url"))
                .withLink(validPostLink(nextUrlPost(), "next_url_post", "application/x-www-form-urlencoded", getChargeIdTokenMap("chargeTokenId")));

        if (!requestParams.getMetadata().isEmpty())
            responseFromConnector.withMetadata(requestParams.getMetadata());
        
        if (requestParams.getEmail() != null) {
            responseFromConnector.withEmail(requestParams.getEmail());
        }
        
        if (requestParams.getCardholderName().isPresent() || requestParams.getAddressLine1().isPresent() || 
                requestParams.getAddressLine2().isPresent() || requestParams.getAddressPostcode().isPresent() || 
                requestParams.getAddressCity().isPresent() || requestParams.getAddressCountry().isPresent()) {
            Address billingAddress = new Address(requestParams.getAddressLine1().orElse(null), requestParams.getAddressLine2().orElse(null),
                    requestParams.getAddressPostcode().orElse(null), requestParams.getAddressCity().orElse(null), requestParams.getAddressCountry().orElse(null));
            CardDetails cardDetails = new CardDetails(null, null, requestParams.getCardholderName().orElse(null),
                    null, billingAddress, null);
            responseFromConnector.withCardDetails(cardDetails);
        }

        whenCreateCharge(gatewayAccountId, requestParams)
                .respond(response()
                        .withStatusCode(CREATED_201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, chargeLocation(gatewayAccountId, "chargeId"))
                        .withBody(buildChargeResponse(responseFromConnector.build())));
    }

    public void respondOk_whenCreateCharge(String chargeTokenId, String gatewayAccountId, ChargeResponseFromConnector responseFromConnector) {
        ChargeResponseFromConnector build = aCreateOrGetChargeResponseFromConnector(responseFromConnector)
                .withLink(validGetLink(chargeLocation(gatewayAccountId, responseFromConnector.getChargeId()), "self"))
                .withLink(validGetLink(nextUrl(chargeTokenId), "next_url"))
                .withLink(validPostLink(nextUrlPost(), "next_url_post", "application/x-www-form-urlencoded", getChargeIdTokenMap(chargeTokenId))).build();
        CreateChargeRequestParams params = aCreateChargeRequestParams()
                .withAmount(responseFromConnector.getAmount().intValue())
                .withReturnUrl(responseFromConnector.getReturnUrl())
                .withReference(responseFromConnector.getReference())
                .withDescription(responseFromConnector.getDescription())
                .withEmail(responseFromConnector.getEmail())
                .build();

        whenCreateCharge(gatewayAccountId, params)
                .respond(response()
                        .withStatusCode(CREATED_201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withHeader(LOCATION, chargeLocation(gatewayAccountId, responseFromConnector.getChargeId()))
                        .withBody(buildChargeResponse(build)));
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

    public void respondOk_whenSearchCharges(String accountId, String expectedResponse) {
        whenSearchCharges(accountId, null, null, null, null, null, null, null, null, null)
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

    public void respondWithChargeFound(String chargeTokenId, String gatewayAccountId, ChargeResponseFromConnector chargeResponseFromConnector) {
        String chargeResponseBody;
        String chargeId = chargeResponseFromConnector.getChargeId();

        var responseFromConnector = aCreateOrGetChargeResponseFromConnector(chargeResponseFromConnector)
                .withLink(validGetLink(chargeLocation(gatewayAccountId, chargeId), "self"));

        if (AWAITING_CAPTURE_REQUEST == chargeResponseFromConnector.getState()) {

            responseFromConnector.withLink(validGetLink(chargeLocation(gatewayAccountId, chargeId) + "/refunds", "refunds"))
                    .withLink(validPostLink(chargeLocation(gatewayAccountId, chargeId) + "/capture", "capture", "application/x-www-form-urlencoded", new HashMap<>()))
                    .build();

            chargeResponseBody = buildChargeResponse(responseFromConnector.build());

        } else {

            responseFromConnector.withLink(validGetLink(chargeLocation(gatewayAccountId, chargeId) + "/refunds", "refunds"))
                    .withLink(validGetLink(nextUrl(chargeId), "next_url"))
                    .withLink(validPostLink(nextUrlPost(), "next_url_post", "application/x-www-form-urlencoded", getChargeIdTokenMap(chargeTokenId)))
                    .build();

            chargeResponseBody = buildChargeResponse(responseFromConnector.build());
        }
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
                .respond(withStatusAndErrorMessage(INTERNAL_SERVER_ERROR_500, "server error"));

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

    public ForwardChainExpectation whenCreateCharge(String gatewayAccountId, CreateChargeRequestParams createChargeRequestParams) {
        return mockClient.when(request()
                .withMethod(POST)
                .withPath(format(CONNECTOR_MOCK_CHARGES_PATH, gatewayAccountId))
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withBody(json(createChargePayload(createChargeRequestParams), MatchType.ONLY_MATCHING_FIELDS))
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
                .withBody(jsonString("message", List.of(errorMsg)));
    }

    //"Gson can not automatically deserialize the pure inner classes since their no-args constructor"
    private Map<String, String> getChargeIdTokenMap(String chargeTokenId) {
        final Map<String, String> chargeTokenIdMap = new HashMap<>();
        chargeTokenIdMap.put("chargeTokenId", chargeTokenId);
        return chargeTokenIdMap;
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
                                .add("message", List.of("A message that should be completely ignored (only log)")).build()));
    }
}
