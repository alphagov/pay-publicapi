package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.directdebit.agreement.CreateAgreementRequest;
import uk.gov.pay.api.model.directdebit.agreement.CreateAgreementResponse;
import uk.gov.pay.api.service.CreateAgreementService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Api(value = "/", description = "Public Api Endpoints for an agreements")
@Produces({"application/json"})
public class AgreementsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementsResource.class);

    private final String baseUrl;
    private CreateAgreementService createAgreementService;


    @Inject
    public AgreementsResource(PublicApiConfig configuration, CreateAgreementService createAgreementService) {
        this.baseUrl = configuration.getBaseUrl();
        this.createAgreementService = createAgreementService;
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
        CreateAgreementResponse createAgreementResponse = createAgreementService.create(account, createAgreementRequest);
        URI agreementUri = UriBuilder.fromUri(baseUrl)
                .path("/v1/agreements/{agreementId}")
                .build(createAgreementResponse.getAgreementId());
        LOGGER.info("Agreement returned (created): [ {} ]", createAgreementResponse);
        return Response.created(agreementUri).entity(createAgreementResponse).build();
    }
}
