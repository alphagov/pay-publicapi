package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.CaptureChargeException;
import uk.gov.pay.api.exception.GetEventsException;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.PaymentEvents;
import uk.gov.pay.api.model.ValidCreatePaymentRequest;
import uk.gov.pay.api.model.links.PaymentWithAllLinks;
import uk.gov.pay.api.model.search.card.PaymentSearchResults;
import uk.gov.pay.api.resources.error.ApiErrorResponse;
import uk.gov.pay.api.service.CancelPaymentService;
import uk.gov.pay.api.service.CapturePaymentService;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.CreatePaymentService;
import uk.gov.pay.api.service.GetPaymentService;
import uk.gov.pay.api.service.PaymentSearchService;
import uk.gov.pay.api.service.PublicApiUriGenerator;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_OK;

@Path("/")
@Api(value = "/", description = "Public Api Endpoints")
@Produces({"application/json"})
public class PaymentsResource {

    private static final Logger logger = LoggerFactory.getLogger(PaymentsResource.class);

    private final Client client;
    private final CreatePaymentService createPaymentService;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final ConnectorUriGenerator connectorUriGenerator;
    private final PaymentSearchService paymentSearchService;
    private final GetPaymentService getPaymentService;
    private final CapturePaymentService capturePaymentService;
    private final CancelPaymentService cancelPaymentService;

    @Inject
    public PaymentsResource(Client client,
                            CreatePaymentService createPaymentService,
                            PaymentSearchService paymentSearchService,
                            PublicApiUriGenerator publicApiUriGenerator,
                            ConnectorUriGenerator connectorUriGenerator,
                            GetPaymentService getPaymentService,
                            CapturePaymentService capturePaymentService,
                            CancelPaymentService cancelPaymentService) {
        this.client = client;
        this.createPaymentService = createPaymentService;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.connectorUriGenerator = connectorUriGenerator;
        this.paymentSearchService = paymentSearchService;
        this.getPaymentService = getPaymentService;
        this.capturePaymentService = capturePaymentService;
        this.cancelPaymentService = cancelPaymentService;
    }

