package uk.gov.pay.api.resources;


import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.pay.api.resources.telephone.TelephonePaymentNotificationResource;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class TelephonePaymentNotificationResourceTest {
    
    @ClassRule
    public static final ResourceTestRule telephonePaymentNotificationResource = ResourceTestRule.builder()
            .addResource(new TelephonePaymentNotificationResource())
            .build();
    
    
    @Test
    public void returnsOK() {
        Response response = telephonePaymentNotificationResource.target("/v1/payment_notification").request().post(Entity.json(null));
        assertThat(response.getStatus(), is(200));
    }
    
    
}
