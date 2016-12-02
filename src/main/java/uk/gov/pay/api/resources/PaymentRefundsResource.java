package uk.gov.pay.api.resources;

import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.exception.CreateRefundException;
import uk.gov.pay.api.exception.GetRefundException;
import uk.gov.pay.api.exception.GetRefundsException;
import uk.gov.pay.api.model.*;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.UriBuilder.fromPath;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.http.HttpStatus.SC_OK;

@Path(PaymentRefundsResource.PAYMENT_REFUNDS_PATH)
@Api(value = "/refunds", description = "Public Api Endpoints for Refunds")
@Produces({"application/json"})
public class PaymentRefundsResource {
    private static final Logger logger = LoggerFactory.getLogger(PaymentRefundsResource.class);

    private static final String API_VERSION_PATH = "/v1";
    private static final String PATH_PAYMENT_KEY = "paymentId";
    public static final String PATH_REFUND_KEY = "refundId";

    private static final String PAYMENTS_ID_PLACEHOLDER = "{" + PATH_PAYMENT_KEY + "}";
    public static final String PAYMENT_BY_ID_PATH = API_VERSION_PATH + "/payments/" + PAYMENTS_ID_PLACEHOLDER;
    public static final String PAYMENT_REFUNDS_PATH = PAYMENT_BY_ID_PATH + "/refunds";
    public static final String PAYMENT_REFUND_BY_ID_PATH = PAYMENT_REFUNDS_PATH + "/{refundId}";

    private static final String CONNECTOR_ACCOUNT_RESOURCE = API_VERSION_PATH + "/api/accounts/%s";
    private static final String CONNECTOR_CHARGES_RESOURCE = CONNECTOR_ACCOUNT_RESOURCE + "/charges";
    private static final String CONNECTOR_CHARGE_RESOURCE = CONNECTOR_CHARGES_RESOURCE + "/%s";

    private static final String CONNECTOR_CHARGE_REFUNDS_RESOURCE = CONNECTOR_CHARGE_RESOURCE + "/refunds";
    private static final String CONNECTOR_CHARGE_REFUND_BY_ID_RESOURCE = CONNECTOR_CHARGE_REFUNDS_RESOURCE + "/%s";

    private final String baseUrl;
    private final Client client;
    private final String connectorUrl;

    public PaymentRefundsResource(String baseUrl, Client client, String connectorUrl) {
        this.baseUrl = baseUrl;
        this.client = client;
        this.connectorUrl = connectorUrl;
    }

    @GET
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Get all refunds for a payment.",
            notes = "Return refunds for a payment. " +
                    "The Authorisation token needs to be specified in the 'authorization' header as 'authorization: Bearer YOUR_API_KEY_HERE'",
            code = 200)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = PaymentError.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response getRefunds(@ApiParam(value = "accountId", hidden = true) @Auth String accountId,
                               @PathParam(PATH_PAYMENT_KEY) String paymentId) {

        logger.info("Get refunds for payment request - paymentId={}", paymentId);
        Response connectorResponse = client
                .target(getConnectorUrl(format(CONNECTOR_CHARGE_REFUNDS_RESOURCE, accountId, paymentId)))
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

    @GET
    @Path("/{refundId}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Find payment refund by ID",
            notes = "Return payment refund information by Refund ID " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            code = 200)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = PaymentError.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response getRefundById(@ApiParam(value = "accountId", hidden = true) @Auth String accountId,
                                  @PathParam(PATH_PAYMENT_KEY) String paymentId,
                                  @PathParam(PATH_REFUND_KEY) String refundId) {

        logger.info("Payment refund request - paymentId={}, refundId={}", paymentId, refundId);
        Response connectorResponse = client
                .target(getConnectorUrl(format(CONNECTOR_CHARGE_REFUND_BY_ID_RESOURCE, accountId, paymentId, refundId)))
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

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @ApiOperation(
            value = "Submit a refund for a payment",
            notes = "Return issued refund information. " +
                    "The Authorisation token needs to be specified in the 'authorization' header as 'authorization: Bearer YOUR_API_KEY_HERE'",
            code = 202)
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "ACCEPTED"),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = PaymentError.class),
            @ApiResponse(code = 412, message = "Refund amount available mismatch"),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response submitRefund(@ApiParam(value = "accountId", hidden = true) @Auth String accountId,
                                 @ApiParam(value = "paymentId", required = true) @PathParam(PATH_PAYMENT_KEY) String paymentId,
                                 @ApiParam(value = "requestPayload", required = true) CreatePaymentRefundRequest requestPayload) {

        logger.info("Create a refund for payment request - paymentId={}", paymentId);

        Integer refundAmountAvailable = requestPayload.getRefundAmountAvailable()
                .orElseGet(() -> {
                    Response getChargeResponse = client
                            .target(getConnectorUrl(format(CONNECTOR_CHARGE_RESOURCE, accountId, paymentId)))
                            .request()
                            .get();

                    ChargeFromResponse chargeFromResponse = getChargeResponse.readEntity(ChargeFromResponse.class);
                    return Long.valueOf(chargeFromResponse.getRefundSummary().getAmountAvailable()).intValue();
        });

        ImmutableMap<String, Object> payloadMap = ImmutableMap.of("amount", requestPayload.getAmount(), "refund_amount_available", refundAmountAvailable);
        String connectorPayload = new GsonBuilder().create().toJson(
                payloadMap);

        Response connectorResponse = client
                .target(getConnectorUrl(format(CONNECTOR_CHARGE_REFUNDS_RESOURCE, accountId, paymentId)))
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
