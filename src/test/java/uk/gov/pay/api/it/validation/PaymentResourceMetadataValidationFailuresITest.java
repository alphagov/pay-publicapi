package uk.gov.pay.api.it.validation;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.ValidatableResponse;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.pay.api.it.PaymentResourceITestBase;
import uk.gov.pay.api.utils.JsonStringBuilder;
import uk.gov.pay.api.utils.mocks.CreateChargeRequestParams;
import uk.gov.pay.api.utils.mocks.CreateChargeRequestParams.CreateChargeRequestParamsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.pay.api.it.CreatePaymentITest.paymentPayload;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;
import static uk.gov.pay.api.utils.mocks.CreateChargeRequestParams.CreateChargeRequestParamsBuilder.aCreateChargeRequestParams;

@RunWith(JUnitParamsRunner.class)
public class PaymentResourceMetadataValidationFailuresITest extends PaymentResourceITestBase {

    private static CreateChargeRequestParamsBuilder createChargeRequestParamsBuilder = aCreateChargeRequestParams()
                .withAmount(100)
                .withDescription("DESCRIPTION")
                .withReference("REFERENCE")
                .withReturnUrl("https://somewhere.gov.uk/rainbow/1");
    
    @Before
    public void before() {
        publicAuthMock.mapBearerTokenToAccountId(API_KEY, GATEWAY_ACCOUNT_ID, CARD);
    }

    @Test
    public void valueIsNotAStringBooleanOrNumber() {

        CreateChargeRequestParams createChargeRequestParams = createChargeRequestParamsBuilder
                .withMetadata(Map.of("foo", List.of("cake", "chocolate"), "bar", Map.of("a", "b")))
                .build();

        assertMetadataValidationError(createChargeRequestParams, 
                "Invalid attribute value: metadata. Values must be of type String, Boolean or Number");
    }

    @Test
    @Parameters({"", " ", "keyMoreThanThirtyCharskeyMoreThanThirtyCharskeyMoreThanThirtyChars"})
    public void keyIsInvalid(String key) {
        
        var createChargeRequestParams = createChargeRequestParamsBuilder.withMetadata(Map.of(key, "boo")).build();

        assertMetadataValidationError(createChargeRequestParams, 
                "Invalid attribute value: metadata. Keys must be between 1 and 30 characters long");
    }
    
    @Test
    @Parameters({
            "null, Must not have null values", 
            "valueMoreThanFiftyCharsvalueMoreThanFiftyCharsvalueMoreThanFiftyCharsvalueMoreThanFiftyChars, Values must be no greater than 50 characters long"})
    public void valueIsInvalid(@Nullable String value, String expectedMessage) {
        
        Map<String, Object> metadata = new HashMap<>() {{ put("key", value); }};
        var createChargeRequestParams = createChargeRequestParamsBuilder.withMetadata(metadata).build();
        assertMetadataValidationError(createChargeRequestParams, "Invalid attribute value: metadata. " + expectedMessage);
    }

    @Test
    public void moreThan10Keys() {
        
        Map<String, Object> metadata = new HashMap<>() {{
            put("reconciled", true);
            put("ledger_code", 123);
            put("fuh", "fuh you");
            put("reconciled1", true);
            put("ledger_code1", 123);
            put("fuh1", "fuh you");
            put("reconciled2", true);
            put("ledger_code2", 123);
            put("fuh2", "fuh you");
            put("reconciled3", true);
            put("ledger_code3", 123);
            put("fuh3", "fuh you");
        }};

        var createChargeRequestParams = createChargeRequestParamsBuilder.withMetadata(metadata).build();
        
        assertMetadataValidationError(createChargeRequestParams, 
                "Invalid attribute value: metadata. Cannot have more than 10 key-value pairs");
    }
    
    @Test
    public void metadataIsNotAnObject() {
        
        var createChargeRequestParams = createChargeRequestParamsBuilder.build();
        
        JsonStringBuilder payload = new JsonStringBuilder()
                .add("amount", createChargeRequestParams.getAmount())
                .add("reference", createChargeRequestParams.getReference())
                .add("description", createChargeRequestParams.getDescription())
                .add("return_url", createChargeRequestParams.getReturnUrl())
                .add("metadata", "something");

        postPaymentResponse(payload.build())
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("field", is("metadata"))
                .body("code", is("P0102"))
                .body("description", is("Invalid attribute value: metadata. Must be an object of JSON key-value pairs"));
    }

    @Test
    public void testMultipleValidationErrors() {

        var metadata = Map.of("key", "valueMoreThanFiftyCharsvalueMoreThanFiftyCharsvalueMoreThanFiftyCharsvalueMoreThanFiftyChars",
                "keyMoreThanThirtyCharskeyMoreThanThirtyCharskeyMoreThanThirtyChars", "fuh",
                "badKey", List.of("cake", "chocolate"));

        var createChargeRequestParams = createChargeRequestParamsBuilder.withMetadata(metadata).build();

        JsonNode jsonBody = postPaymentResponse(paymentPayload(createChargeRequestParams))
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("field", is("metadata"))
                .body("code", is("P0102"))
                .extract().body().as(JsonNode.class);

        var descriptions = asList(jsonBody.get("description").asText()
                .replace("Invalid attribute value: metadata. ", "")
                .split("\\. "));
        assertThat(descriptions).containsExactlyInAnyOrder(
                "Values must be no greater than 50 characters long",
                "Keys must be between 1 and 30 characters long",
                "Values must be of type String, Boolean or Number"
        );
    }

    private ValidatableResponse postPaymentResponse(String payload) {
        return given().port(app.getLocalPort())
                .body(payload)
                .accept(JSON)
                .contentType(JSON)
                .header(AUTHORIZATION, "Bearer " + PaymentResourceITestBase.API_KEY)
                .post(PAYMENTS_PATH)
                .then();
    }

    private void assertMetadataValidationError(CreateChargeRequestParams createChargeRequestParams, String message) {
        postPaymentResponse(paymentPayload(createChargeRequestParams))
                .statusCode(HttpStatus.SC_UNPROCESSABLE_ENTITY)
                .contentType(JSON)
                .body("field", is("metadata"))
                .body("code", is("P0102"))
                .body("description", is(message));
    }
}
