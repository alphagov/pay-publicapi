package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CaptureChargeException;
import uk.gov.pay.api.model.CreateCardPaymentRequest;
import uk.gov.pay.api.model.CreatePaymentResult;
import uk.gov.pay.api.model.RequestError;
import uk.gov.pay.api.model.PaymentEventsResponse;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;
import uk.gov.pay.api.model.search.card.GetPaymentResult;
import uk.gov.pay.api.model.search.card.PaymentSearchResults;
import uk.gov.pay.api.resources.error.ApiErrorResponse;
import uk.gov.pay.api.service.CancelPaymentService;
import uk.gov.pay.api.service.CapturePaymentService;
import uk.gov.pay.api.service.CreatePaymentService;
import uk.gov.pay.api.service.GetPaymentEventsService;
import uk.gov.pay.api.service.GetPaymentService;
import uk.gov.pay.api.service.PaymentSearchParams;
import uk.gov.pay.api.service.PaymentSearchService;
import uk.gov.pay.api.service.PublicApiUriGenerator;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Tag(name = "Card payments")
@Produces({"application/json"})
public class PaymentsResource {

    private static final Logger logger = LoggerFactory.getLogger(PaymentsResource.class);

    private final CreatePaymentService createPaymentService;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final PaymentSearchService paymentSearchService;
    private final GetPaymentService getPaymentService;
    private final CapturePaymentService capturePaymentService;
    private final CancelPaymentService cancelPaymentService;
    private final GetPaymentEventsService getPaymentEventsService;

    @Inject
    public PaymentsResource(CreatePaymentService createPaymentService,
                            PaymentSearchService paymentSearchService,
                            PublicApiUriGenerator publicApiUriGenerator,
                            GetPaymentService getPaymentService,
                            CapturePaymentService capturePaymentService,
                            CancelPaymentService cancelPaymentService,
                            GetPaymentEventsService getPaymentEventsService) {
        this.createPaymentService = createPaymentService;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.paymentSearchService = paymentSearchService;
        this.getPaymentService = getPaymentService;
        this.capturePaymentService = capturePaymentService;
        this.cancelPaymentService = cancelPaymentService;
        this.getPaymentEventsService = getPaymentEventsService;
    }

