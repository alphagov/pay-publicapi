package uk.gov.pay.api.agreement.resource;

import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.agreement.model.CreateAgreementRequest;
import uk.gov.pay.api.agreement.model.AgreementResponse;
import uk.gov.pay.api.agreement.service.AgreementService;
import uk.gov.pay.api.auth.Account;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Response;

@Path("/")
public class AgreementsApiResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgreementsApiResource.class);

    private final AgreementService agreementService;

    @Inject
    public AgreementsApiResource(AgreementService agreementService) {
        this.agreementService = agreementService;
    }

    @POST
    @Path("/v1/api/accounts/{accountId}/agreements")
    @Produces("application/json")
    @Consumes("application/json")
    public Response createAgreement(@Parameter(hidden = true) @Auth Account account,
            @Valid CreateAgreementRequest agreementCreateRequest
    ) {
        LOGGER.info("Creating new agreement for reference  {}", agreementCreateRequest.getReference());
        AgreementResponse a = agreementService.create(account, agreementCreateRequest);

        Response b = Response.status(201).entity(a).build();

       return b;
    }


   
}
