package uk.gov.pay.api.resources.telephone;

import org.junit.Test;
import uk.gov.pay.api.utils.JsonStringBuilder;

import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

import static groovy.json.JsonOutput.toJson;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class CardExpiryValidationResourceTest extends ValidationResourceTest {
    
    @Test
    public void respondWith422_whenMonthIs00() {

        String payload = toJson(Map.of("amount", 100,
                "reference", "Some reference",
                "description","hi",
                "processor_id", "1PROC",
                "provider_id", "1PROV",
                "payment_outcome", Map.of("status", "success"),
                "card_type", "visa",
                "card_expiry", "00/99",
                "last_four_digits", "1234",
                "first_six_digits", "123456"));
        
        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }

    @Test
    public void respondWith422_whenMonthIsInvalid() {

        String payload = toJson(Map.of("amount", 100,
                "reference", "Some reference",
                "description","hi",
                "processor_id", "1PROC",
                "provider_id", "1PROV",
                "payment_outcome", Map.of("status", "success"),
                "card_type", "visa",
                "card_expiry", "99/99",
                "last_four_digits", "1234",
                "first_six_digits", "123456"));

        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }

    @Test
    public void respondWith422_whenCardExpiryIsNull() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("amount", 100);
        map.put("reference", "Some reference");
        map.put("description", "hi");
        map.put("processor_id", "1PROC");
        map.put("provider_id", "1PROV");
        map.put("payment_outcome", Map.of("status", "success"));
        map.put("card_type", "visa");
        map.put("card_expiry", null);
        map.put("last_four_digits", "1234");
        map.put("first_six_digits", "123456");

        String payload = toJson(map);

        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }
}