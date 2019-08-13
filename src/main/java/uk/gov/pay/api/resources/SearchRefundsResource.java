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
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.RefundError;
import uk.gov.pay.api.model.search.card.SearchRefundsResults;
import uk.gov.pay.api.service.RefundsParams;
import uk.gov.pay.api.service.SearchRefundsService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Api(tags = "Refunding card payments", value = "/")
@Produces({"application/json"})
public class SearchRefundsResource {

    private static final Logger logger = LoggerFactory.getLogger(SearchRefundsResource.class);

    private final SearchRefundsService searchRefundsService;

    @Inject
    public SearchRefundsResource(SearchRefundsService searchRefundsService) {
        this.searchRefundsService = searchRefundsService;
    }

    @GET
    @Timed
    @Path("/v1/refunds")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            nickname = "Search refunds",
            value = "Search refunds",
            notes = "Search refunds by 'from' and 'to' date. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responseContainer = "List",
            authorizations = {@Authorization("Authorization")},
            code = 200)

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = SearchRefundsResults.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 422, message = "Invalid parameters. See Public API documentation for the correct data formats", response = RefundError.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = RefundError.class)})
    public SearchRefundsResults searchRefunds(@ApiParam(value = "accountId", hidden = true)
                                  @Auth Account account,
                                  @ApiParam(value = "From date of refunds to be searched (this date is inclusive). Example=2015-08-13T12:35:00Z", hidden = false)
                                  @QueryParam("from_date") String fromDate,
                                  @ApiParam(value = "To date of refunds to be searched (this date is exclusive). Example=2015-08-14T12:35:00Z", hidden = false)
                                  @QueryParam("to_date") String toDate,
                                  @ApiParam(value = "Page number requested for the search, should be a positive integer (optional, defaults to 1)", hidden = false)
                                  @QueryParam("page") String pageNumber,
                                  @ApiParam(value = "Number of results to be shown per page, should be a positive integer (optional, defaults to 500, max 500)", hidden = false)
                                  @QueryParam("display_size") String displaySize) {

        logger.info("Refunds search request - [ {} ]",
                format("from_date: %s, to_date: %s, page: %s, display_size: %s",
                        fromDate, toDate, pageNumber, displaySize));

        RefundsParams refundsParams = new RefundsParams(fromDate, toDate, pageNumber, displaySize);
        return searchRefundsService.searchConnectorRefunds(account, refundsParams);
    }
}
