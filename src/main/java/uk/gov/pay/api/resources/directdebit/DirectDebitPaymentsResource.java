package uk.gov.pay.api.resources.directdebit;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.CreateDirectDebitPaymentRequest;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.directdebit.mandates.DirectDebitPayment;
import uk.gov.pay.api.model.directdebit.mandates.MandateError;
import uk.gov.pay.api.model.directdebit.mandates.MandateResponse;
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
    @ApiOperation(
            nickname = "createDirectDebitPayment",
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
    public Response createPayment(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                  @Valid CreateDirectDebitPaymentRequest request) {
        DirectDebitPayment payment = createDirectDebitPaymentsService.create(account, request);
        return Response.created(publicApiUriGenerator.getDirectDebitPaymentURI(payment.getPaymentId()))
                .entity(payment).build();
    }


    @GET
    @Timed
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            nickname = "searchDirectDebitPayments",
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
    public Response searchPayments(@ApiParam(value = "accountId", hidden = true)
                                   @Auth Account account,
                                   @Valid @BeanParam DirectDebitSearchPaymentsParams searchPaymentsParams,
                                   @Context UriInfo uriInfo) {

        LOGGER.info("Payments search request - {}", searchPaymentsParams);
        return directDebitPaymentSearchService.doSearch(account, searchPaymentsParams);
    }

    @GET
    @Timed
    @Path("{paymentId}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            nickname = "getDirectDebitPayment",
            value = "Find direct debit payment by ID",
            notes = "Return information about the direct debit payment. " +
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
    public Response getPayment(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                               @PathParam("paymentId") @ApiParam(value = "Payment identifier") String paymentId) {

        LOGGER.info("Direct Debit Payment request - paymentId={}", paymentId);
        DirectDebitPayment payment = getDirectDebitPaymentService.getDirectDebitPayment(account, paymentId);
        return Response.ok(payment).build();
    }
}
