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
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.search.card.SearchRefundsResults;
import uk.gov.pay.api.service.SearchRefundsService;
import uk.gov.pay.api.service.PublicApiUriGenerator;
import uk.gov.pay.api.service.RefundsUriGenerator;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Api(value = "/", description = "Public Api Endpoint to get all Refunds")
@Produces({"application/json"})
public class SearchRefundsResource {

    private static final Logger logger = LoggerFactory.getLogger(SearchRefundsResource.class);
    
    private final Client client;
    private final SearchRefundsService searchRefundsService;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final RefundsUriGenerator refundsUriGenerator;

    @Inject
    public SearchRefundsResource(Client client,
                           SearchRefundsService searchRefundsService,
                           PublicApiUriGenerator publicApiUriGenerator,
                           RefundsUriGenerator refundsUriGenerator) {
        this.client = client;
        this.searchRefundsService = searchRefundsService;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.refundsUriGenerator = refundsUriGenerator;
    }

    @GET
    @Timed
    @Path("/v1/refunds/account/{accountId}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            value = "Search refunds",
            notes = "Search refunds by page number and display size. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responseContainer = "List",
            code = 200)

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = SearchRefundsResults.class),
            @ApiResponse(code = 401, message = "Credentials are required to access this resource"),
            @ApiResponse(code = 422, message = "Invalid parameters. See Public API documentation for the correct data formats", response = PaymentError.class),
            @ApiResponse(code = 500, message = "Downstream system error", response = PaymentError.class)})
    public Response searchRefunds(@ApiParam(value = "accountId", hidden = true)
                                   @Auth Account account,
                                   @ApiParam(value = "Page number requested for the search, should be a positive integer (optional, defaults to 1)", hidden = false)
                                   @QueryParam("page") String pageNumber,
                                   @ApiParam(value = "Number of results to be shown per page, should be a positive integer (optional, defaults to 500, max 500)", hidden = false)
                                   @QueryParam("display_size") String displaySize) {

        logger.info("Refunds search request - [ {} ]",
                format("page: %s, display_size: %s",
                        pageNumber, displaySize));
        
        return searchRefundsService.getAllRefunds(account, pageNumber, displaySize);
    }
}
