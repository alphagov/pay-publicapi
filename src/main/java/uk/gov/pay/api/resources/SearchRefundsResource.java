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
import uk.gov.pay.api.model.RefundError;
import uk.gov.pay.api.model.search.card.SearchRefundsResults;
import uk.gov.pay.api.service.RefundsParams;
import uk.gov.pay.api.service.SearchRefundsService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
    private PublicApiConfig configuration;

    @Inject
    public SearchRefundsResource(SearchRefundsService searchRefundsService, PublicApiConfig configuration) {
        this.searchRefundsService = searchRefundsService;
        this.configuration = configuration;
    }

    @GET
    @Timed
    @Path("/v1/refunds")
    @Produces(APPLICATION_JSON)
    @ApiOperation(
            nickname = "Search refunds",
            value = "Search refunds",
            notes = "Search refunds. You must include your API key in the 'Authorization' HTTP header: `Authorization: Bearer YOUR-API-KEY`.",
            responseContainer = "List",
            authorizations = {@Authorization("Authorization")},
            code = 200)

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Your request succeeded.", response = SearchRefundsResults.class),
            @ApiResponse(code = 401, message = "You did not include your API key in the 'Authorization' HTTP header, or the key was invalid."),
            @ApiResponse(code = 422, message = "There were invalid parameters in your request.", response = RefundError.class),
            @ApiResponse(code = 500, message = "Something's wrong with GOV.UK Pay. [Contact us](https://docs.payments.service.gov.uk/support_contact_and_more_information/#contact-us) for help.", response = RefundError.class)})
    public SearchRefundsResults searchRefunds(@ApiParam(value = "accountId", hidden = true)
                                  @Auth Account account,
                                  @ApiParam(value = "The start date for refunds to be searched, inclusive. Dates must be in ISO 8601 format. For example 2015-08-13T12:35:00Z.", hidden = false)
                                  @QueryParam("from_date") String fromDate,
                                  @ApiParam(value = "The end date for refunds to be searched, exclusive. Dates must be in ISO 8601 format. For example 2015-08-13T12:35:00Z.", hidden = false)
                                  @QueryParam("to_date") String toDate,
                                  @ApiParam(value = "The page number of results to return.", hidden = false)
                                  @QueryParam("page") String pageNumber,
                                  @ApiParam(value = "The number of results per page.", hidden = false)
                                  @QueryParam("display_size") String displaySize,
                                  @ApiParam(hidden = true) @HeaderParam("X-Ledger") String strategyName) {

        logger.info("Refunds search request with strategy [{}]- [ {} ]", strategyName,
                format("from_date: %s, to_date: %s, page: %s, display_size: %s",
                        fromDate, toDate, pageNumber, displaySize));

        RefundsParams refundsParams = new RefundsParams(fromDate, toDate, pageNumber, displaySize);
        SearchRefundsStrategy strategy = new SearchRefundsStrategy(configuration, strategyName, account, refundsParams, searchRefundsService);

        return strategy.validateAndExecute();
    }
}
