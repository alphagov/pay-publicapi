package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CreateRefundException;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.CreatePaymentRefundRequest;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.RefundFromConnector;
import uk.gov.pay.api.model.RefundResponse;
import uk.gov.pay.api.model.RefundsResponse;
import uk.gov.pay.api.model.search.card.RefundForSearchResult;
import uk.gov.pay.api.model.search.card.RefundResult;
import uk.gov.pay.api.resources.error.ApiErrorResponse;
import uk.gov.pay.api.service.GetPaymentRefundService;
import uk.gov.pay.api.service.GetPaymentRefundsService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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

@Path(PaymentRefundsResource.PAYMENT_REFUNDS_PATH)
@Tag(name = "Refunding card payments")
@Api(tags = "Refunding card payments", value = "/refunds")
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
    private PublicApiConfig configuration;
    private GetPaymentRefundsService getPaymentRefundsService;
    private GetPaymentRefundService getPaymentRefundService;

    @Inject
    public PaymentRefundsResource(Client client, PublicApiConfig configuration,
                                  GetPaymentRefundsService getPaymentRefundsService,
                                  GetPaymentRefundService getPaymentRefundService) {
        this.client = client;
        this.baseUrl = configuration.getBaseUrl();
        this.connectorUrl = configuration.getConnectorUrl();
        this.configuration = configuration;
        this.getPaymentRefundsService = getPaymentRefundsService;
        this.getPaymentRefundService = getPaymentRefundService;
    }

    @GET
    @Timed
    @Produces(APPLICATION_JSON)
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Get all refunds for a payment",
            summary = "Return refunds for a payment. " +
                    "The Authorisation token needs to be specified in the 'authorization' header as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = RefundForSearchResult.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credentials are required to access this resource"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found",
                            content = @Content(schema = @Schema(implementation = PaymentError.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = PaymentError.class)))
            }
    )
    @ApiOperation(
            response = RefundForSearchResult.class,
            nickname = "Get all refunds for a payment",
            value = "Get all refunds for a payment",
            notes = "Return refunds for a payment. " +
                    "The Authorisation token needs to be specified in the 'authorization' header as 'authorization: Bearer YOUR_API_KEY_HERE'",
            code = 200,
            authorizations = {@Authorization("Authorization")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = PaymentError.class),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public RefundsResponse getRefunds(@Parameter(hidden = true) @ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                      @PathParam(PATH_PAYMENT_KEY) String paymentId,
                                      @Parameter(hidden = true) @ApiParam(hidden = true) @HeaderParam("X-Ledger") String strategyName) {

        logger.info("Get refunds for payment request - paymentId={} using strategy={}", paymentId, strategyName);

        GetPaymentRefundsStrategy strategy = new GetPaymentRefundsStrategy(configuration, strategyName, account, paymentId, getPaymentRefundsService);
        RefundsResponse refundsResponse = strategy.validateAndExecute();

        logger.debug("refund returned - [ {} ]", refundsResponse);
        return refundsResponse;
    }

    @GET
    @Timed
    @Path("/{refundId}")
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Get a payment refund",
            summary = "Return payment refund information by Refund ID " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = RefundResult.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credentials are required to access this resource"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found",
                            content = @Content(schema = @Schema(implementation = PaymentError.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = PaymentError.class)))
            }
    )
    @Produces(APPLICATION_JSON)
    @ApiOperation(response = RefundResult.class,
            nickname = "Get a payment refund",
            value = "Find payment refund by ID",
            notes = "Return payment refund information by Refund ID " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            code = 200,
            authorizations = {@Authorization("Authorization")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = PaymentError.class),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public RefundResponse getRefundById(@Parameter(hidden = true) @ApiParam(value = "accountId", hidden = true) 
                                            @Auth Account account,
                                        @PathParam(PATH_PAYMENT_KEY) String paymentId,
                                        @PathParam(PATH_REFUND_KEY) String refundId,
                                        @Parameter(hidden = true) @ApiParam(hidden = true) @HeaderParam("X-Ledger") String strategyName) {

        logger.info("Payment refund request - paymentId={}, refundId={}", paymentId, refundId);

        var strategy = new GetPaymentRefundStrategy(configuration, strategyName, account, paymentId, refundId, getPaymentRefundService);
        RefundResponse refundResponse = strategy.validateAndExecute();

        logger.info("refund returned - [ {} ]", refundResponse);

        return refundResponse;
    }

    @POST
    @Timed
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Submit a refund for a payment",
            summary = "Return issued refund information. " +
                    "The Authorisation token needs to be specified in the 'authorization' header as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "ACCEPTED",
                            content = @Content(schema = @Schema(implementation = RefundResult.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credentials are required to access this resource"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found",
                            content = @Content(schema = @Schema(implementation = PaymentError.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "412", description = "Refund amount available mismatch"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = PaymentError.class)))
            }
    )
    @ApiOperation(
            response = RefundResult.class,
            nickname = "Submit a refund for a payment",
            value = "Submit a refund for a payment",
            notes = "Return issued refund information. " +
                    "The Authorisation token needs to be specified in the 'authorization' header as 'authorization: Bearer YOUR_API_KEY_HERE'",
            code = 202,
            authorizations = {@Authorization("Authorization")}
    )
    @ApiResponses(value = {
            @ApiResponse(code = 202, message = "ACCEPTED"),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = PaymentError.class),
            @ApiResponse(code = 412, message = "Refund amount available mismatch"),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response submitRefund(@Parameter(hidden = true) @ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                 @ApiParam(value = "paymentId", required = true) @PathParam(PATH_PAYMENT_KEY) String paymentId,
                                 @Parameter(required = true, description = "requestPayload")
                                 @ApiParam(value = "requestPayload", required = true) CreatePaymentRefundRequest requestPayload) {

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

            return Response.accepted(refundResponse).build();
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
