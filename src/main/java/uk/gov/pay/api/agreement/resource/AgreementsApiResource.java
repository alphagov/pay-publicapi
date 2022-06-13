package uk.gov.pay.api.agreement.resource;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.agreement.model.CreateAgreementRequest;
import uk.gov.pay.api.agreement.model.ConnectorAgreementResponse;
import uk.gov.pay.api.agreement.service.AgreementService;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.service.LedgerService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_CREATED;

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
    @Produces("application/json")
    @Consumes("application/json")
    public Response createAgreement(@Parameter(hidden = true) @Auth Account account,
            @Valid CreateAgreementRequest createAgreementRequest
    ) {
        LOGGER.info("Creating new agreement for reference {} and gateway accountID {}", 
                createAgreementRequest.getReference(), account.getAccountId());
        ConnectorAgreementResponse agreementResponse = agreementService.create(account, createAgreementRequest);
       return Response.status(SC_CREATED).entity(agreementResponse).build();
    }

    @GET
    @Timed
    @Path("/v1/agreements/{agreementId}")
    @Produces(APPLICATION_JSON)
    public Response getAgreement(@Parameter(hidden = true) @Auth Account account,
                                 @PathParam("agreementId") String agreementId) {
        var agreement = ledgerService.getAgreement(account, agreementId, true);
        return Response.ok(agreement).build();
    }

    @GET
    @Timed
    @Path("/v1/agreements")
    @Produces(APPLICATION_JSON)
    public Response searchAgreement(@Parameter(hidden = true) @Auth Account account,
                                    @QueryParam("reference") String reference,
                                    @QueryParam("status") String status,
                                    @QueryParam("page") Integer page) {
        // @TODO(sfount): all of this replaced for a single explicit query params POJO
        Map<String, String> result = new HashMap<>();
        Optional.ofNullable(reference).ifPresent(ref -> result.put("reference", ref));
        Optional.ofNullable(status).ifPresent(stat -> result.put("status", stat));
        Optional.ofNullable(page).ifPresent(pg -> result.put("page", String.valueOf(page)));
        var searchResponse = ledgerService.searchAgreements(account, result);
        return Response.ok(searchResponse).build();
    }
}
