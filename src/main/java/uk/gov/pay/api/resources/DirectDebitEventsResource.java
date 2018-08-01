package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.links.directdebit.DirectDebitEventsResponse;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.DirectDebitEventService;
import uk.gov.pay.api.validation.DirectDebitEventSearchValidator;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.jetty.util.StringUtil.isBlank;

@Path("/")
@Api(value = "/", description = "Public Api Endpoint to get Direct Debit Events")
@Produces({"application/json"})
public class DirectDebitEventsResource {

    private final Client client;
    private final ConnectorUriGenerator connectorUriGenerator;
    private final DirectDebitEventService directDebitEventService;

    @Inject
    public DirectDebitEventsResource(Client client, ConnectorUriGenerator connectorUriGenerator, DirectDebitEventService directDebitEventService) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
        this.directDebitEventService = directDebitEventService;
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

        DirectDebitEventSearchValidator.validateSearchParameters(toDate, fromDate, displaySize);
        String uri = connectorUriGenerator.eventsURI(account, parseDate(toDate), parseDate(fromDate), page, displaySize, agreementId, paymentId);
        DirectDebitEventsResponse response = directDebitEventService.getResponse(uri);
        return Response.ok(response).build();
    }
    
    private Optional<ZonedDateTime> parseDate(String date) {
        if (!isBlank(date)) {
            return Optional.of(ZonedDateTime.parse(date));
        }
        return Optional.empty();
    }
}
