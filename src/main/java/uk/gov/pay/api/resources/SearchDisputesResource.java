package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.RequestError;
import uk.gov.pay.api.model.search.dispute.DisputesSearchResults;
import uk.gov.pay.api.resources.error.ApiErrorResponse;
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
@Tag(name = "Disputes")
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
    @Operation(security = {@SecurityRequirement(name = "BearerAuth")},
            operationId = "Search disputes",
            summary = "Search disputes",
            description = "Search disputes by status, 'from' and 'to' date, and 'from' and 'to' settled_date. " +
                    "The Authorisation token needs to be specified in the 'authorization' header " +
                    "as 'authorization: Bearer YOUR_API_KEY_HERE'",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = DisputesSearchResults.class))),
                    @ApiResponse(responseCode = "401",
                            description = "Credentials are required to access this resource"),
                    @ApiResponse(responseCode = "422",
                            description = "Invalid parameters: from_date, to_date, from_settled_date, to_settled_date, status, display_size. See Public API documentation for the correct data formats",
                            content = @Content(schema = @Schema(implementation = RequestError.class))),
                    @ApiResponse(responseCode = "429", description = "Too many requests",
                            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Downstream system error",
                            content = @Content(schema = @Schema(implementation = RequestError.class)))
            }
    )
    public DisputesSearchResults searchDisputes(@Parameter(hidden = true)
                                                    @Auth Account account,
                                                @Parameter(description = "Returns disputes raised on or after the 'from_date' Date and time must be coordinated Universal Time (UTC) and ISO 8601 format to second-level accuracy - 'YYYY-MM-DDTHH:MM:SSZ'.")
                                                @QueryParam("from_date") String fromDate,
                                                @Parameter(description = "Returns disputes raised before the 'to_date' Date and time must be coordinated Universal Time (UTC) and ISO 8601 format to second-level accuracy - 'YYYY-MM-DDTHH:MM:SSZ'.")
                                                @QueryParam("to_date") String toDate,
                                                @Parameter(description = "Returns disputes settled on or after the 'from_settled_date'. Date must be in ISO 8601 format to date-level accuracy - 'YYYY-MM-DD'. Disputes are settled when your payment service provider takes the disputed amount from a payout to your bank account.You can only use 'from_settled_date' to find disputes raised on or after 8 August 2022.")
                                                @QueryParam("from_settled_date") String fromSettledDate,
                                                @Parameter(description = "Returns disputes settled before the 'to_settled_date'. 'Date must be in ISO 8601 format to date-level accuracy - 'YYYY-MM-DD'.Disputes are settled when your payment service provider takes the disputed amount from a payout to your bank account. You can only use 'to_settled_date' to find disputes raised on or after 8 August 2022.")
                                                @QueryParam("to_settled_date") String toSettledDate,
                                                @Parameter(description = "Returns disputes with a matching 'status'. 'status' reflects what stage of the dispute process a dispute is at.", example = "won",
                                                        schema = @Schema(allowableValues = {"needs_response", "under_review", "lost", "won"}))
                                                @QueryParam("status") String status,
                                                @Parameter(description = "Returns a specific page of results. Defaults to '1'.")
                                                @QueryParam("page") String pageNumber,
                                                @Parameter(description = "The number of disputes returned per results page. Defaults to '500'. Maximum value is '500'")
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
