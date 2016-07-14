package uk.gov.pay.api.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static uk.gov.pay.api.model.PaymentError.Code.REQUEST_DENIED_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

@Path("/")
public class RequestDeniedResource {

    private static final Logger logger = LoggerFactory.getLogger(RequestDeniedResource.class);

    @POST
    @Path("request-denied")
    @Produces(APPLICATION_JSON)
    public Response requestDenied(@HeaderParam("x-naxsi_sig") String naxsiViolatedRules) {
        logger.info("Naxsi rules violated - [ {} ]", naxsiViolatedRules);
        return Response.status(BAD_REQUEST).entity(aPaymentError(REQUEST_DENIED_ERROR)).build();
    }
}
