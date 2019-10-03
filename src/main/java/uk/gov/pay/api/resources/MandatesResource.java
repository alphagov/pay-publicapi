package uk.gov.pay.api.resources;

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
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.directdebit.mandates.MandateError;
import uk.gov.pay.api.model.directdebit.mandates.CreateMandateRequest;
import uk.gov.pay.api.model.directdebit.mandates.MandateResponse;
import uk.gov.pay.api.model.search.directdebit.DirectDebitSearchMandatesParams;
import uk.gov.pay.api.model.search.directdebit.SearchMandateResponse;
import uk.gov.pay.api.resources.error.ApiErrorResponse;
import uk.gov.pay.api.service.MandatesService;
import uk.gov.pay.api.service.directdebit.DirectDebitMandateSearchService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Api(tags = "Direct Debit", value = "/v1/directdebit/mandates")
@Produces({"application/json"})
public class MandatesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(MandatesResource.class);

    private final String baseUrl;
    private final MandatesService mandateService;
    private final DirectDebitMandateSearchService mandateSearchService;

    @Inject
    public MandatesResource(PublicApiConfig configuration, MandatesService mandateService, DirectDebitMandateSearchService mandateSearchService) {
        this.baseUrl = configuration.getBaseUrl();
        this.mandateService = mandateService;
        this.mandateSearchService = mandateSearchService;
    }

    @GET
    @Timed
    @Path("/v1/directdebit/mandates/{mandateId}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            nickname = "Get a mandate",
            value = "Find mandate by ID",
            notes = "Get information about a single mandate. You must include your API key in the 'Authorization' HTTP header: `Authorization: Bearer YOUR-API-KEY`.",
            authorizations = {@Authorization("Authorization")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Your request succeeded.", response = MandateResponse.class),
            @ApiResponse(code = 401, message = "You did not include your API key in the 'Authorization' HTTP header, or the key was invalid."),
            @ApiResponse(code = 404, message = "No mandate matched the `mandateId` you provided.", response = MandateError.class),
            @ApiResponse(code = 429, message = "You exceeded a [rate limit](https://docs.payments.service.gov.uk/api_reference/#rate-limits) for requests to the API.", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Something's wrong with GOV.UK Pay. [Contact us](https://docs.payments.service.gov.uk/support_contact_and_more_information/#contact-us) for help.", response = MandateError.class)})
    public Response getMandate(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                               @PathParam("mandateId") String mandateId) {
        LOGGER.info("Mandate get request - [ {} ]", mandateId);
        MandateResponse getMandateResponse = mandateService.get(account, mandateId);
        LOGGER.info("Mandate returned: [ {} ]", getMandateResponse);
        return Response.ok().entity(getMandateResponse).build();
    }

    @GET
    @Timed
    @Path("/v1/directdebit/mandates/")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            nickname = "Search mandates",
            value = "Search mandates",
            notes = "Search Direct Debit mandates. You must include your API key in the 'Authorization' HTTP header: `Authorization: Bearer YOUR-API-KEY`",
            authorizations = {@Authorization("Authorization")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Your request succeeded.", response = SearchMandateResponse.class),
            @ApiResponse(code = 401, message = "You did not include your API key in the 'Authorization' HTTP header, or the key was invalid."),
            @ApiResponse(code = 404, message = "There are no results that match your search.", response = MandateError.class),
            @ApiResponse(code = 422, message = "There were invalid parameters in your request.", response = PaymentError.class),
            @ApiResponse(code = 429, message = "You exceeded a [rate limit](https://docs.payments.service.gov.uk/api_reference/#rate-limits) for requests to the API.", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Something's wrong with GOV.UK Pay. [Contact us](https://docs.payments.service.gov.uk/support_contact_and_more_information/#contact-us) for help.", response = MandateError.class)})
    public Response searchMandates(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                   @Valid @BeanParam DirectDebitSearchMandatesParams mandateSearchParams) {
        LOGGER.info("Mandate search request - [ {} ]", mandateSearchParams.toString());
        SearchMandateResponse searchMandatesResponse = mandateSearchService.search(account, mandateSearchParams);

        LOGGER.info("Mandate search returned: [ {} ]", searchMandatesResponse);
        return Response.ok().entity(searchMandatesResponse).build();
    }

    @POST
    @Timed
    @Path("/v1/directdebit/mandates")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            nickname = "Create a mandate",
            value = "Create a new mandate",
            notes = "Set up a Direct Debit mandate. You must include your API key in the 'Authorization' HTTP header: `Authorization: Bearer YOUR-API-KEY`. The API will create a mandate in the account linked to the API key you provide.",
            code = 201,
            authorizations = {@Authorization("Authorization")})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The mandate was successfully set up.", response = MandateResponse.class),
            @ApiResponse(code = 400, message = "The API could not process your request, for example because you did not include a required parameter.", response = PaymentError.class),
            @ApiResponse(code = 401, message = "You did not include your API key in the 'Authorization' HTTP header, or the key was invalid."),
            @ApiResponse(code = 429, message = "You exceeded a [rate limit](https://docs.payments.service.gov.uk/api_reference/#rate-limits) for requests to the API.", response = ApiErrorResponse.class),
            @ApiResponse(code = 500, message = "Something's wrong with GOV.UK Pay. [Contact us](https://docs.payments.service.gov.uk/support_contact_and_more_information/#contact-us) for help.", response = PaymentError.class)})
    public Response createMandate(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                  @ApiParam(value = "The structure of your request to the API when you set up a Direct Debit mandate.", required = true) @Valid CreateMandateRequest createMandateRequest) {
        LOGGER.info("Mandate create request - [ {} ]", createMandateRequest);
        MandateResponse createMandateResponse = mandateService.create(account, createMandateRequest);
        URI mandateUri = UriBuilder.fromUri(baseUrl)
                .path("/v1/directdebit/mandates/{mandateId}")
                .build(createMandateResponse.getMandateId());
        LOGGER.info("Mandate returned (created): [ {} ]", createMandateResponse);
        return Response.created(mandateUri).entity(createMandateResponse).build();
    }
}
