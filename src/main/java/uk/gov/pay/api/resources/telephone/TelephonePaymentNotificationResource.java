package uk.gov.pay.api.resources.telephone;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.telephone.CreateTelephonePaymentRequest;
import uk.gov.pay.api.model.telephone.TelephonePaymentResponse;
import uk.gov.pay.api.service.telephone.CreateTelephonePaymentService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class TelephonePaymentNotificationResource {

    private static final Logger logger = LoggerFactory.getLogger(TelephonePaymentNotificationResource.class);

    private final CreateTelephonePaymentService createTelephonePaymentService;

    @Inject
    public TelephonePaymentNotificationResource(CreateTelephonePaymentService createTelephonePaymentService) {
        this.createTelephonePaymentService = createTelephonePaymentService;
    }


    @POST
    @Timed
    @Path("/v1/payment_notification")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response newPayment(@Auth Account account, @Valid CreateTelephonePaymentRequest createTelephonePaymentRequest) {
        Pair<TelephonePaymentResponse, Integer> responseAndStatusCode = createTelephonePaymentService.create(account, createTelephonePaymentRequest);
        var response = responseAndStatusCode.getLeft();
        var statusCode = responseAndStatusCode.getRight();

        return Response.status(statusCode).entity(response).build();
    }
}

