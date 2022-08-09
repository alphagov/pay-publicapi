package uk.gov.pay.api.agreement.resource;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.agreement.model.Agreement;
import uk.gov.pay.api.agreement.model.AgreementLedgerResponse;
import uk.gov.pay.api.agreement.model.CreateAgreementRequest;
import uk.gov.pay.api.agreement.service.AgreementService;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.ledger.model.AgreementSearchParams;
import uk.gov.pay.api.ledger.model.SearchResults;
import uk.gov.pay.api.service.LedgerService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;

@Path("/")
public class AgreementsApiResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementsApiResource.class);

    private final AgreementService agreementService;
    private final LedgerService ledgerService;

    @Inject
    public AgreementsApiResource(AgreementService agreementService, LedgerService ledgerService) {
        this.agreementService = agreementService;
        this.ledgerService = ledgerService;
    }

    @POST
    @Path("/v1/agreements")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response createAgreement(
            @Parameter(hidden = true) @Auth Account account,
            @Valid CreateAgreementRequest createAgreementRequest
    ) {
        LOGGER.info("Creating new agreement for reference {} and gateway accountID {}", 
                createAgreementRequest.getReference(), account.getAccountId());
        var agreementCreatedResponse = agreementService.create(account, createAgreementRequest);
        var agreementLedgerResponse = ledgerService.getAgreement(account, agreementCreatedResponse.getAgreementId());
        return Response.status(SC_CREATED).entity(Agreement.from(agreementLedgerResponse)).build();
    }

    @GET
    @Path("/v1/agreements/{agreementId}")
    @Produces(APPLICATION_JSON)
    public Agreement getAgreement(
            @Parameter(hidden = true) @Auth Account account,
            @PathParam("agreementId") String agreementId
    ) {
        LOGGER.info("Get agreement {} request", agreementId);
        var agreementLedgerResponse = ledgerService.getAgreement(account, agreementId);
        return Agreement.from(agreementLedgerResponse);
    }

    @GET
    @Timed
    @Path("/v1/agreements")
    @Produces(APPLICATION_JSON)
    public SearchResults<Agreement> getAgreements(@Auth Account account, @BeanParam AgreementSearchParams searchParams) {
        SearchResults<AgreementLedgerResponse> searchResults = ledgerService.searchAgreements(account, searchParams);
        return new SearchResults<>(searchResults.getTotal(), searchResults.getCount(),
                searchResults.getPage(), searchResults.getResults().stream().map(Agreement::from).collect(Collectors.toUnmodifiableList()), null);
    }

    @POST
    @Path("/v1/agreements/{agreementId}/cancel")
    @Produces(APPLICATION_JSON)
    public Response createAgreement(@Parameter(hidden = true) @Auth Account account, @PathParam("agreementId") String agreementId) {
        agreementService.cancel(account, agreementId);
        return Response.status(SC_NO_CONTENT).build();
    }

}
