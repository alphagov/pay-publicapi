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
import uk.gov.pay.api.validation.DirectDebitEventSearchValidator;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

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
            @QueryParam("to_date") String toDate,
            @QueryParam("from_date") String fromDate,
            @ApiParam(value = "Defaults to a maximum of 500", hidden = false)
            @QueryParam("display_size") Integer displaySize,
            @QueryParam("page") Integer page,
            @ApiParam(value = "ID of associated agreement", hidden = false)
            @QueryParam("agreement_id") String agreementId,
            @ApiParam(value = "ID of associated payment", hidden = false)
            @QueryParam("payment_id") String paymentId
    ) {

        DirectDebitEventSearchValidator.validateSearchParameters(toDate, fromDate);

        String uri = connectorUriGenerator.eventsURI(account, ZonedDateTime.parse(toDate), ZonedDateTime.parse(fromDate), page, displaySize, agreementId, paymentId);
        Response ddConnectorResponse = client.target(uri)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        
        if (ddConnectorResponse.getStatus() == HttpStatus.SC_OK) {
            DirectDebitEventsResponse eventsResponse = ddConnectorResponse.readEntity(DirectDebitEventsResponse.class);
            return Response.ok(eventsResponse).build();
        }
        
        throw new ConnectorResponseErrorException(ddConnectorResponse);
    }

    private class BeforeAndAfter {
        public final ZonedDateTime before;
        public final ZonedDateTime after;

        public BeforeAndAfter(ZonedDateTime before, ZonedDateTime after) {
            this.before = before;
            this.after = after;
        }
    }
}
