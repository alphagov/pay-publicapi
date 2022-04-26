package uk.gov.pay.api.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.AuthorisationAPIRequest;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Produces({"application/json"})
public class AuthorisationAPIResource {

    private static final Logger logger = LoggerFactory.getLogger(AuthorisationAPIResource.class);

    public AuthorisationAPIResource() {
    }

    @POST
    @Timed
    @Path("/v1/auth")
    @Produces(APPLICATION_JSON)
    public Response authorisePayment(@Parameter(hidden = true) @Auth Account account,
                                     @Parameter(required = true)
                                     @Valid AuthorisationAPIRequest authorisationRequest) {

        logger.info("Payment authorised");
        return Response.noContent().build();
    }
}
