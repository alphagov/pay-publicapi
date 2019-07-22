package uk.gov.pay.api.it.telephone;



import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.resources.telephone.TelephonePaymentResourceITBase;
import uk.gov.pay.api.utils.PublicAuthMockClient;

import java.util.Map;

public class GetTelephonePaymentIT extends TelephonePaymentResourceITBase {
    
    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);

    @Before
    public void setUpBearerToken() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @Test
    public void respondWith201_whenRequiredFieldsAreValid() {
        String payload = toJson(Map.of("amount", 100,
                "reference", "Some reference",
                "description","hi",
                "processor_id", "1PROC",
                "provider_id", "1PROV",
                "card_type", "visa",
                "card_expiry", "01/99",
                "last_four_digits", "1234",
                "first_six_digits", "123456"));
        
        postPaymentResponse(payload).statusCode(201);
    }
}
