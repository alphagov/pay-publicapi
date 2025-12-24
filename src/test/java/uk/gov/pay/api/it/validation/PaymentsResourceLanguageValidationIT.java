package uk.gov.pay.api.it.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.utils.PublicAuthMockClientJUnit5;
import uk.gov.pay.api.utils.mocks.ConnectorMockClientJUnit5;

import java.util.Map;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.utils.mocks.CreateChargeRequestParams.CreateChargeRequestParamsBuilder.aCreateChargeRequestParams;

class PaymentsResourceLanguageValidationIT extends PaymentResourceITestBase {

    private final PublicAuthMockClientJUnit5 publicAuthMockClient = new PublicAuthMockClientJUnit5(publicAuthServer);
    private final ConnectorMockClientJUnit5 connectorMockClient = new ConnectorMockClientJUnit5(connectorServer);

    @BeforeEach
    void setUpBearerToken() {
        publicAuthMockClient.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID);
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "cy"})
    void valid(String language) {
        String payload = toJson(
                Map.of("amount", 100,
                        "reference", "Some ref",
                        "description", "hi",
                        "return_url", "https://somewhere.gov.uk/rainbow/1",
                        "email", "dorothy@rainbow.com",
                        "language", language));

        connectorMockClient.respondCreated_whenCreateCharge(GATEWAY_ACCOUNT_ID, aCreateChargeRequestParams()
                .withAmount(100)
                .withDescription("hi")
                .withReference("Some ref")
                .withReturnUrl("https://somewhere.gov.uk/rainbow/1")
                .build());

        postPaymentResponse(payload).statusCode(201);
    }

    @ParameterizedTest
    @CsvSource({"fr,422", "' ',400", "'',400"})
    void invalidLanguage(String language, int statusCode) {
        String payload = toJson(
                Map.of("amount", 100,
                        "reference", "Some ref",
                        "description", "hi",
                        "return_url", "https://somewhere.gov.uk/rainbow/1",
                        "email", "dorothy@rainbow.com",
                        "language", language));

        postPaymentResponse(payload)
                .statusCode(statusCode)
                .contentType(JSON)
                .body("size()", is(3))
                .body("field", is("language"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: language. Must be \"en\" or \"cy\""));
    }

    @Test
    void createPayment_responseWith400_whenLanguageIsNumeric() {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 9900,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://example.com\",\n" +
                "  \"language\": 1337\n" +
                "}";

        postPaymentResponse(payload)
                .statusCode(400)
                .contentType(JSON)
                .body("size()", is(3))
                .body("field", is("language"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: language. Must be \"en\" or \"cy\""));
    }

    @Test
    void createPayment_responseWith400_whenLanguageIsNull() {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 9900,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://example.com\",\n" +
                "  \"language\": null\n" +
                "}";

        postPaymentResponse(payload)
                .statusCode(400)
                .contentType(JSON)
                .body("size()", is(3))
                .body("field", is("language"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: language. Must be \"en\" or \"cy\""));
    }

    @Test
    void createPayment_responseWith400_whenLanguageHasNotAValidJsonValue() {
        String payload = "{" +
                "  \"amount\" : 9900," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"https://example.com\"," +
                "  \"language\" : " +
                "}";

        postPaymentResponse(payload)
                .statusCode(400)
                .contentType(JSON)
                .body("size()", is(2))
                .body("code", is("P0197"))
                .body("description", is("Unable to parse JSON"));
    }

    @Test
    void createPayment_responseWith400_whenLanguageFieldIsNotExpectedJsonField() {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 9900,\n" +
                "  \"language\": {\n" +
                "    \"whatever\": 1\n" +
                "  },\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://example.com\"\n" +
                "}";

        postPaymentResponse(payload)
                .statusCode(400)
                .contentType(JSON)
                .body("size()", is(3))
                .body("field", is("language"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: language. Must be \"en\" or \"cy\""));
    }
}
