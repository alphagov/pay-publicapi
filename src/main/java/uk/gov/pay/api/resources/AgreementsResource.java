package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.URI;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.directdebit.agreement.AgreementError;
import uk.gov.pay.api.model.directdebit.agreement.CreateAgreementRequest;
import uk.gov.pay.api.model.directdebit.agreement.CreateAgreementResponse;
import uk.gov.pay.api.model.directdebit.agreement.GetAgreementResponse;
import uk.gov.pay.api.service.AgreementService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Api(value = "/", description = "Public Api Endpoints for an agreements")
@Produces({"application/json"})
public class AgreementsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementsResource.class);

    private final String baseUrl;
    private final AgreementService agreementService;


    @Inject
    public AgreementsResource(PublicApiConfig configuration, AgreementService agreementService) {
        this.baseUrl = configuration.getBaseUrl();
        this.agreementService = agreementService;
    }

    @GET
    @Timed
    @Path("/v1/agreements/{agreementId}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Find agreement by ID",
            notes = "Return information about the payment " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = GetAgreementResponse.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 404, message = "Not found", response = AgreementError.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = AgreementError.class)})
    public Response getPayment(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
            @PathParam("agreementId") String agreementId) {
        LOGGER.info("Agreement get request - [ {} ]", agreementId);
        GetAgreementResponse getAgreementResponse = agreementService.get(account, agreementId);
        LOGGER.info("Agreement returned (created): [ {} ]", getAgreementResponse);
        return Response.ok().entity(getAgreementResponse).build();
    }
    
    @POST
    @Timed
    @Path("/v1/agreements")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Create a new agreement",
            notes = "Create a new agreement",
            code = 201,
            nickname = "newAgreement")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = CreateAgreementResponse.class),
            @ApiResponse(code = 400, message = "Bad request", response = PaymentError.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response createNewAgreement(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                       @ApiParam(value = "requestPayload", required = true) CreateAgreementRequest createAgreementRequest) {
        LOGGER.info("Agreement create request - [ {} ]", createAgreementRequest);
        CreateAgreementResponse createAgreementResponse = agreementService.create(account, createAgreementRequest);
        URI agreementUri = UriBuilder.fromUri(baseUrl)
                .path("/v1/agreements/{agreementId}")
                .build(createAgreementResponse.getAgreementId());
        LOGGER.info("Agreement returned (created): [ {} ]", createAgreementResponse);
        return Response.created(agreementUri).entity(createAgreementResponse).build();
    }
}
