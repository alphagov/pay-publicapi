package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
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
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.eclipse.jetty.util.StringUtil.isBlank;

@Path("/")
@Produces({"application/json"})
public class DirectDebitEventsResource {

    private final ConnectorUriGenerator connectorUriGenerator;
    private final DirectDebitEventService directDebitEventService;

    @Inject
    public DirectDebitEventsResource(ConnectorUriGenerator connectorUriGenerator, DirectDebitEventService directDebitEventService) {
        this.connectorUriGenerator = connectorUriGenerator;
        this.directDebitEventService = directDebitEventService;
    }

    @GET
    @Timed
    @Path("/v1/events")
    @Produces(APPLICATION_JSON)
    public Response getDirectDebitEvents(
            @Auth Account account,
            @QueryParam("to_date") String toDate,
            @QueryParam("from_date") String fromDate,
            @QueryParam("display_size") Integer displaySize,
            @QueryParam("page") Integer page,
            @QueryParam("agreement_id") String agreementId,
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
