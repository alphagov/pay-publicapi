package uk.gov.pay.api.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.pay.api.exception.CreateChargeException;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.directdebit.CreateAgreementRequest;
import uk.gov.pay.api.model.directdebit.CreateAgreementResponse;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static java.lang.String.format;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Api(value = "/", description = "Public Api Endpoints")
@Produces({"application/json"})
public class AgreementsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementsResource.class);

    private final String baseUrl;

    private final Client client;
    private final String connectorDDUrl;
    private final ObjectMapper objectMapper;

    public AgreementsResource(String baseUrl, Client client, String connectorDDUrl, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.client = client;
        this.connectorDDUrl = connectorDDUrl;
        this.objectMapper = objectMapper;
    }

    @POST()
    @Path("/v1/agreements")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Create new agreement",
            notes = "Create a new agreement",
            code = 201,
            nickname = "newAgreement")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = CreateAgreementResponse.class),
            @ApiResponse(code = 400, message = "Bad request", response = PaymentError.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 422, message = "Invalid attribute value: description. Must be less than or equal to 255 characters length", response = PaymentError.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response createNewAgreement(@ApiParam(value = "accountId", hidden = true) @Auth Account account,
                                       @ApiParam(value = "requestPayload", required = true) CreateAgreementRequest requestPayload) {

        LOGGER.info("Agreement create request - [ {} ]", requestPayload);

        Response connectorResponse = client
                .target(getDDConnectorUrl(format("/v1/api/accounts/%s/agreements", account.getName())))
                .request()
                .post(buildAgreementRequestPayload(requestPayload));

        if (connectorResponse.getStatus() == HttpStatus.SC_CREATED) {
            CreateAgreementResponse agreementFromResponse = connectorResponse.readEntity(CreateAgreementResponse.class);
            URI agreementUri = UriBuilder.fromUri(baseUrl)
                    .path("/v1/agreements/{agreementId}")
                    .build(agreementFromResponse.getAgreementId());
            LOGGER.info("Agreement returned (created): [ {} ]", agreementFromResponse);
            return Response.created(agreementUri).entity(agreementFromResponse).build();

        }

        throw new CreateChargeException(connectorResponse);
    }

    private Entity buildAgreementRequestPayload(CreateAgreementRequest requestPayload) {
        return json(new JsonStringBuilder()
                .add("name", requestPayload.getName())
                .add("email", requestPayload.getEmail())
                .add("type", requestPayload.getType())
                .build());
    }

    private String getDDConnectorUrl(String urlPath) {
        UriBuilder builder = UriBuilder
                .fromPath(connectorDDUrl)
                .path(urlPath);
        
        return builder.toString();
    }
}