    @GET
    @Timed
    @Path("/v1/payments/{paymentId}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Find payment by ID",
            notes = "Return information about the payment " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            code = 200)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PaymentWithAllLinks.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = PaymentError.class),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response getPayment(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                               @PathParam("paymentId") String paymentId) {

        logger.info("Payment request - paymentId={}", paymentId);

        PaymentWithAllLinks payment = getPaymentService.getPayment(account, paymentId);

        logger.info("Payment returned - [ {} ]", payment);
        return Response.ok(payment).build();

    }

    @GET
    @Timed
    @Path("/v1/payments/{paymentId}/events")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Return payment events by ID",
            notes = "Return payment events information about a certain payment " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            code = 200)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PaymentEvents.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = PaymentError.class),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response getPaymentEvents(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                     @PathParam("paymentId") String paymentId) {

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

    @GET
    @Timed
    @Path("/v1/payments")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Search payments",
            notes = "Search payments by reference, state, 'from' and 'to' date. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responseContainer = "List",
            code = 200)

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = PaymentSearchResults.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 422, message = "Invalid parameters: from_date, to_date, status, display_size. See Public API documentation for the correct data formats", response = PaymentError.class),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response searchPayments(@ApiParam(value = "accountId", hidden = true)
                                   @Auth Account account,
                                   @ApiParam(value = "Your payment reference to search", hidden = false)
                                   @QueryParam("reference") String reference,
                                   @ApiParam(value = "The user email used in the payment to be searched", hidden = false)
                                   @QueryParam("email") String email,
                                   @ApiParam(value = "State of payments to be searched. Example=success", hidden = false, allowableValues = "range[created,started,submitted,success,failed,cancelled,error")
                                   @QueryParam("state") String state,
                                   @ApiParam(value = "Card brand used for payment. Example=master-card", hidden = false)
                                   @QueryParam("card_brand") String cardBrand,
                                   @ApiParam(value = "From date of payments to be searched (this date is inclusive). Example=2015-08-13T12:35:00Z", hidden = false)
                                   @QueryParam("from_date") String fromDate,
                                   @ApiParam(value = "To date of payments to be searched (this date is exclusive). Example=2015-08-14T12:35:00Z", hidden = false)
                                   @QueryParam("to_date") String toDate,
                                   @ApiParam(value = "Page number requested for the search, should be a positive integer (optional, defaults to 1)", hidden = false)
                                   @QueryParam("page") String pageNumber,
                                   @ApiParam(value = "Number of results to be shown per page, should be a positive integer (optional, defaults to 500, max 500)", hidden = false)
                                   @QueryParam("display_size") String displaySize,
                                   @ApiParam(value = "Direct Debit Agreement Id", hidden = true)
                                   @QueryParam("agreement_id") String agreementId,
                                   @ApiParam(value = "Name on card used to make payment", hidden = false)
                                       @QueryParam("cardholder_name") String cardHolderName,
                                   @ApiParam(value = "First six digits of the card used to make payment", hidden = false)

                                       @QueryParam("first_digits_card_number") String firstDigitsCardNumber,
                                   @ApiParam(value = "Last four digits of the card used to make payment", hidden = false)

                                       @QueryParam("last_digits_card_number") String lastDigitsCardNumber,
                                   @Context UriInfo uriInfo) {

        logger.info("Payments search request - [ {} ]",
                format("reference:%s, email: %s, status: %s, card_brand %s, fromDate: %s, toDate: %s, page: %s, display_size: %s, agreement_id: %s, cardholder_name: %s, first_digits_card_number: %s, last_digits_card_number: %s",
                        reference, email, state, cardBrand, fromDate, toDate, pageNumber, displaySize, agreementId, cardHolderName, firstDigitsCardNumber, lastDigitsCardNumber));

        return paymentSearchService.doSearch(account, reference, email, state, cardBrand,
                fromDate, toDate, pageNumber, displaySize, agreementId, cardHolderName, firstDigitsCardNumber, lastDigitsCardNumber);
    }

    @POST
    @Timed
    @Path("/v1/payments")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Create new payment",
            notes = "Create a new payment for the account associated to the Authorisation token. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            code = 201,
            nickname = "newPayment")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = PaymentWithAllLinks.class),
            @ApiResponse(code = 400, message = "Bad request", response = PaymentError.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 422, message = "Invalid attribute value: description. Must be less than or equal to 255 characters length", response = PaymentError.class),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response createNewPayment(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                     @ApiParam(value = "requestPayload", required = true) ValidCreatePaymentRequest validCreatePaymentRequest) {
        logger.info("Payment create request passed validation and parsed to {}", validCreatePaymentRequest);

        PaymentWithAllLinks createdPayment = createPaymentService.create(account, validCreatePaymentRequest);

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
    @ApiOperation(
            value = "Cancel payment",
            notes = "Cancel a payment based on the provided payment ID and the Authorisation token. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'. A payment can only be cancelled if it's in " +
                    "a state that isn't finished.",
            code = 204)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 400, message = "Cancellation of payment failed", response = PaymentError.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = PaymentError.class),
            @ApiResponse(code = 409, message = "Conflict", response = PaymentError.class),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)
    })
    public Response cancelPayment(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                  @PathParam("paymentId") String paymentId) {

        logger.info("Payment cancel request - payment_id=[{}]", paymentId);

        return cancelPaymentService.cancel(account, paymentId);
    }

    @POST
    @Timed
    @Path("/v1/payments/{paymentId}/capture")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Capture payment",
            notes = "Capture a payment based on the provided payment ID and the Authorisation token. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'. A payment can only be captured if it's in " +
                    "'submitted' state",
            code = 204)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "No Content"),
            @ApiResponse(code = 400, message = "Capture of payment failed", response = PaymentError.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = PaymentError.class),
            @ApiResponse(code = 409, message = "Conflict", response = PaymentError.class),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)
    })
    public Response capturePayment(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                   @PathParam("paymentId") String paymentId) {
        logger.info("Payment capture request - payment_id=[{}]", paymentId);

        Response connectorResponse = capturePaymentService.capture(account, paymentId);

        if (connectorResponse.getStatus() == HttpStatus.SC_NO_CONTENT) {
            connectorResponse.close();
            return Response.noContent().build();
        }

        throw new CaptureChargeException(connectorResponse);
    }
}
