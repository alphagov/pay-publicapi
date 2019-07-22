package uk.gov.pay.api.resources.telephone;


import org.junit.Test;

import javax.ws.rs.core.Response;

import java.util.Map;

import static groovy.json.JsonOutput.toJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;


public class FirstSixCardDigitsValidationResourceTest extends ValidationResourceTest {
    
    @Test
    public void respondWith422_whenFiveDigitsProvidedOnly() {
        String payload = toJson(Map.of("amount", 100,
                "reference", "Some reference",
                "description","hi",
                "processor_id", "1PROC",
                "provider_id", "1PROV",
                "card_type", "visa",
                "card_expiry", "01/99",
                "last_four_digits", "123",
                "first_six_digits", "12345"));

        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }

    @Test
    public void respondWith422_whenSevenDigitsProvidedOnly() {
        String payload = toJson(Map.of("amount", 100,
                "reference", "Some reference",
                "description","hi",
                "processor_id", "1PROC",
                "provider_id", "1PROV",
                "card_type", "visa",
                "card_expiry", "01/99",
                "last_four_digits", "1234",
                "first_six_digits", "1234567"));

        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }
    
    @Test
    public void respondWith422_whenNullProvided() {
        String payload = "{" +
                "  \"amount\" : 100," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"hi\"," +
                "  \"processor_id\" : \"1PROC\"," +
                "  \"provider_id\" : \"1PROV\"," +
                "  \"card_type\" : \"visa\"," +
                "  \"card_expiry\" : \"01/99\"," +
                "  \"last_four_digits\" : \"1234\"," +
                "  \"first_six_digits\" : null" +
                "}";
        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }
}
