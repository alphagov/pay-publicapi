package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.HttpStatus;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.ConnectorResponseErrorException;
import uk.gov.pay.api.model.links.directdebit.DirectDebitEventsResponse;
import uk.gov.pay.api.service.ConnectorUriGenerator;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Api(value = "/", description = "Public Api Endpoint to get Direct Debit Events")
@Produces({"application/json"})
public class DirectDebitEventsResource {

    private final Client client;
    private final ConnectorUriGenerator connectorUriGenerator;

    @Inject
    public DirectDebitEventsResource(Client client, ConnectorUriGenerator connectorUriGenerator) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
    }

    @GET
    @Timed
    @Path("/v1/events")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Get direct debit events",
            notes = "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = List.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource")})
    public Response getDirectDebitEvents(
            @ApiParam(value = "accountId", hidden = true) @Auth Account account,
            @QueryParam("before") String beforeDate,
            @QueryParam("after") String afterDate,
            @ApiParam(value = "Defaults to a maximum of 500", hidden = false)
            @QueryParam("page_size") Integer pageSize,
            @QueryParam("page") Integer page,
            @ApiParam(value = "ID of associated agreement", hidden = false)
            @QueryParam("agreement_id") String agreementId,
            @ApiParam(value = "ID of associated payment", hidden = false)
            @QueryParam("payment_id") String paymentId
    ) {
        
        if (!isValid(beforeDate, afterDate))
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("The supplied dates were not in the format yyyy-MM-ddThh:mm:ssZ.")
                    .build();
        
        Response ddConnectorResponse = client.target(connectorUriGenerator.eventsURI(account, beforeDate, afterDate, page, pageSize, agreementId, paymentId))
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        
        if (ddConnectorResponse.getStatus() == HttpStatus.SC_OK) {
            DirectDebitEventsResponse eventsResponse = ddConnectorResponse.readEntity(DirectDebitEventsResponse.class);
            return Response.ok(eventsResponse).build();
        }
        
        throw new ConnectorResponseErrorException(ddConnectorResponse);
    }

    private boolean isValid(String beforeDate, String afterDate) {
        try {
            ZonedDateTime.parse(beforeDate);
            ZonedDateTime.parse(afterDate);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }
}