    @GET
    @Timed
    @Path("/v1/payments/{paymentId}")
    @Produces(APPLICATION_JSON)
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Get a payment",
            summary = "Find payment by ID",
            description = "Return information about the payment " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = GetPaymentResult.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Credentials are required to access this resource"),
                    @ApiResponse(responseCode = "404", description = "Not found",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public Response getPayment(@Parameter(hidden = true) @Auth Account account,
                               @PathParam("paymentId")
                               @Parameter(name = "paymentId", description = "Payment identifier", example = "hu20sqlact5260q2nanm0q8u93")
                                       String paymentId,
                               @Parameter(hidden = true) @HeaderParam("X-Ledger") String strategyName) {

        logger.info("Payment request - paymentId={}", paymentId);

        var strategy = new GetOnePaymentStrategy(strategyName, account, paymentId, getPaymentService);
        PaymentWithAllLinks payment = strategy.validateAndExecute();

        logger.info("Payment returned - [ {} ]", payment);
        return Response.ok(payment).build();
    }

    @GET
    @Timed
    @Path("/v1/payments/{paymentId}/events")
    @Produces(APPLICATION_JSON)
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Get events for a payment",
            summary = "Return payment events by ID",
            description = "Return payment events information about a certain payment " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = PaymentEventsResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Credentials are required to access this resource"),
                    @ApiResponse(responseCode = "404", description = "Not found",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public PaymentEventsResponse getPaymentEvents(@Parameter(hidden = true) @Auth Account account,
                                                  @PathParam("paymentId")
                                                  @Parameter(name = "paymentId", description = "Payment identifier", example = "hu20sqlact5260q2nanm0q8u93")
                                                          String paymentId,
                                                  @Parameter(hidden = true) @HeaderParam("X-Ledger") String strategyName) {

        logger.info("Payment events request - payment_id={}", paymentId);

        var strategy = new GetPaymentEventsStrategy(strategyName, account, paymentId, getPaymentEventsService);
        PaymentEventsResponse paymentEventsResponse = strategy.validateAndExecute();

        logger.info("Payment events returned - [ {} ]", paymentEventsResponse);

        return paymentEventsResponse;
    }

    @GET
    @Timed
    @Path("/v1/payments")
    @Produces(APPLICATION_JSON)
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Search payments",
            summary = "Search payments",
            description = "Search payments by reference, state, 'from' and 'to' date. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = PaymentSearchResults.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Credentials are required to access this resource"),
                    @ApiResponse(responseCode = "422",
                            description = "Invalid parameters: from_date, to_date, status, display_size. See Public API documentation for the correct data formats",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public Response searchPayments(@Parameter(hidden = true)
                                   @Auth Account account,
                                   @Parameter(description = "Your payment reference to search (exact match, case insensitive)")
                                   @QueryParam("reference") String reference,
                                   @Parameter(description = "The user email used in the payment to be searched")
                                   @QueryParam("email") String email,
                                   @Parameter(description = "State of payments to be searched. Example=success", example = "success",
                                           schema = @Schema(allowableValues = {"created", "started", "submitted", "success", "failed", "cancelled", "error"}))
                                   @QueryParam("state") String state,
                                   @Parameter(description = "Card brand used for payment. Example=master-card")
                                   @QueryParam("card_brand") String cardBrand,
                                   @Parameter(description = "From date of payments to be searched (this date is inclusive). Example=2015-08-13T12:35:00Z")
                                   @QueryParam("from_date") String fromDate,
                                   @Parameter(description = "To date of payments to be searched (this date is exclusive). Example=2015-08-14T12:35:00Z")
                                   @QueryParam("to_date") String toDate,
                                   @Parameter(description = "Page number requested for the search, should be a positive integer (optional, defaults to 1)")
                                   @QueryParam("page") String pageNumber,
                                   @Parameter(description = "Number of results to be shown per page, should be a positive integer (optional, defaults to 500, max 500)")
                                   @QueryParam("display_size") String displaySize,
                                   @Parameter(description = "Name on card used to make payment")
                                   @QueryParam("cardholder_name") String cardHolderName,
                                   @Parameter(description = "First six digits of the card used to make payment")
                                   @QueryParam("first_digits_card_number") String firstDigitsCardNumber,
                                   @Parameter(description = "Last four digits of the card used to make payment", hidden = false)
                                   @QueryParam("last_digits_card_number") String lastDigitsCardNumber,
                                   @Parameter(description = "From settled date of payment to be searched (this date is inclusive). Example=2015-08-13")
                                   @QueryParam("from_settled_date") String fromSettledDate,
                                   @Parameter(description = "To settled date of payment to be searched (this date is inclusive). Example=2015-08-14")
                                   @QueryParam("to_settled_date") String toSettledDate,
                                   @Context UriInfo uriInfo) {

        logger.info("Payments search request - [ {} ]",
                format("reference:%s, email: %s, status: %s, card_brand %s, fromDate: %s, toDate: %s, page: %s, " +
                                "display_size: %s, cardholder_name: %s, first_digits_card_number: %s, " +
                                "last_digits_card_number: %s, from_settled_date: %s, to_settled_date: %s",
                        reference, email, state, cardBrand, fromDate, toDate, pageNumber, displaySize,
                        cardHolderName, firstDigitsCardNumber, lastDigitsCardNumber, fromSettledDate, toSettledDate));

        var paymentSearchParams = new PaymentSearchParams.Builder()
                .withReference(reference)
                .withEmail(email)
                .withState(state)
                .withCardBrand(cardBrand)
                .withFromDate(fromDate)
                .withToDate(toDate)
                .withPageNumber(pageNumber)
                .withDisplaySize(displaySize)
                .withCardHolderName(cardHolderName)
                .withFirstDigitsCardNumber(firstDigitsCardNumber)
                .withLastDigitsCardNumber(lastDigitsCardNumber)
                .withFromSettledDate(fromSettledDate)
                .withToSettledDate(toSettledDate)
                .build();

        return paymentSearchService.searchLedgerPayments(account, paymentSearchParams);
    }

    @POST
    @Timed
    @Path("/v1/payments")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Create a payment",
            summary = "Create new payment",
            description = "Create a new payment for the account associated to the Authorisation token. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = CreatePaymentResult.class))),
                    @ApiResponse(responseCode = "400", description = "Bad request",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Credentials are required to access this resource"),
                    @ApiResponse(responseCode = "422",
                            description = "Invalid attribute value: description. Must be less than or equal to 255 characters length",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public Response createNewPayment(@Parameter(hidden = true) @Auth Account account,
                                     @Parameter(required = true, description = "requestPayload")
                                     @Valid CreateCardPaymentRequest createCardPaymentRequest) {
        logger.info("Payment create request parsed to {}", createCardPaymentRequest);

        PaymentWithAllLinks createdPayment = createPaymentService.create(account, createCardPaymentRequest);

        Response response = Response
                .created(publicApiUriGenerator.getPaymentURI(createdPayment.getPayment().getPaymentId()))
                .entity(createdPayment)
                .build();

        logger.info("Payment returned (created): [ {} ]", createdPayment);
        return response;
    }

    @POST
    @Timed
    @Path("/v1/payments/{paymentId}/cancel")
    @Produces(APPLICATION_JSON)
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Cancel a payment",
            summary = "Cancel payment",
            description = "Cancel a payment based on the provided payment ID and the Authorisation token. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'. A payment can only be cancelled if it's in " +
                    "a state that isn't finished.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content"),
                    @ApiResponse(responseCode = "400", description = "Cancellation of payment failed",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Credentials are required to access this resource"),
                    @ApiResponse(responseCode = "404", description = "Not found",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "409", description = "Conflict",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public Response cancelPayment(@Parameter(hidden = true) @Auth Account account,
                                  @PathParam("paymentId")
                                  @Parameter(name = "paymentId", description = "Payment identifier", example = "hu20sqlact5260q2nanm0q8u93")
                                          String paymentId) {

        logger.info("Payment cancel request - payment_id=[{}]", paymentId);

        return cancelPaymentService.cancel(account, paymentId);
    }

    @POST
    @Timed
    @Path("/v1/payments/{paymentId}/capture")
    @Produces(APPLICATION_JSON)
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Capture a payment",
            summary = "Capture payment",
            description = "Capture a payment based on the provided payment ID and the Authorisation token. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'. A payment can only be captured if it's in " +
                    "'submitted' state",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content"),
                    @ApiResponse(responseCode = "400", description = "Capture of payment failed",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Credentials are required to access this resource"),
                    @ApiResponse(responseCode = "404", description = "Not found",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "409", description = "Conflict",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public Response capturePayment(@Parameter(hidden = true) @Auth Account account,
                                   @PathParam("paymentId")
                                   @Parameter(name = "paymentId", description = "Payment identifier", example = "hu20sqlact5260q2nanm0q8u93")
                                           String paymentId) {
        logger.info("Payment capture request - payment_id=[{}]", paymentId);

        Response connectorResponse = capturePaymentService.capture(account, paymentId);

        if (connectorResponse.getStatus() == HttpStatus.SC_NO_CONTENT) {
            connectorResponse.close();
            return Response.noContent().build();
        }

        throw new CaptureChargeException(connectorResponse);
    }
}
