package uk.gov.pay.api.agreement.resource;

import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.agreement.model.AgreementResponse;
import uk.gov.pay.api.agreement.model.CreateAgreementRequest;
import uk.gov.pay.api.agreement.service.AgreementService;
import uk.gov.pay.api.auth.Account;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;

@Path("/")
public class AgreementsApiResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementsApiResource.class);

    private final AgreementService agreementService;

    @Inject
    public AgreementsApiResource(AgreementService agreementService) {
        this.agreementService = agreementService;
    }

    @POST
    @Path("/v1/agreements")
    @Produces("application/json")
    @Consumes("application/json")
    public Response createAgreement(@Parameter(hidden = true) @Auth Account account,
            @Valid CreateAgreementRequest createAgreementRequest) {
        LOGGER.info("Creating new agreement for reference {} and gateway accountID {}", 
                createAgreementRequest.getReference(), account.getAccountId());
        AgreementResponse agreementResponse = agreementService.create(account, createAgreementRequest);
       return Response.status(SC_CREATED).entity(agreementResponse).build();
    }

    @POST
    @Path("/v1/agreements/{agreementId}/cancel")
    @Consumes("application/json")
    public Response createAgreement(@Parameter(hidden = true) @Auth Account account, @PathParam("agreementId") String agreementId) {
        agreementService.cancel(account, agreementId);
        return Response.status(SC_NO_CONTENT).build();
    }

}
