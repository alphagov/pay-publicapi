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
    public void shouldNotPrintEmailAndCardDetailsWhenToString() throws IOException {

        URI selfUri = URI.create("http://self.link.com");
        URI eventsUri = URI.create("http://self.link.com/events");
        URI cancelUri = URI.create("http://self.link.com/cancel");
        URI refundsUri = URI.create("http://self.link.com/cancel");

        ChargeFromResponse paymentFromConnector = objectMapper.readValue("{" +
                "\"email\":\"user@example.com\"," +
                "\"card_details\":{" +
                "\"card_brand\": \"Visa\"," +
                "\"expiry_date\": \"12/19\"," +
                "\"cardholder_name\": \"Mr. payment\"," +
                "\"billing_address\": {" +
                "\"line1\": \"line1\"," +
                "\"postcode\": \"NR25 6EG\"," +
                "\"country\": \"UK\"" +
                "}," +
                "\"last_digits_card_number\": \"4321\"" +
                "}," +
                "\"amount\":500," +
                "\"state\":{" +
                "\"status\":\"created\"," +
                "\"finished\":false" +
                "}}", ChargeFromResponse.class);

        Payment payment = PaymentWithAllLinks.valueOf(paymentFromConnector, selfUri, eventsUri, cancelUri, refundsUri);

        assertThat(payment.toString(), not(containsString("user@example.com")));
        assertThat(payment.toString(), not(containsString("last_digits_card_number")));
        assertThat(payment.toString(), not(containsString("4321")));
        assertThat(payment.toString(), not(containsString("12/19")));
        assertThat(payment.toString(), not(containsString("Mr. payment")));
        assertThat(payment.toString(), not(containsString("NR25 6EG")));
        assertThat(payment.toString(), not(containsString("UK")));
    }
}
