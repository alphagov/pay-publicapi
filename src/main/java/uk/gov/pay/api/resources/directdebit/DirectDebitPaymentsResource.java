package uk.gov.pay.api.resources.directdebit;

import com.codahale.metrics.annotation.Timed;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.CreateDirectDebitPaymentRequest;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.directdebit.mandates.DirectDebitPayment;
import uk.gov.pay.api.model.search.directdebit.DirectDebitSearchPaymentsParams;
import uk.gov.pay.api.model.search.directdebit.DirectDebitSearchResponse;
import uk.gov.pay.api.resources.error.ApiErrorResponse;
import uk.gov.pay.api.service.GetDirectDebitPaymentService;
import uk.gov.pay.api.service.PublicApiUriGenerator;
import uk.gov.pay.api.service.directdebit.CreateDirectDebitPaymentsService;
import uk.gov.pay.api.service.directdebit.DirectDebitPaymentSearchService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/v1/directdebit/payments/")
@Api(tags = "Direct Debit", value = "/v1/directdebit/payments")
@Tag(name = "Direct Debit")
@Produces({"application/json"})
public class DirectDebitPaymentsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectDebitPaymentsResource.class);

    private final CreateDirectDebitPaymentsService createDirectDebitPaymentsService;
    private final DirectDebitPaymentSearchService directDebitPaymentSearchService;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final GetDirectDebitPaymentService getDirectDebitPaymentService;


    @Inject
    public DirectDebitPaymentsResource(CreateDirectDebitPaymentsService createDirectDebitPaymentsService,
                                       DirectDebitPaymentSearchService directDebitPaymentSearchService,
                                       PublicApiUriGenerator publicApiUriGenerator, GetDirectDebitPaymentService getDirectDebitPaymentService) {
        this.createDirectDebitPaymentsService = createDirectDebitPaymentsService;
        this.directDebitPaymentSearchService = directDebitPaymentSearchService;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.getDirectDebitPaymentService = getDirectDebitPaymentService;
    }

    @POST
    @Timed
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Collect a Direct Debit payment",
            summary = "Create new Direct Debit payment",
            description = "Create a new Direct Debit payment for the account associated to the Authorisation token. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = DirectDebitPayment.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad request",
                            content = @Content(schema = @Schema(implementation = PaymentError.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", 
                            description = "Credentials are required to access this resource"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", 
                            description = "Invalid parameters: amount, reference, description, mandate id. See Public API documentation for the correct data formats",
                            content = @Content(schema = @Schema(implementation = PaymentError.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = PaymentError.class)))
            }
    )
    @ApiOperation(
            nickname = "Collect a Direct Debit payment",
            value = "Create new Direct Debit payment",
            notes = "Create a new Direct Debit payment for the account associated to the Authorisation token. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            code = 201,
            authorizations = {@Authorization("Authorization")})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = DirectDebitPayment.class),
            @ApiResponse(code = 400, message = "Bad request", response = PaymentError.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 422, message = "Invalid parameters: amount, reference, description, mandate id. See Public API documentation for the correct data formats", response = PaymentError.class),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response createPayment(@Parameter(hidden = true) @ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                  @Parameter(required = true, description = "requestPayload") @ApiParam(value = "requestPayload", required = true) 
                                  @Valid CreateDirectDebitPaymentRequest request) {
        DirectDebitPayment payment = createDirectDebitPaymentsService.create(account, request);
        return Response.created(publicApiUriGenerator.getDirectDebitPaymentURI(payment.getPaymentId()))
                .entity(payment).build();
    }


    @GET
    @Timed
    @Produces(APPLICATION_JSON)
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Search Direct Debit payments",
            summary = "Search Direct Debit payments",
            description = "Search Direct Debit payments by reference, state, mandate id, and 'from' and 'to' dates. " +
                    "The Authorisation token needs to be specified in the 'Authorization' header " +
                    "as 'Authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = DirectDebitSearchResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                            description = "Credentials are required to access this resource"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422",
                            description = "Invalid parameter. See Public API documentation for the correct data formats",
                            content = @Content(schema = @Schema(implementation = PaymentError.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = PaymentError.class)))
            }
    )
    @ApiOperation(
            nickname = "Search Direct Debit payments",
            value = "Search Direct Debit payments",
            notes = "Search Direct Debit payments by reference, state, mandate id, and 'from' and 'to' dates. " +
                    "The Authorisation token needs to be specified in the 'Authorization' header " +
                    "as 'Authorization: Bearer YOUR_API_KEY_HERE'",
            responseContainer = "List",
            code = 200,
            authorizations = {@Authorization("Authorization")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = DirectDebitSearchResponse.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 422, message = "Invalid parameter. See Public API documentation for the correct data formats", response = PaymentError.class),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response searchPayments(@Parameter(hidden = true) @ApiParam(value = "accountId", hidden = true)
                                   @Auth Account account,
                                   @Valid @BeanParam DirectDebitSearchPaymentsParams searchPaymentsParams,
                                   @Parameter(hidden = true) @Context UriInfo uriInfo) {

        LOGGER.info("Payments search request - {}", searchPaymentsParams);
        return directDebitPaymentSearchService.doSearch(account, searchPaymentsParams);
    }

    @GET
    @Timed
    @Path("{paymentId}")
    @Produces(APPLICATION_JSON)
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Get a Direct Debit payment",
            summary = "Find Direct Debit payment by ID",
            description = "Return information about the Direct Debit payment. " +
                    "The Authorisation token needs to be specified in the 'Authorization' header " +
                    "as 'Authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = DirectDebitPayment.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401",
                            description = "Credentials are required to access this resource"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Not found",
                            content = @Content(schema = @Schema(implementation = PaymentError.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = PaymentError.class)))
            }
    )
    @ApiOperation(
            nickname = "Get a Direct Debit payment",
            value = "Find Direct Debit payment by ID",
            notes = "Return information about the Direct Debit payment. " +
                    "The Authorisation token needs to be specified in the 'Authorization' header " +
                    "as 'Authorization: Bearer YOUR_API_KEY_HERE'",
            code = 200,
            authorizations = {@Authorization("Authorization")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = DirectDebitPayment.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = PaymentError.class),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response getPayment(@Parameter(hidden = true) @ApiParam(value = "accountId", hidden = true) @Auth Account account,
                               @PathParam("paymentId") @Parameter(required = true, description = "Payment identifier") 
                               @ApiParam(value = "Payment identifier") String paymentId) {

        LOGGER.info("Direct Debit Payment request - paymentId={}", paymentId);
        DirectDebitPayment payment = getDirectDebitPaymentService.getDirectDebitPayment(account, paymentId);
        return Response.ok(payment).build();
    }
}
