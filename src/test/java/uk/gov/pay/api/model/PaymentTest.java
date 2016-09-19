package uk.gov.pay.api.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class PaymentTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldNotPrintEmailWhenToString() throws IOException {

        URI selfUri = URI.create("http://self.link.com");
        URI eventsUri = URI.create("http://self.link.com/events");
        URI cancelUri = URI.create("http://self.link.com/cancel");
        URI refundsUri = URI.create("http://self.link.com/cancel");

        ChargeFromResponse paymentFromConnector = objectMapper.readValue("{" +
                "\"email\":\"user@example.com\"," +
                "\"amount\":500," +
                "\"state\":{" +
                "\"status\":\"created\"," +
                "\"finished\":false" +
                "}}", ChargeFromResponse.class);

        Payment payment = PaymentWithAllLinks.valueOf(paymentFromConnector, selfUri, eventsUri, cancelUri, refundsUri);

        assertThat(payment.toString(), not(containsString("user@example.com")));
    }
}
