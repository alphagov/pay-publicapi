package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CaptureChargeException;
import uk.gov.pay.api.exception.CreateRefundException;
import uk.gov.pay.api.exception.GetEventsException;
import uk.gov.pay.api.exception.GetRefundException;
import uk.gov.pay.api.exception.GetRefundsException;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.CreatePaymentRefundRequest;
import uk.gov.pay.api.model.PaymentEvents;
import uk.gov.pay.api.model.PaymentWithAllLinks;
import uk.gov.pay.api.model.RefundFromConnector;
import uk.gov.pay.api.model.RefundResponse;
import uk.gov.pay.api.model.RefundsFromConnector;
import uk.gov.pay.api.model.RefundsResponse;
import uk.gov.pay.api.model.ValidCreatePaymentRequest;
import uk.gov.pay.api.service.CancelPaymentService;
import uk.gov.pay.api.service.CapturePaymentService;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.CreatePaymentService;
import uk.gov.pay.api.service.GetPaymentService;
import uk.gov.pay.api.service.PaymentSearchService;
import uk.gov.pay.api.service.PublicApiUriGenerator;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.UriBuilder.fromPath;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.http.HttpStatus.SC_OK;

public class PaymentsResource {

    private static final Logger logger = LoggerFactory.getLogger(PaymentsResource.class);

    private static final String API_VERSION_PATH = "/v1";
    private static final String CONNECTOR_ACCOUNT_RESOURCE = API_VERSION_PATH + "/api/accounts/%s";
    private static final String CONNECTOR_CHARGES_RESOURCE = CONNECTOR_ACCOUNT_RESOURCE + "/charges";
    private static final String CONNECTOR_CHARGE_RESOURCE = CONNECTOR_CHARGES_RESOURCE + "/%s";
    private static final String CONNECTOR_CHARGE_REFUNDS_RESOURCE = CONNECTOR_CHARGE_RESOURCE + "/refunds";
    private static final String CONNECTOR_CHARGE_REFUND_BY_ID_RESOURCE = CONNECTOR_CHARGE_REFUNDS_RESOURCE + "/%s";
    
    private final Client client;
    private final CreatePaymentService createPaymentService;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final ConnectorUriGenerator connectorUriGenerator;
    private final PaymentSearchService paymentSearchService;
    private final GetPaymentService getPaymentService;
    private final CapturePaymentService capturePaymentService;
    private final String connectorUrl;
    private final String baseUrl;
    private final CancelPaymentService cancelPaymentService;

    @Inject
    public PaymentsResource(Client client,
                            CreatePaymentService createPaymentService,
                            PaymentSearchService paymentSearchService,
                            PublicApiUriGenerator publicApiUriGenerator,
                            ConnectorUriGenerator connectorUriGenerator,
                            GetPaymentService getPaymentService,
                            CapturePaymentService capturePaymentService,
                            CancelPaymentService cancelPaymentService,
                            PublicApiConfig configuration) {
        this.client = client;
        this.createPaymentService = createPaymentService;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.connectorUriGenerator = connectorUriGenerator;
        this.paymentSearchService = paymentSearchService;
        this.getPaymentService = getPaymentService;
        this.capturePaymentService = capturePaymentService;
        this.connectorUrl = configuration.getConnectorUrl();
        this.baseUrl = configuration.getBaseUrl();
        this.cancelPaymentService = cancelPaymentService;
    }

    public Response getPayment(
            String paymentId,
            Account account
    ) {

        logger.info("Payment request - paymentId={}", paymentId);

        PaymentWithAllLinks payment = getPaymentService.getPayment(account, paymentId);

        logger.info("Payment returned - [ {} ]", payment);
        return Response.ok(payment).build();

    }

    public Response getPaymentEvents(
            String paymentId,
            Account account
    ) {

        logger.info("Payment events request - payment_id={}", paymentId);

        Response connectorResponse = client
                .target(connectorUriGenerator.chargeEventsURI(account, paymentId))
                .request()
                .get();

        if (connectorResponse.getStatus() == SC_OK) {

            JsonNode payload = connectorResponse.readEntity(JsonNode.class);
            URI paymentEventsLink = publicApiUriGenerator.getPaymentEventsURI(payload.get("charge_id").asText());

            URI paymentLink = publicApiUriGenerator.getPaymentURI(payload.get("charge_id").asText());

            PaymentEvents response =
                    PaymentEvents.createPaymentEventsResponse(payload, paymentLink.toString())
                            .withSelfLink(paymentEventsLink.toString());

            logger.info("Payment events returned - [ {} ]", response);

            return Response.ok(response).build();
        }

        throw new GetEventsException(connectorResponse);
    }

    public Response searchPayments(String reference,
                                   String email,
                                   String state,
                                   String cardBrand,
                                   String fromDate,
                                   String toDate,
                                   String pageNumber,
                                   String displaySize,
                                   String cardHolderName,
                                   String firstDigitsCardNumber,
                                   String lastDigitsCardNumber,
                                   Account account) {

        logger.info("Payments search request - [ {} ]",
                format("reference:%s, email: %s, status: %s, card_brand %s, fromDate: %s, toDate: %s, page: %s, display_size: %s, cardholder_name: %s, first_digits_card_number: %s, last_digits_card_number: %s",
                        reference, email, state, cardBrand, fromDate, toDate, pageNumber, displaySize, cardHolderName, firstDigitsCardNumber, lastDigitsCardNumber));

        return paymentSearchService.doSearch(account, reference, email, state, cardBrand,
                fromDate, toDate, pageNumber, displaySize, null, cardHolderName, firstDigitsCardNumber, lastDigitsCardNumber);
    }

