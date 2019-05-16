package uk.gov.pay.api.it.validation;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.api.utils.PublicAuthMockClient;

import java.util.Map;

import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.core.Is.is;

@RunWith(JUnitParamsRunner.class)
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
    @Parameters({
            "line1, 255", 
            "line2, 255"
    })
    public void shouldFailOnInvalidAddress(String addressField, String maxLength) {
        payload.add("prefilled_cardholder_details", Map.of("billing_address", 
                Map.of(addressField, randomAlphanumeric(256))));
        postPaymentResponse(payload.build())
                .statusCode(422)
                .contentType(JSON)
                .body("size()", is(3))
                .body("field", is(addressField))
                .body("code", is("P0102"))
                .body("description", is(format("Invalid attribute value: %s. Must be less than or equal to %s characters length", addressField, maxLength)));
    }

    @Test
    public void shouldCreateSuccessfullyWithEmptyCountry() {

    }
}
