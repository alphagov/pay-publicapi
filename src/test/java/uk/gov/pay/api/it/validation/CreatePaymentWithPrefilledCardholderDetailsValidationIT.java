package uk.gov.pay.api.it.validation;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.api.utils.PublicAuthMockClientJUnit5;
import uk.gov.pay.api.utils.mocks.ConnectorMockClientJUnit5;

import java.util.Map;

import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.utils.mocks.CreateChargeRequestParams.CreateChargeRequestParamsBuilder.aCreateChargeRequestParams;

class CreatePaymentWithPrefilledCardholderDetailsValidationIT extends PaymentResourceITestBase {

    private final PublicAuthMockClientJUnit5 publicAuthMockClient = new PublicAuthMockClientJUnit5(publicAuthServer);
    private final ConnectorMockClientJUnit5 connectorMockClient = new ConnectorMockClientJUnit5(connectorServer);
    private JsonStringBuilder payload;

    @BeforeEach
    void setUpBearerToken() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
        payload = new JsonStringBuilder()
                .add("amount", 100)
                .add("reference", "Ref")
                .add("description", "hi")
                .add("return_url", "https://somewhere.gov.uk/rainbow/1");
    }

    @Test
    void shouldFailOnInvalidCardHolderName() {
        payload.add("prefilled_cardholder_details", Map.of("cardholder_name", RandomStringUtils.insecure().nextAlphanumeric(256)));
        postPaymentResponse(payload.build())
                .statusCode(422)
                .contentType(JSON)
                .body("size()", is(3))
                .body("field", is("cardholder_name"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: cardholder_name. Must be less than or equal to 255 characters length"));
    }

    @ParameterizedTest
    @CsvSource({
            "line1, Must be less than or equal to 255 characters length",
            "line2, Must be less than or equal to 255 characters length",
            "city, Must be less than or equal to 255 characters length",
            "postcode, Must be less than or equal to 25 characters length"
    })
    void shouldFailOnInvalidAddress(String addressField, String message) {
        payload.add("prefilled_cardholder_details", Map.of("billing_address",
                Map.of(addressField, RandomStringUtils.insecure().nextAlphanumeric(256))));
        postPaymentResponse(payload.build())
                .statusCode(422)
                .contentType(JSON)
                .body("size()", is(3))
                .body("field", is(addressField))
                .body("code", is("P0102"))
                .body("description", is(format("Invalid attribute value: %s. %s", addressField, message)));
    }

    @Test
    void shouldCreateSuccessfullyWithEmptyCountry() {
        payload.add("prefilled_cardholder_details", Map.of("billing_address",
                Map.of("country", "")));

        connectorMockClient.respondCreated_whenCreateCharge(GATEWAY_ACCOUNT_ID, aCreateChargeRequestParams()
                .withAmount(100)
                .withDescription("hi")
                .withReference("Ref")
                .withReturnUrl("https://somewhere.gov.uk/rainbow/1")
                .build());

        postPaymentResponse(payload.build()).statusCode(201);
    }
}
