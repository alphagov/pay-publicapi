package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CreateRefundException;
import uk.gov.pay.api.model.CreatePaymentRefundRequest;
import uk.gov.pay.api.model.RequestError;
import uk.gov.pay.api.model.RefundFromConnector;
import uk.gov.pay.api.model.RefundResponse;
import uk.gov.pay.api.model.RefundSummary;
import uk.gov.pay.api.model.RefundsResponse;
import uk.gov.pay.api.model.CardPayment;
import uk.gov.pay.api.model.search.card.RefundForSearchResult;
import uk.gov.pay.api.model.search.card.RefundResult;
import uk.gov.pay.api.resources.error.ApiErrorResponse;
import uk.gov.pay.api.service.ConnectorService;
import uk.gov.pay.api.service.GetPaymentRefundService;
import uk.gov.pay.api.service.GetPaymentRefundsService;
import uk.gov.pay.api.service.GetPaymentService;

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
import java.util.Optional;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.UriBuilder.fromPath;

@Path("/v1/payments/{paymentId}/refunds")
@Tag(name = "Refunding card payments")
@Produces({"application/json"})
public class PaymentRefundsResource {
    private static final Logger logger = LoggerFactory.getLogger(PaymentRefundsResource.class);

    private final String baseUrl;
    private final Client client;
    private final String connectorUrl;
    private final GetPaymentRefundsService getPaymentRefundsService;
    private final GetPaymentRefundService getPaymentRefundService;
    private final ConnectorService connectorService;
    private final GetPaymentService getPaymentService;

    @Inject
    public PaymentRefundsResource(Client client, PublicApiConfig configuration,
                                  GetPaymentRefundsService getPaymentRefundsService,
                                  GetPaymentRefundService getPaymentRefundService, ConnectorService connectorService, GetPaymentService getPaymentService) {
        this.client = client;
        this.baseUrl = configuration.getBaseUrl();
        this.connectorUrl = configuration.getConnectorUrl();
        this.getPaymentRefundsService = getPaymentRefundsService;
        this.getPaymentRefundService = getPaymentRefundService;
        this.connectorService = connectorService;
        this.getPaymentService = getPaymentService;
    }

    @GET
    @Timed
    @Produces(APPLICATION_JSON)
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Get all refunds for a payment",
            summary = "Get all refunds for a payment",
            description = "Return refunds for a payment. " +
                    "The Authorisation token needs to be specified in the 'authorization' header as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = RefundForSearchResult.class))),
                    @ApiResponse(responseCode = "401", description = "Credentials are required to access this resource"),
                    @ApiResponse(responseCode = "404", description = "Not found",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public RefundsResponse getRefunds(@Parameter(hidden = true) @Auth Account account,
                                      @PathParam("paymentId") String paymentId,
                                      @Parameter(hidden = true) @HeaderParam("X-Ledger") String strategyName) {

        logger.info("Get refunds for payment request - paymentId={} using strategy={}", paymentId, strategyName);

        GetPaymentRefundsStrategy strategy = new GetPaymentRefundsStrategy(strategyName, account, paymentId, getPaymentRefundsService);
        RefundsResponse refundsResponse = strategy.validateAndExecute();

        logger.debug("refund returned - [ {} ]", refundsResponse);
        return refundsResponse;
    }

    @GET
    @Timed
    @Path("/{refundId}")
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Get a payment refund",
            summary = "Find payment refund by ID",
            description = "Return payment refund information by Refund ID " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = RefundResult.class))),
                    @ApiResponse(responseCode = "401", description = "Credentials are required to access this resource"),
                    @ApiResponse(responseCode = "404", description = "Not found",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    @Produces(APPLICATION_JSON)
    public RefundResponse getRefundById(@Parameter(hidden = true)
                                        @Auth Account account,
                                        @PathParam("paymentId") String paymentId,
                                        @PathParam("refundId") String refundId,
                                        @Parameter(hidden = true) @HeaderParam("X-Ledger") String strategyName) {

        logger.info("Payment refund request - paymentId={}, refundId={}", paymentId, refundId);

        var strategy = new GetPaymentRefundStrategy(strategyName, account, paymentId, refundId, getPaymentRefundService);
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
            summary = "Submit a refund for a payment",
            description = "Return issued refund information. " +
                    "The Authorisation token needs to be specified in the 'authorization' header as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @ApiResponse(responseCode = "200", description = "successful operation",
                            content = @Content(schema = @Schema(implementation = RefundResult.class))),
                    @ApiResponse(responseCode = "202", description = "ACCEPTED"),
                    @ApiResponse(responseCode = "401", description = "Credentials are required to access this resource"),
                    @ApiResponse(responseCode = "404", description = "Not found",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "412", description = "Refund amount available mismatch"),
                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public Response submitRefund(@Parameter(hidden = true) @Auth Account account,
                                 @PathParam("paymentId") String paymentId,
                                 @Parameter(required = true, description = "requestPayload")
                                 CreatePaymentRefundRequest requestPayload) {
        var strategy = new GetOnePaymentStrategy("", account, paymentId, getPaymentService);

        logger.info("Create a refund for payment request - paymentId={}", paymentId);

        Integer refundAmountAvailable = requestPayload.getRefundAmountAvailable()
                .orElseGet(() -> Optional.of((CardPayment) strategy.validateAndExecute().getPayment())
                        .map(p -> p.getRefundSummary()
                                .map(RefundSummary::getAmountAvailable)
                                .orElse(0L))
                        .map(Long::intValue)
                        .orElse(0));

        ImmutableMap<String, Object> payloadMap = ImmutableMap.of("amount", requestPayload.getAmount(), "refund_amount_available", refundAmountAvailable);
        String connectorPayload = new GsonBuilder().create().toJson(
                payloadMap);

        Response connectorResponse = client
                .target(getConnectorUrl(format("/v1/api/accounts/%s/charges/%s/refunds", account.getAccountId(), paymentId)))
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
        return fromPath(connectorUrl).path(urlPath).toString();
    }
}
