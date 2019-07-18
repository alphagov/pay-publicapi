package uk.gov.pay.api.it.validation.telephone;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.resources.telephone.TelephonePaymentResourceITBase;
import uk.gov.pay.api.utils.PublicAuthMockClient;

import java.util.Map;


public class CreateTelepehonePaymentFirstSixCardDigitsValidation extends TelephonePaymentResourceITBase {

    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);

    @Before
    public void setUpBearerToken() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

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

        postPaymentResponse(payload).statusCode(422);
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

        postPaymentResponse(payload).statusCode(422);
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

        postPaymentResponse(payload).statusCode(422);
    }

    @Test
    public void respondWith422_whenNumbersProvided() {
        String payload = toJson(Map.of("amount", 100,
                "reference", "Some reference",
                "description","hi",
                "processor_id", "1PROC",
                "provider_id", "1PROV",
                "card_type", "visa",
                "card_expiry", "01/99",
                "last_four_digits", "1234",
                "first_six_digits", 123456));

        postPaymentResponse(payload).statusCode(422);
    }
}
