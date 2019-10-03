package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Refunding card payments")
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
    @Operation(
            security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Search refunds",
            summary = "Search refunds by 'from' and 'to' date. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SearchRefundsResults.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credentials are required to access this resource"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Invalid parameters. See Public API documentation for the correct data formats",
                            content = @Content(schema = @Schema(implementation = RefundError.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = RefundError.class))),
            }
    )
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
    public SearchRefundsResults searchRefunds(@Parameter(hidden = true) @ApiParam(value = "accountId", hidden = true)
                                  @Auth Account account,
                                  @Parameter(description = "From date of refunds to be searched (this date is inclusive). Example=2015-08-13T12:35:00Z")
                                  @ApiParam(value = "From date of refunds to be searched (this date is inclusive). Example=2015-08-13T12:35:00Z", hidden = false)
                                  @QueryParam("from_date") String fromDate,
                                  @Parameter(description = "To date of refunds to be searched (this date is exclusive). Example=2015-08-14T12:35:00Z")
                                  @ApiParam(value = "To date of refunds to be searched (this date is exclusive). Example=2015-08-14T12:35:00Z", hidden = false)
                                  @QueryParam("to_date") String toDate,
                                  @Parameter(description = "Page number requested for the search, should be a positive integer (optional, defaults to 1)")
                                  @ApiParam(value = "Page number requested for the search, should be a positive integer (optional, defaults to 1)", hidden = false)
                                  @QueryParam("page") String pageNumber,
                                  @Parameter(description = "Number of results to be shown per page, should be a positive integer (optional, defaults to 500, max 500)", hidden = false)
                                  @ApiParam(value = "Number of results to be shown per page, should be a positive integer (optional, defaults to 500, max 500)", hidden = false)
                                  @QueryParam("display_size") String displaySize,
                                  @Parameter(hidden = true) @ApiParam(hidden = true) @HeaderParam("X-Ledger") String strategyName) {

        logger.info("Refunds search request with strategy [{}]- [ {} ]", strategyName,
                format("from_date: %s, to_date: %s, page: %s, display_size: %s",
                        fromDate, toDate, pageNumber, displaySize));

        RefundsParams refundsParams = new RefundsParams(fromDate, toDate, pageNumber, displaySize);
        SearchRefundsStrategy strategy = new SearchRefundsStrategy(configuration, strategyName, account, refundsParams, searchRefundsService);

        return strategy.validateAndExecute();
    }
}
