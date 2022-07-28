package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.search.dispute.DisputesSearchResults;
import uk.gov.pay.api.service.DisputesSearchParams;
import uk.gov.pay.api.service.SearchDisputesService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.pay.api.validation.DisputeSearchValidator.validateDisputeParameters;

@Path("/")
@Produces({"application/json"})
public class SearchDisputesResource {
    private static final Logger logger = LoggerFactory.getLogger(SearchDisputesResource.class);
    private final SearchDisputesService searchDisputesService;

    @Inject
    public SearchDisputesResource(SearchDisputesService searchDisputesService) {
        this.searchDisputesService = searchDisputesService;
    }

    @GET
    @Timed
    @Path("/v1/disputes")
    @Produces(APPLICATION_JSON)
    public DisputesSearchResults searchDisputes(@Auth Account account,
                                                @QueryParam("from_date") String fromDate,
                                                @QueryParam("to_date") String toDate,
                                                @QueryParam("from_settled_date") String fromSettledDate,
                                                @QueryParam("to_settled_date") String toSettledDate,
                                                @QueryParam("status") String status,
                                                @QueryParam("page") String pageNumber,
                                                @QueryParam("display_size") String displaySize) {
        logger.info("Disputes search request - [ {} ]",
                format("from_date: %s, to_date: %s, from_settled_date: %s, to_settled_date: %s, " +
                                "status: s, page: %s, display_size: %s",
                        fromDate, toDate, fromSettledDate, toSettledDate, status, pageNumber, displaySize));

        DisputesSearchParams params = new DisputesSearchParams.Builder()
                .withFromDate(fromDate)
                .withToDate(toDate)
                .withFromSettledDate(fromSettledDate)
                .withToSettledDate(toSettledDate)
                .withStatus(status)
                .withPage(pageNumber)
                .withDisplaySize(displaySize)
                .build();

        validateDisputeParameters(params);

        return searchDisputesService.searchDisputes(account, params);
    }
}
