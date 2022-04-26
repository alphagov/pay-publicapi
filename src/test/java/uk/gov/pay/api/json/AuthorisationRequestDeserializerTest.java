package uk.gov.pay.api.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.pay.api.exception.BadAuthorisationRequestException;
import uk.gov.pay.api.model.AuthorisationRequest;
import uk.gov.pay.api.model.RequestError;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;

class AuthorisationRequestDeserializerTest {

    @Mock
    private DeserializationContext ctx;
    private JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());
    private AuthorisationRequestDeserializer deserializer;

    @BeforeEach
    public void setup() {
        deserializer = new AuthorisationRequestDeserializer();
    }

    @Test
    void deserialize_shouldDeserializeARequestSuccessfully() throws Exception {
        String validJson = "{\n" +
                "  \"one_time_token\": \"27432234tyu-4567\",\n" +
                "  \"card_number\": \"12345678901234\",\n" +
                "  \"cvc\": \"123\",\n" +
                "  \"expiry_date\": \"09/28\",\n" +
                "  \"cardholder_name\": \"Joe Boggs\"\n" +
                "}";
        AuthorisationRequest request = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(request.getOneTimeToken(), is("27432234tyu-4567"));
        assertThat(request.getCardNumber(), is("12345678901234"));
        assertThat(request.getCvc(), is("123"));
        assertThat(request.getExpiryDate(), is("09/28"));
        assertThat(request.getCardholderName(), is("Joe Boggs"));
    }

    @Test
    void deserialize_shouldThrowBadAuthorisationAPIRequestException_AsExpiryDateMissing_whenExpiryDateIsNullValue() throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"one_time_token\": \"27432234tyu-4567\",\n" +
                "  \"card_number\": \"12345678901234\",\n" +
                "  \"cvc\": \"123\",\n" +
                "  \"expiry_date\": null,\n" +
                "  \"cardholder_name\": \"Joe Boggs\"\n" +
                "}";

        BadAuthorisationRequestException badRequestException = assertThrows(BadAuthorisationRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(validJson), ctx));

        RequestError error = badRequestException.getRequestError();

        assertThat(error.getCode(), is("P1201"));
        assertThat(error.getDescription(), is("Missing mandatory attribute: expiry_date"));
        assertThat(error.getField(), is("expiry_date"));
    }

    @Test
    void deserialize_shouldThrowBadAuthorisationAPIRequestException_whenCVCIsFiveCharLong() {
        String validJson = "{\n" +
                "  \"one_time_token\": \"27432234tyu-4567\",\n" +
                "  \"card_number\": \"12345678901234\",\n" +
                "  \"cvc\": \"12345\",\n" +
                "  \"expiry_date\": \"09/28\",\n" +
                "  \"cardholder_name\": \"Joe Boggs\"\n" +
                "}";

        BadAuthorisationRequestException badRequestException = assertThrows(BadAuthorisationRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(validJson), ctx));

        RequestError error = badRequestException.getRequestError();

        assertThat(error.getCode(), is("P1202"));
        assertThat(error.getDescription(), is("Invalid attribute value: cvc. Must be between 3 and 4 characters long"));
        assertThat(error.getField(), is("cvc"));
    }

    @Test
    void deserialize_shouldThrowBadAuthorisationAPIRequestException_whenCardholderNameIsMissing() {
        String validJson = "{\n" +
                "  \"one_time_token\": \"27432234tyu-4567\",\n" +
                "  \"card_number\": \"12345678901234\",\n" +
                "  \"cvc\": \"123\",\n" +
                "  \"expiry_date\": \"09/28\"\n" +
                "}";

        BadAuthorisationRequestException badRequestException = assertThrows(BadAuthorisationRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(validJson), ctx));

        RequestError error = badRequestException.getRequestError();

        assertThat(error.getCode(), is("P1201"));
        assertThat(error.getDescription(), is("Missing mandatory attribute: cardholder_name"));
        assertThat(error.getField(), is("cardholder_name"));
    }

    @Test
    void deserialize_shouldThrowBadAuthorisationAPIRequestException_whenCardNumberIsNumeric() {
        String validJson = "{\n" +
                "  \"one_time_token\": \"27432234tyu-4567\",\n" +
                "  \"card_number\": 12345678901234,\n" +
                "  \"cvc\": \"123\",\n" +
                "  \"expiry_date\": \"09/28\",\n" +
                "  \"cardholder_name\": \"Joe Boggs\"\n" +
                "}";

        BadAuthorisationRequestException badRequestException = assertThrows(BadAuthorisationRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(validJson), ctx));

        RequestError error = badRequestException.getRequestError();

        assertThat(error.getCode(), is("P1202"));
        assertThat(error.getDescription(), is("Invalid attribute value: card_number. Must be a string"));
        assertThat(error.getField(), is("card_number"));
    }

    @Test
    void deserialize_shouldThrowBadAuthorisationAPIRequestException_whenCardholderNameIsTooLong() {
        String cardholderName = "Joe J. Boggs".repeat(24);
        String validJson = "{\n" +
                "  \"one_time_token\": \"27432234tyu-4567\",\n" +
                "  \"card_number\": \"12345678901234\",\n" +
                "  \"cvc\": \"123\",\n" +
                "  \"expiry_date\": \"09/28\",\n" +
                "  \"cardholder_name\": \"Joe Boggs\"\n" +
                "}";

        String invalidCardholderName = validJson.replace("Joe Boggs", cardholderName);

        BadAuthorisationRequestException badRequestException = assertThrows(BadAuthorisationRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(invalidCardholderName), ctx));

        RequestError error = badRequestException.getRequestError();

        assertThat(error.getCode(), is("P1202"));
        assertThat(error.getDescription(), is("Invalid attribute value: cardholder_name. Must be less than or equal to 255 characters length"));
        assertThat(error.getField(), is("cardholder_name"));
    }

    @Test
    void deserialize_shouldThrowBadAuthorisationAPIRequestException_whenJsonIsUnprocessable() {
        String invalidJson = "{\n" +
                "  \"one_time_token\": \"27432234tyu-4567\",\n" +
                "  \"card_number\": \"12345678901234\",\n" +
                "  \"cvc\": \"123\",\n" +
                "  \"expiry_date\": \"09/28\",\n" +
                "  \"cardholder_name\": \"Joe Boggs\",\n" +
                "}";

        BadAuthorisationRequestException badRequestException = assertThrows(BadAuthorisationRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(invalidJson), ctx));

        RequestError error = badRequestException.getRequestError();

        assertThat(error.getCode(), is("P1297"));
        assertThat(error.getDescription(), is("Unable to parse JSON"));
        assertThat(error.getField(), is(nullValue()));
    }

    @Test
    void deserialize_shouldThrowBadAuthorisationAPIRequestException_AsExpiryDateMissing_whenExpiryDateIsZeroLength() throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"one_time_token\": \"27432234tyu-4567\",\n" +
                "  \"card_number\": \"12345678901234\",\n" +
                "  \"cvc\": \"123\",\n" +
                "  \"expiry_date\": \"\",\n" +
                "  \"cardholder_name\": \"Joe Boggs\"\n" +
                "}";

        BadAuthorisationRequestException badRequestException = assertThrows(BadAuthorisationRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(validJson), ctx));

        RequestError error = badRequestException.getRequestError();

        assertThat(error.getCode(), is("P1201"));
        assertThat(error.getDescription(), is("Missing mandatory attribute: expiry_date"));
        assertThat(error.getField(), is("expiry_date"));
    }
}
