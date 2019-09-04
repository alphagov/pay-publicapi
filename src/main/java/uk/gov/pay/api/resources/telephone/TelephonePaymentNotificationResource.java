package uk.gov.pay.api.resources.telephone;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.telephone.CreateTelephonePaymentRequest;
import uk.gov.pay.api.model.telephone.TelephonePaymentResponse;
import uk.gov.pay.api.service.telephone.CreateTelephonePaymentService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Api(value = "/", description = "Public Api Endpoints to receive Telephone Payments")
public class TelephonePaymentNotificationResource {

    private static final Logger logger = LoggerFactory.getLogger(TelephonePaymentNotificationResource.class);
    
    private final CreateTelephonePaymentService createTelephonePaymentService;
    
    @Inject
    public TelephonePaymentNotificationResource(CreateTelephonePaymentService createTelephonePaymentService){
        this.createTelephonePaymentService = createTelephonePaymentService;
    }
    
    
    @POST
    @Timed
    @Path("/v1/payment_notification")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response newPayment(@Auth Account account, @Valid CreateTelephonePaymentRequest createTelephonePaymentRequest) {

        TelephonePaymentResponse telephonePaymentResponse = createTelephonePaymentService.create(account, createTelephonePaymentRequest);
        
        return Response
                .status(201)
                .entity(telephonePaymentResponse)
                .build();
    }
}