    public Response createNewPayment(
            ValidCreatePaymentRequest validCreatePaymentRequest,
            Account account
    ) {
        logger.info("Payment create request passed validation and parsed to {}", validCreatePaymentRequest);

        PaymentWithAllLinks createdPayment = createPaymentService.create(account, validCreatePaymentRequest);

        Response response = Response
                .created(publicApiUriGenerator.getPaymentURI(createdPayment.getPayment().getPaymentId()))
                .entity(createdPayment)
                .build();

        logger.info("Payment returned (created): [ {} ]", createdPayment);
        return response;
    }

    public Response cancelPayment(
            String paymentId,
            Account account
    ) {

        logger.info("Payment cancel request - payment_id=[{}]", paymentId);

        return cancelPaymentService.cancel(account, paymentId);
    }

    public Response capturePayment(
            String paymentId,
            Account account
    ) {
        logger.info("Payment capture request - payment_id=[{}]", paymentId);

        Response connectorResponse = capturePaymentService.capture(account, paymentId);

        if (connectorResponse.getStatus() == HttpStatus.SC_NO_CONTENT) {
            connectorResponse.close();
            return Response.noContent().build();
        }

        throw new CaptureChargeException(connectorResponse);
    }

    public Response getRefunds(
            String paymentId,
            Account account
    ) {

        logger.info("Get refunds for payment request - paymentId={}", paymentId);
        Response connectorResponse = client
                .target(getConnectorUrl(format(CONNECTOR_CHARGE_REFUNDS_RESOURCE, account.getAccountId(), paymentId)))
                .request()
                .get();

        if (connectorResponse.getStatus() == SC_OK) {
            RefundsFromConnector refundsFromConnector = connectorResponse.readEntity(RefundsFromConnector.class);
            logger.debug("refund returned - [ {} ]", refundsFromConnector);
            RefundsResponse refundsResponse = RefundsResponse.valueOf(refundsFromConnector, baseUrl);

            return Response.ok(refundsResponse.serialize()).build();
        }

        throw new GetRefundsException(connectorResponse);
    }

    public Response getRefundById(
            String paymentId,
            String refundId,
            Account account
    ) {

        logger.info("Payment refund request - paymentId={}, refundId={}", paymentId, refundId);
        Response connectorResponse = client
                .target(getConnectorUrl(format(CONNECTOR_CHARGE_REFUND_BY_ID_RESOURCE, account.getAccountId(), paymentId, refundId)))
                .request()
                .get();

        if (connectorResponse.getStatus() == SC_OK) {
            RefundFromConnector refundFromConnector = connectorResponse.readEntity(RefundFromConnector.class);
            logger.info("refund returned - [ {} ]", refundFromConnector);

            RefundResponse refundResponse = RefundResponse.valueOf(refundFromConnector, paymentId, baseUrl);
            return Response.ok(refundResponse.serialize()).build();
        }
        throw new GetRefundException(connectorResponse);
    }

    public Response submitRefund(
            String paymentId,
            CreatePaymentRefundRequest requestPayload,
            Account account
    ) {

        logger.info("Create a refund for payment request - paymentId={}", paymentId);

        Integer refundAmountAvailable = requestPayload.getRefundAmountAvailable()
                .orElseGet(() -> {
                    Response getChargeResponse = client
                            .target(getConnectorUrl(format(CONNECTOR_CHARGE_RESOURCE, account.getAccountId(), paymentId)))
                            .request()
                            .get();

                    ChargeFromResponse chargeFromResponse = getChargeResponse.readEntity(ChargeFromResponse.class);
                    return Long.valueOf(chargeFromResponse.getRefundSummary().getAmountAvailable()).intValue();
                });

        ImmutableMap<String, Object> payloadMap = ImmutableMap.of("amount", requestPayload.getAmount(), "refund_amount_available", refundAmountAvailable);
        String connectorPayload = new GsonBuilder().create().toJson(
                payloadMap);

        Response connectorResponse = client
                .target(getConnectorUrl(format(CONNECTOR_CHARGE_REFUNDS_RESOURCE, account.getAccountId(), paymentId)))
                .request()
                .post(json(connectorPayload));

        if (connectorResponse.getStatus() == ACCEPTED.getStatusCode()) {
            RefundFromConnector refundFromConnector = connectorResponse.readEntity(RefundFromConnector.class);
            logger.debug("created refund returned - [ {} ]", refundFromConnector);
            RefundResponse refundResponse = RefundResponse.valueOf(refundFromConnector, paymentId, baseUrl);

            return Response.accepted(refundResponse.serialize()).build();
        }

        throw new CreateRefundException(connectorResponse);
    }

    private String getConnectorUrl(String urlPath) {
        return getConnectorUrl(urlPath, Collections.emptyList());
    }

    private String getConnectorUrl(String urlPath, List<Pair<String, String>> queryParams) {
        UriBuilder builder =
                fromPath(connectorUrl)
                        .path(urlPath);

        queryParams.stream().forEach(pair -> {
            if (isNotBlank(pair.getRight())) {
                builder.queryParam(pair.getKey(), pair.getValue());
            }
        });
        return builder.toString();
    }
}
