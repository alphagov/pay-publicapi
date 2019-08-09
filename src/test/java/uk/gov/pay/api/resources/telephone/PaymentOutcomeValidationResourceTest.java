package uk.gov.pay.api.resources.telephone;

import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static groovy.json.JsonOutput.toJson;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PaymentOutcomeValidationResourceTest extends ValidationResourceTest {
    
    @Test
    public void respondWith422_whenStatusIsOtherThanSuccessOrFailed() {

        String payload = toJson(Map.of("amount", 100,
                "reference", "Some reference",
                "description","hi",
                "processor_id", "1PROC",
                "provider_id", "1PROV",
                "payment_outcome", Map.of("status", "other"),
                "card_type", "visa",
                "card_expiry", "00/99",
                "last_four_digits", "1234",
                "first_six_digits", "123456"));
        
        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }

    @Test
    public void respondWith422_whenStatusIsFailedButNoErrorCodeProvided() {

        String payload = toJson(Map.of("amount", 100,
                "reference", "Some reference",
                "description","hi",
                "processor_id", "1PROC",
                "provider_id", "1PROV",
                "payment_outcome", Map.of(
                        "status", "failed"
                ),
                "card_type", "visa",
                "card_expiry", "00/99",
                "last_four_digits", "1234",
                "first_six_digits", "123456"));

        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }
    
    @Test
    public void respondWith422_whenStatusIsFailedButErrorCodeNotRecognized() {

        String payload = toJson(Map.of("amount", 100,
                "reference", "Some reference",
                "description","hi",
                "processor_id", "1PROC",
                "provider_id", "1PROV",
                "payment_outcome", Map.of(
                        "status", "failed", 
                        "code", "unknown_error"
                ),
                "card_type", "visa",
                "card_expiry", "99/99",
                "last_four_digits", "1234",
                "first_six_digits", "123456"));

        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }

    @Test
    public void respondWith422_whenPaymentOutcomeIsNull() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("amount", 100);
        map.put("reference", "Some reference");
        map.put("description", "hi");
        map.put("processor_id", "1PROC");
        map.put("provider_id", "1PROV");
        map.put("payment_outcome", null);
        map.put("card_type", "visa");
        map.put("card_expiry", "01/08");
        map.put("last_four_digits", "1234");
        map.put("first_six_digits", "123456");

        String payload = toJson(map);

        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }

    @Test
    public void respondWith422_whenPaymentOutcomeIsMissing() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("amount", 100);
        map.put("reference", "Some reference");
        map.put("description", "hi");
        map.put("processor_id", "1PROC");
        map.put("provider_id", "1PROV");
        map.put("card_type", "visa");
        map.put("card_expiry", "01/08");
        map.put("last_four_digits", "1234");
        map.put("first_six_digits", "123456");

        String payload = toJson(map);

        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }

    @Test
    public void respondWith422_whenPaymentOutcomeStatusIsMissing() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("amount", 100);
        map.put("reference", "Some reference");
        map.put("description", "hi");
        map.put("processor_id", "1PROC");
        map.put("provider_id", "1PROV");
        map.put("payment_outcome", Map.of(
        ));
        map.put("card_type", "visa");
        map.put("card_expiry", "01/08");
        map.put("last_four_digits", "1234");
        map.put("first_six_digits", "123456");

        String payload = toJson(map);

        Response response = sendPayload(payload);
        assertThat(response.getStatus(), is(422));
    }
}
