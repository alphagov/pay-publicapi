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
import uk.gov.pay.api.model.search.directdebit.DirectDebitSearchResponse;
import uk.gov.pay.api.resources.error.ApiErrorResponse;
import uk.gov.pay.api.service.PublicApiUriGenerator;
import uk.gov.pay.api.service.directdebit.CreateDirectDebitPaymentsService;
import uk.gov.pay.api.service.directdebit.DirectDebitPaymentSearchService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/v1/directdebit/payments/")
@Api(value = "/v1/directdebit/payments/")
@Produces({"application/json"})
public class DirectDebitPaymentsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectDebitPaymentsResource.class);

    private final CreateDirectDebitPaymentsService createDirectDebitPaymentsService;
    private final DirectDebitPaymentSearchService directDebitPaymentSearchService;
    private final PublicApiUriGenerator publicApiUriGenerator;

    @Inject
    public DirectDebitPaymentsResource(CreateDirectDebitPaymentsService createDirectDebitPaymentsService,
                                       DirectDebitPaymentSearchService directDebitPaymentSearchService,
                                       PublicApiUriGenerator publicApiUriGenerator) {
        this.createDirectDebitPaymentsService = createDirectDebitPaymentsService;
        this.directDebitPaymentSearchService = directDebitPaymentSearchService;
        this.publicApiUriGenerator = publicApiUriGenerator;
    }

    @POST
    @Timed
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Create new DirectDebit payment",
            notes = "Create a new DirectDebit payment for the account associated to the Authorisation token. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            code = 201,
            nickname = "newDirectDebitPayment",
            authorizations = {@Authorization("Authorization")})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = DirectDebitPayment.class),
            @ApiResponse(code = 400, message = "Bad request", response = PaymentError.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 422, message = "Invalid attribute value: description. Must be less than or equal " +
                    "to 255 characters length", response = PaymentError.class),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response createPayment(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                  @Valid CreateDirectDebitPaymentRequest request) {
        DirectDebitPayment payment = createDirectDebitPaymentsService.create(account, request);
        return Response.created(publicApiUriGenerator.getDirectDebitPaymentURI(payment.getPaymentId()))
                .entity(payment).build();
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = DirectDebitSearchResponse.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 422, message = "Invalid parameter. See Public API documentation for the correct data formats", response = PaymentError.class),
            @ApiResponse(code = 429, message = "Too many requests", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response searchPayments(@ApiParam(value = "accountId", hidden = true)
                                   @Auth Account account,
                                   @ApiParam(value = "Your payment reference to search", hidden = false)
                                   @QueryParam("reference") String reference,
                                   @ApiParam(value = "State of payments to be searched. Example=success", hidden = false, allowableValues = "pending,success,failed,cancelled,expired")
                                   @QueryParam("state") String state,
                                   @ApiParam(value = "The GOV.UK Pay identifier for the mandate", hidden = false)
                                   @QueryParam("mandate_id") String mandateId,
                                   @ApiParam(value = "From date of payments to be searched (this date is inclusive). Example=2015-08-13T12:35:00Z", hidden = false)
                                   @QueryParam("from_date") String fromDate,
                                   @ApiParam(value = "To date of payments to be searched (this date is exclusive). Example=2015-08-14T12:35:00Z", hidden = false)
                                   @QueryParam("to_date") String toDate,
                                   @ApiParam(value = "Page number requested for the search, should be a positive integer (optional, defaults to 1)", hidden = false)
                                   @QueryParam("page") String pageNumber,
                                   @ApiParam(value = "Number of results to be shown per page, should be a positive integer (optional, defaults to 500, max 500)", hidden = false)
                                   @QueryParam("display_size") String displaySize,
                                   @Context UriInfo uriInfo) {

        LOGGER.info("Payments search request - [ {} ]",
                format("reference:%s, status: %s, mandate_id %s, fromDate: %s, toDate: %s, page: %s, display_size: %s",
                        reference, state, mandateId, fromDate, toDate, pageNumber, displaySize));

        return directDebitPaymentSearchService.doSearch(
                account,
                reference,
                state,
                mandateId,
                fromDate,
                toDate,
                pageNumber,
                displaySize);
    }
}
