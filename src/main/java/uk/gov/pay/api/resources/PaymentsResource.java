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
import jakarta.annotation.Nullable;
import org.apache.http.HttpStatus;
import org.hibernate.validator.constraints.Length;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CaptureChargeException;
import uk.gov.pay.api.model.CreateCardPaymentRequest;
import uk.gov.pay.api.model.CreatePaymentResult;
import uk.gov.pay.api.model.CreatedPaymentWithAllLinks;
import uk.gov.pay.api.model.PaymentEventsResponse;
import uk.gov.pay.api.model.RequestError;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;
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

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import static java.lang.String.format;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpHeaders.CACHE_CONTROL;
import static org.apache.http.HttpHeaders.PRAGMA;
import static uk.gov.pay.api.common.ResponseConstants.RESPONSE_200_DESCRIPTION;
import static uk.gov.pay.api.common.ResponseConstants.RESPONSE_400_DESCRIPTION;
import static uk.gov.pay.api.common.ResponseConstants.RESPONSE_401_DESCRIPTION;
import static uk.gov.pay.api.common.ResponseConstants.RESPONSE_404_DESCRIPTION;
import static uk.gov.pay.api.common.ResponseConstants.RESPONSE_409_DESCRIPTION;
import static uk.gov.pay.api.common.ResponseConstants.RESPONSE_422_DESCRIPTION;
import static uk.gov.pay.api.common.ResponseConstants.RESPONSE_429_DESCRIPTION;
import static uk.gov.pay.api.common.ResponseConstants.RESPONSE_500_DESCRIPTION;

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
            summary = "Get information about a single payment",
            description = "You can use this endpoint to [get details about a single payment you’ve previously created]" +
                    "(https://docs.payments.service.gov.uk/reporting/#get-information-about-a-single-payment).",
            responses = {
                    @ApiResponse(responseCode = "200", description = RESPONSE_200_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = PaymentWithAllLinks.class))),
                    @ApiResponse(responseCode = "401",
                            description = RESPONSE_401_DESCRIPTION),
                    @ApiResponse(responseCode = "404", description = RESPONSE_404_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = RESPONSE_429_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = RESPONSE_500_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public Response getPayment(@Parameter(hidden = true) @Auth Account account,
                               @PathParam("paymentId")
                               @Parameter(name = "paymentId", description = "Returns the payment with the matching `payment_id`.", example = "hu20sqlact5260q2nanm0q8u93")
                               String paymentId,
                               @Parameter(hidden = true) @HeaderParam("X-Ledger") String strategyName) {

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
            summary = "Get a payment's events",
            description = "You can use this endpoint to " +
                    "[get a list of a payment’s events](https://docs.payments.service.gov.uk/reporting/#get-a-payment-s-events). " +
                    "A payment event is when a payment’s `state` changes, such as when the payment is created, or when the paying user submits their details.",
            responses = {
                    @ApiResponse(responseCode = "200", description = RESPONSE_200_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = PaymentEventsResponse.class))),
                    @ApiResponse(responseCode = "401",
                            description = RESPONSE_401_DESCRIPTION),
                    @ApiResponse(responseCode = "404", description = RESPONSE_404_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = RESPONSE_429_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = RESPONSE_500_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public PaymentEventsResponse getPaymentEvents(@Parameter(hidden = true) @Auth Account account,
                                                  @PathParam("paymentId")
                                                  @Parameter(name = "paymentId", description = "Payment identifier", example = "hu20sqlact5260q2nanm0q8u93")
                                                  String paymentId,
                                                  @Parameter(hidden = true) @HeaderParam("X-Ledger") String strategyName) {

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
            description = "You can use this endpoint to [search for payments you’ve previously created](https://docs.payments.service.gov.uk/reporting/#search-payments/). " +
                    "Payments are sorted by date, with the most recently-created payment appearing first.",
            responses = {
                    @ApiResponse(responseCode = "200", description = RESPONSE_200_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = PaymentSearchResults.class))),
                    @ApiResponse(responseCode = "401",
                            description = RESPONSE_401_DESCRIPTION),
                    @ApiResponse(responseCode = "422",
                            description = "Invalid parameters: from_date, to_date, status, display_size. See Public API documentation for the correct data formats",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = RESPONSE_429_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = RESPONSE_500_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public Response searchPayments(@Parameter(hidden = true)
                                   @Auth Account account,
                                   @Parameter(description = "Returns payments with `reference` values exactly matching your specified value.")
                                   @QueryParam("reference") String reference,
                                   @Parameter(description = "Returns payments with matching `email` values. You can send full or partial email addresses. " +
                                           "`email` is the paying user’s email address.")
                                   @QueryParam("email") String email,
                                   @Parameter(description = "Returns payments in a matching `state`. `state` reflects where a payment is in the " +
                                           "[payment status lifecycle](https://docs.payments.service.gov.uk/api_reference/#payment-status-lifecycle).", example = "success",
                                           schema = @Schema(allowableValues = {"created", "started", "submitted", "success", "failed", "cancelled", "error"}))
                                   @QueryParam("state") String state,
                                   @Parameter(description = "Returns payments paid with a particular card brand.")
                                   @QueryParam("card_brand") String cardBrand,
                                   @Parameter(description = "Returns payments created on or after the `from_date`. " +
                                           "Date and time must be coordinated Universal Time (UTC) and ISO 8601 format to second-level accuracy - `YYYY-MM-DDThh:mm:ssZ`.", example = "2015-08-13T12:35:00Z")
                                   @QueryParam("from_date") String fromDate,
                                   @Parameter(description = "Returns payments created before the `to_date`. " +
                                           "Date and time must be coordinated Universal Time (UTC) and ISO 8601 format to second-level accuracy - `YYYY-MM-DDThh:mm:ssZ`.", example = "2015-08-13T12:35:00Z")
                                   @QueryParam("to_date") String toDate,
                                   @Parameter(description = "Returns a [specific page of results](https://docs.payments.service.gov.uk/api_reference/#pagination). Defaults to `1`.")
                                   @QueryParam("page") String pageNumber,
                                   @Parameter(description = "The number of payments returned " +
                                           "[per results page](https://docs.payments.service.gov.uk/api_reference/#pagination). Defaults to `500`. Maximum value is `500`.")
                                   @QueryParam("display_size") String displaySize,
                                   @Parameter(description = "Returns payments paid with cards under this cardholder name.")
                                   @QueryParam("cardholder_name") String cardHolderName,
                                   @Parameter(description = "Returns payments paid by cards beginning with the `first_digits_card_number` value. "
                                           + "`first_digits_card_number` value must be 6 digits.")
                                   @QueryParam("first_digits_card_number") String firstDigitsCardNumber,
                                   @Parameter(description = "Returns payments paid by cards ending with the `last_digits_card_number` value. " +
                                           "`last_digits_card_number` value must be 4 digits.")
                                   @QueryParam("last_digits_card_number") String lastDigitsCardNumber,
                                   @Parameter(description = "Returns payments settled on or after the `from_settled_date` value. " +
                                           "You can only search by settled date if your payment service provider is Stripe. " +
                                           "Date must be in ISO 8601 format to date-level accuracy - `YYYY-MM-DD`. " +
                                           "Payments are settled when your payment service provider sends funds to your bank account.")
                                   @QueryParam("from_settled_date") String fromSettledDate,
                                   @Parameter(description = "Returns payments settled before the `to_settled_date` value. " +
                                           "You can only search by settled date if your payment service provider is Stripe. " +
                                           "Date must be in ISO 8601 format to date-level accuracy - `YYYY-MM-DD`. " +
                                           "Payments are settled when your payment service provider sends funds to your bank account.")
                                   @QueryParam("to_settled_date") String toSettledDate,
                                   @Parameter(description = "Returns payments that were authorised using the agreement with this `agreement_id`. " +
                                           "Must be an exact match.", example = "abcefghjklmnopqr1234567890")
                                   @QueryParam("agreement_id") String agreementId,
                                   @Context UriInfo uriInfo) {

        logger.info("Payments search request - [ reference: {}, email: REDACTED, status: {}, card_brand {}, fromDate: {}, toDate: {}, page: {}, display_size: {}, cardholder_name: REDACTED, first_digits_card_number: {}, last_digits_card_number: {}, from_settled_date: {}, to_settled_date: {}, agreement_id: {} ]",
                reference, state, cardBrand, fromDate, toDate, pageNumber, displaySize, firstDigitsCardNumber, lastDigitsCardNumber, fromSettledDate, toSettledDate, agreementId);

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
                .withAgreementId(agreementId)
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
            summary = "Create a payment",
            description = "You can use this endpoint to [create a new payment](https://docs.payments.service.gov.uk/making_payments/).",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = CreatePaymentResult.class))),
                    @ApiResponse(responseCode = "400", description = RESPONSE_400_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "401",
                            description = RESPONSE_401_DESCRIPTION),
                    @ApiResponse(responseCode = "422",
                            description = RESPONSE_422_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = RESPONSE_429_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = RESPONSE_500_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public Response createNewPayment(@Parameter(hidden = true) @Auth Account account,
                                     @Parameter(required = true, description = "requestPayload")
                                     @Valid CreateCardPaymentRequest createCardPaymentRequest,
                                     @Nullable
                                     @Length(min = 1, max = 255, message = "Header [Idempotency-Key] can have a size between 1 and 255")
                                     @Pattern(regexp = "^$|^[a-zA-Z0-9-]+$", message = "Header [Idempotency-Key] can only contain alphanumeric characters and hyphens")
                                     @HeaderParam("Idempotency-Key")
                                     String idempotencyKey) {
        logger.info("Payment create request parsed to {}", createCardPaymentRequest);

        CreatedPaymentWithAllLinks createdPayment = createPaymentService.create(account, createCardPaymentRequest, idempotencyKey);

        PaymentWithAllLinks paymentWithAllLinks = createdPayment.getPayment();
        Response.ResponseBuilder response;

        switch (createdPayment.getWhenCreated()) {
            case BRAND_NEW -> response = Response
                    .created(publicApiUriGenerator.getPaymentURI(paymentWithAllLinks.getPaymentId()));
            case EXISTING -> response = Response.ok();
            default ->
                    throw new IllegalArgumentException(format("Unrecognised WhenCreated enum: %s", createdPayment.getWhenCreated()));
        }

        response.entity(paymentWithAllLinks)
                .header(PRAGMA, "no-cache")
                .header(CACHE_CONTROL, "no-store");

        logger.info("Payment returned (created): [ {} ]", paymentWithAllLinks);
        return response.build();
    }

    @POST
    @Timed
    @Path("/v1/payments/{paymentId}/cancel")
    @Produces(APPLICATION_JSON)
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Cancel a payment",
            summary = "Cancel payment",
            description = "You can use this endpoint [to cancel an unfinished payment]" +
                    "(https://docs.payments.service.gov.uk/making_payments/#cancel-a-payment-that-s-in-progress).",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content"),
                    @ApiResponse(responseCode = "400", description = "Cancellation of payment failed",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "401",
                            description = RESPONSE_401_DESCRIPTION),
                    @ApiResponse(responseCode = "404", description = RESPONSE_404_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "409", description = RESPONSE_409_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = RESPONSE_429_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = RESPONSE_500_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public Response cancelPayment(@Parameter(hidden = true) @Auth Account account,
                                  @PathParam("paymentId")
                                  @Parameter(name = "paymentId", description = "The `payment_id` of the payment you’re cancelling.", example = "hu20sqlact5260q2nanm0q8u93")
                                  String paymentId) {

        return cancelPaymentService.cancel(account, paymentId);
    }

    @POST
    @Timed
    @Path("/v1/payments/{paymentId}/capture")
    @Produces(APPLICATION_JSON)
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Capture a payment",
            summary = "Take a delayed payment",
            description = "You can use this endpoint to [take (‘capture’) a delayed payment from the paying user’s bank account]" +
                    "(https://docs.payments.service.gov.uk/delayed_capture/).",
            responses = {
                    @ApiResponse(responseCode = "204", description = "No Content"),
                    @ApiResponse(responseCode = "400", description = "Capture of payment failed",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "401",
                            description = RESPONSE_401_DESCRIPTION),
                    @ApiResponse(responseCode = "404", description = RESPONSE_404_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "409", description = RESPONSE_409_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = RESPONSE_429_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = RESPONSE_500_DESCRIPTION,
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public Response capturePayment(@Parameter(hidden = true) @Auth Account account,
                                   @PathParam("paymentId")
                                   @Parameter(name = "paymentId", description = "The `payment_id` of the payment you’re capturing.", example = "hu20sqlact5260q2nanm0q8u93")
                                   String paymentId) {
        Response connectorResponse = capturePaymentService.capture(account, paymentId);

        if (connectorResponse.getStatus() == HttpStatus.SC_NO_CONTENT) {
            connectorResponse.close();
            return Response.noContent().build();
        }
        throw new CaptureChargeException(connectorResponse);
    }
}
