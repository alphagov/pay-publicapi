package uk.gov.pay.api.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.api.json.JsonParser.parseInteger;
import static uk.gov.pay.api.json.JsonParser.parseString;
import static uk.gov.pay.api.matcher.BadRequestExceptionMatcher.aBadRequestExceptionContaining;
import static uk.gov.pay.api.model.PaymentError.Code.*;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class JsonParserTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void parseString_shouldParseSuccessfully() throws Exception {

        JsonNode jsonNode = objectMapper.readTree("{\"test\":\"test-value\"}");

        String value = parseString(jsonNode, "test");

        assertThat(value, is("test-value"));
    }

    @Test
    public void parseString_shouldParseSuccessfully_overrideError_shouldNotAffectOutput() throws Exception {

        JsonNode jsonNode = objectMapper.readTree("{\"test\":\"test-value\"}");

        String value = parseString(jsonNode, "test", aPaymentError(P0102, "Whatever error"));

        assertThat(value, is("test-value"));
    }

    @Test
    public void parseString_whenIsNotAString() throws Exception {

        JsonNode jsonNode = objectMapper.readTree("{\"test\":1}");

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0100, "Invalid attribute value: test. Must be a valid string format")));

        parseString(jsonNode, "test");
    }

    @Test
    public void parseString_whenIsNotAString_shouldOverrideFormattingErrorMessage() throws Exception {

        JsonNode jsonNode = objectMapper.readTree("{\"test\":1}");

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0100, "Override the error")));

        parseString(jsonNode, "test", aPaymentError(P0100, "Override the error"));
    }

    @Test
    public void parseString_whenIsNullValue() throws Exception {

        JsonNode jsonNode = objectMapper.readTree("{\"test\":null}");

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0103, "Missing mandatory attribute: test")));

        parseString(jsonNode, "test");
    }

    @Test
    public void parseString_whenIsMissing() throws Exception {

        JsonNode jsonNode = objectMapper.readTree("{\"other\":\"bla\"}");

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0103, "Missing mandatory attribute: test")));

        parseString(jsonNode, "test");
    }

    @Test
    public void parseInteger_shouldParseSuccessfully() throws Exception {

        JsonNode jsonNode = objectMapper.readTree("{\"test\":1}");

       Integer value = parseInteger(jsonNode, "test");

        assertThat(value, is(1));
    }

    @Test
    public void parseInteger_whenIsNotAnInteger() throws Exception {

        JsonNode jsonNode = objectMapper.readTree("{\"test\":\"1\"}");

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0100, "Invalid attribute value: test. Must be a valid numeric format")));

        parseInteger(jsonNode, "test");
    }

    @Test
    public void parseInteger_whenIsNullValue() throws Exception {

        JsonNode jsonNode = objectMapper.readTree("{\"test\":null}");

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0103, "Missing mandatory attribute: test")));

        parseInteger(jsonNode, "test");
    }

    @Test
    public void parseInteger_whenIsMissing() throws Exception {

        JsonNode jsonNode = objectMapper.readTree("{\"other\":\"bla\"}");

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0103, "Missing mandatory attribute: test")));

        parseInteger(jsonNode, "test");
    }
}
