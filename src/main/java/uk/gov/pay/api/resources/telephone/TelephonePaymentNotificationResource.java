package uk.gov.pay.api.resources.telephone;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.model.telephone.CreateTelephonePayment;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
@Api(value = "/", description = "Public Api Endpoints to receive Telephone Payments")
public class TelephonePaymentNotificationResource {

    private static final Logger logger = LoggerFactory.getLogger(TelephonePaymentNotificationResource.class);
    
    @POST
    @Timed
    @Path("/v1/payment_notification")
    @Consumes(APPLICATION_JSON)
    public Response newPayment(CreateTelephonePayment createTelephonePayment) {
        return Response.status(201).build();
    }
}
