package uk.gov.pay.api.resources.directdebit;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.CreateDirectDebitPaymentRequest;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.directdebit.agreement.DirectDebitPayment;
import uk.gov.pay.api.resources.error.ApiErrorResponse;
import uk.gov.pay.api.service.PublicApiUriGenerator;
import uk.gov.pay.api.service.directdebit.CreateDirectDebitPaymentsService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/v1/directdebit/payments/")
@Api(value = "/v1/directdebit/payments/")
@Produces({"application/json"})
public class DirectDebitPaymentsResource {

    private final CreateDirectDebitPaymentsService createDirectDebitPaymentsService;
    private final PublicApiUriGenerator publicApiUriGenerator;

    @Inject
    public DirectDebitPaymentsResource(CreateDirectDebitPaymentsService createDirectDebitPaymentsService,
                                       PublicApiUriGenerator publicApiUriGenerator) {
        this.createDirectDebitPaymentsService = createDirectDebitPaymentsService;
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
}
