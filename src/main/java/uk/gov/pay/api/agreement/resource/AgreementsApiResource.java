package uk.gov.pay.api.agreement.resource;

import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.agreement.model.CreateAgreementRequest;
import uk.gov.pay.api.agreement.service.AgreementService;
import uk.gov.pay.api.auth.Account;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import uk.gov.pay.api.agreement.model.Agreement;
import uk.gov.pay.api.service.LedgerService;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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
        var agreement = ledgerService.getAgreement(account, agreementCreatedResponse.getAgreementId());
        return Response.status(SC_CREATED).entity(agreement).build();
    }

    @GET
    @Path("/v1/agreements/{agreementId}")
    @Produces(APPLICATION_JSON)
    public Agreement getAgreement(
            @Parameter(hidden = true) @Auth Account account,
            @PathParam("agreementId") String agreementId
    ) {
        LOGGER.info("Get agreement {} request", agreementId);
        return ledgerService.getAgreement(account, agreementId);
    }

    @POST
    @Path("/v1/agreements/{agreementId}/cancel")
    @Consumes("application/json")
    public Response createAgreement(@Parameter(hidden = true) @Auth Account account, @PathParam("agreementId") String agreementId) {
        agreementService.cancel(account, agreementId);
        return Response.status(SC_NO_CONTENT).build();
    }

}
