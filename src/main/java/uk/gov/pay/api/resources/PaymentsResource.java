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
import static org.apache.http.HttpHeaders.CACHE_CONTROL;
import static org.apache.http.HttpHeaders.PRAGMA;

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
        return Response.ok(payment)
                .header(PRAGMA, "no-cache")
                .header(CACHE_CONTROL, "no-store")
                .build();
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
            description = "You can use this endpoint to search for payments you’ve previously created. " +
                    "Payments are sorted by date, with the most recently-created payment appearing first.<br><br>" +
                    "You can see a full reference for this endpoint in " +
                    "[our documentation](https://docs.payments.service.gov.uk/api_reference/search_payments_reference)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK - your request was successful",
                            content = @Content(schema = @Schema(implementation = PaymentSearchResults.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Unauthorised: Your API key is missing or invalid.<br><br><a href=\"/api_reference/#authentication\">" +
                                    "Read more about authenticating GOV.UK Pay API requests</a>."),
                    @ApiResponse(responseCode = "422",
                            description = "Unprocessable entity: One of the values you sent is formatted incorrectly. " +
                                    "This could be an invalid value, or a value that exceeds a character limit." +
                                    "<br><br>Check the <code>field</code>, <code>code</code>, and <code>description</code> " +
                                    "attributes in the response for more information.",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = "Too many requests: You&rsquo;ve made too many requests using your API key.<br><br>" +
                                    "<a href=\"/api_reference#rate-limits\">Read more about rate limits</a>.",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal server error: There&rsquo;s something wrong with GOV.UK Pay. <br><br>" +
                                    "If there are no issues on <a href=\"https://payments.statuspage.io\">our status page</a>, " +
                                    "you can <a href=\"/support_contact_and_more_information/\">contact us with your error code</a> and we&rsquo;ll investigate.",
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public Response searchPayments(@Parameter(hidden = true)
                                   @Auth Account account,
                                   @Parameter(description = "Returns payments with <code>reference</code> values exactly matching your specified value.")
                                   @QueryParam("reference") String reference,
                                   @Parameter(description = "\n" +
                                           "Returns payments with matching `email` values. You can send full or partial email addresses.<br><br>" +
                                           "`email` is the paying user’s email address.\n")
                                   @QueryParam("email") String email,
                                   @Parameter(description = "Returns payments in a matching `state`.<br><br>" +
                                           "`state` reflects where a payment is in the [payment status lifecycle](https://docs.payments.service.gov.uk/api_reference/#payment-status-lifecycle).",
                                           example = "success", schema = @Schema(allowableValues = {"created", "started", "submitted", "success", "failed", "cancelled", "error"}))
                                   @QueryParam("state") String state,
                                   @Parameter(description = "Returns payments paid with a particular card brand.")
                                   @QueryParam("card_brand") String cardBrand,
                                   @Parameter(description = "Returns payments created on or after the `from_date`.<br><br>" +
                                           "Date and time must be coordinated Universal Time (UTC) and ISO 8601 format to second-level accuracy - `YYYY-MM-DDTHH:MM:SSZ`.")
                                   @QueryParam("from_date") String fromDate,
                                   @Parameter(description = "Returns payments created before the `to_date`.<br><br>" +
                                           "Date and time must be coordinated Universal Time (UTC) and ISO 8601 format to second-level accuracy - " +
                                           "`YYYY-MM-DDTHH:MM:SSZ`.")
                                   @QueryParam("to_date") String toDate,
                                   @Parameter(description = "Returns a specific page of results.<br><br>" +
                                           "Defaults to `1`.<br><br>You can [read more about search pagination](/api_reference/#pagination).")
                                   @QueryParam("page") String pageNumber,
                                   @Parameter(description = "The number of payments returned per results page. <br><br>" +
                                           "Defaults to `500`. Maximum value is `500`.<br><br>You can [read more about search pagination](/api_reference/#pagination).")
                                   @QueryParam("display_size") String displaySize,
                                   @Parameter(description = "Returns payments paid with cards under this cardholder name.")
                                   @QueryParam("cardholder_name") String cardHolderName,
                                   @Parameter(description = "Returns payments paid by cards beginning with the `first_digits_card_number` value. <br><br>" +
                                           "`first_digits_card_number` value must be 6 digits.")
                                   @QueryParam("first_digits_card_number") String firstDigitsCardNumber,
                                   @Parameter(description = "Returns payments paid by cards ending with the `last_digits_card_number` value.<br><br>" +
                                           "`last_digits_card_number` value must be 4 digits.", hidden = false)
                                   @QueryParam("last_digits_card_number") String lastDigitsCardNumber,
                                   @Parameter(description = "Returns payments settled on or after the `from_settled_date` value.<br><br>" +
                                           "You can only search by settled date if your payment service provider is Stripe.<br><br>" +
                                           "Date must be in ISO 8601 format to date-level accuracy - `YYYY-MM-DD`.<br><br>" +
                                           "Payments are settled when your payment service provider sends funds to your bank account.")
                                   @QueryParam("from_settled_date") String fromSettledDate,
                                   @Parameter(description = "Returns payments settled before the `to_settled_date` value.<br><br>" +
                                           "You can only search by settled date if your payment service provider is Stripe. <br><br>" +
                                           "Date must be in ISO 8601 format to date-level accuracy - `YYYY-MM-DD`.<br><br>" +
                                           "Payments are settled when your payment service provider sends funds to your bank account.")
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
                .header(PRAGMA, "no-cache")
                .header(CACHE_CONTROL, "no-store")
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
