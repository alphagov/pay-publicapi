package uk.gov.pay.api.it.validation;

import org.junit.Before;
import org.junit.Test;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.api.utils.PublicAuthMockClient;

import java.util.Map;

import static io.restassured.http.ContentType.JSON;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.core.Is.is;

public class CreatePaymentWithPrefilledCardholderDetailsValidationITest extends PaymentResourceITestBase {

    private PublicAuthMockClient publicAuthMockClient = new PublicAuthMockClient(publicAuthMock);
    private JsonStringBuilder payload;

    @Before
    public void setUpBearerToken() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        payload = new JsonStringBuilder()
                .add("amount", 100)
                .add("reference", "Ref")
                .add("description", "hi")
                .add("return_url", "https://somewhere.gov.uk/rainbow/1");
    }

    @Test
    public void shouldFailOnInvalidCardHolderName() {
        payload.add("prefilled_cardholder_details", Map.of("cardholder_name", randomAlphanumeric(256)));
        postPaymentResponse(payload.build())
                .statusCode(422)
                .contentType(JSON)
                .body("size()", is(3))
                .body("field", is("cardholder_name"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: cardholder_name. Must be less than or equal to 255 characters length"));
    }

    @Test
    public void shouldFailOnInvalidAddress() {
        payload.add("prefilled_cardholder_details", Map.of("billing_address", 
                Map.of("line1", randomAlphanumeric(256))));
        postPaymentResponse(payload.build())
                .statusCode(422)
                .contentType(JSON)
                .body("size()", is(3))
                .body("field", is("line1"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: line1. Must be less than or equal to 255 characters length"));
    }

    @Test
    public void shouldCreateSuccessfullyWithEmptyCountry() {

    }
}
