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
@Api(tags = "Direct Debit", value = "/v1/directdebit/payments")
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
            nickname = "Collect a Direct Debit payment",
            value = "Create new Direct Debit payment",
            notes = "Collect a payment against a Direct Debit mandate. You must include your API key in the 'Authorization' HTTP header: `Authorization: Bearer YOUR-API-KEY`. The API will create a mandate in the account linked to the API key you provide.",
            code = 201,
            authorizations = {@Authorization("Authorization")})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The payment was successfully created.", response = DirectDebitPayment.class),
            @ApiResponse(code = 400, message = "The API could not process your request, for example because you did not include a required parameter.", response = PaymentError.class),
            @ApiResponse(code = 401, message = "You did not include your API key in the 'Authorization' HTTP header, or the key was invalid."),
            @ApiResponse(code = 422, message = "There were invalid parameters in your request.", response = PaymentError.class),
            @ApiResponse(code = 429, message = "You exceeded a [rate limit](https://docs.payments.service.gov.uk/api_reference/#rate-limits) for requests to the API.", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Something's wrong with GOV.UK Pay. [Contact us](https://docs.payments.service.gov.uk/support_contact_and_more_information/#contact-us) for help.", response = PaymentError.class)})
    public Response createPayment(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                  @ApiParam(value = "requestPayload", required = true) @Valid CreateDirectDebitPaymentRequest request) {
        DirectDebitPayment payment = createDirectDebitPaymentsService.create(account, request);
        return Response.created(publicApiUriGenerator.getDirectDebitPaymentURI(payment.getPaymentId()))
                .entity(payment).build();
    }


    @GET
    @Timed
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            nickname = "Search Direct Debit payments",
            value = "Search Direct Debit payments",
            notes = "Search Direct Debit payments. You must include your API key in the 'Authorization' HTTP header: `Authorization: Bearer YOUR-API-KEY`.",
            responseContainer = "List",
            code = 200,
            authorizations = {@Authorization("Authorization")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Your request succeeded.", response = DirectDebitSearchResponse.class),
            @ApiResponse(code = 401, message = "You did not include your API key in the 'Authorization' HTTP header, or the key was invalid."),
            @ApiResponse(code = 422, message = "There were invalid parameters in your request.", response = PaymentError.class),
            @ApiResponse(code = 429, message = "You exceeded a [rate limit](https://docs.payments.service.gov.uk/api_reference/#rate-limits) for requests to the API.", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Something's wrong with GOV.UK Pay. [Contact us](https://docs.payments.service.gov.uk/support_contact_and_more_information/#contact-us) for help.", response = PaymentError.class)})
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
            nickname = "Get a Direct Debit payment",
            value = "Find Direct Debit payment by ID",
            notes = "Get information about a single Direct Debit payment. You must include your API key in the 'Authorization' HTTP header: `Authorization: Bearer YOUR-API-KEY`.",
            code = 200,
            authorizations = {@Authorization("Authorization")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Your request succeeded.", response = DirectDebitPayment.class),
            @ApiResponse(code = 401, message = "You did not include your API key in the 'Authorization' HTTP header, or the key was invalid."),
            @ApiResponse(code = 404, message = "No payment matched the `paymentId` you provided.", response = PaymentError.class),
            @ApiResponse(code = 429, message = "You exceeded a [rate limit](https://docs.payments.service.gov.uk/api_reference/#rate-limits) for requests to the API.", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Something's wrong with GOV.UK Pay. [Contact us](https://docs.payments.service.gov.uk/support_contact_and_more_information/#contact-us) for help.", response = PaymentError.class)})
    public Response getPayment(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                               @PathParam("paymentId") @ApiParam(value = "The payment to get information about.") String paymentId) {

        LOGGER.info("Direct Debit Payment request - paymentId={}", paymentId);
        DirectDebitPayment payment = getDirectDebitPaymentService.getDirectDebitPayment(account, paymentId);
        return Response.ok(payment).build();
    }
}
