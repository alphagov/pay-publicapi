package uk.gov.pay.api.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.pay.api.model.CreatePaymentRefundRequest;
import uk.gov.pay.api.model.CreatePaymentRequest;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static uk.gov.pay.api.json.RequestJsonParser.parsePaymentRequest;
import static uk.gov.pay.api.json.RequestJsonParser.parseRefundRequest;
import static uk.gov.pay.api.matcher.BadRequestExceptionMatcher.aBadRequestExceptionWithError;

public class RequestJsonParserTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void parsePaymentRequest_withReturnUrl_shouldParseSuccessfully() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        CreatePaymentRequest createPaymentRequest = parsePaymentRequest(jsonNode);

        assertThat(createPaymentRequest, is(notNullValue()));
        assertThat(createPaymentRequest.getAmount(), is(1000));
        assertThat(createPaymentRequest.getReference(), is("Some reference"));
        assertThat(createPaymentRequest.getDescription(), is("Some description"));
        assertThat(createPaymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
    }

    @Test
    public void parsePaymentRequest_withReturnUrlAndLanguageAndDelayedCapture_shouldParseSuccessfully() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"language\": \"en\",\n" +
                "  \"delayed_capture\": false\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        CreatePaymentRequest createPaymentRequest = parsePaymentRequest(jsonNode);

        assertThat(createPaymentRequest, is(notNullValue()));
        assertThat(createPaymentRequest.getAmount(), is(1000));
        assertThat(createPaymentRequest.getReference(), is("Some reference"));
        assertThat(createPaymentRequest.getDescription(), is("Some description"));
        assertThat(createPaymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(createPaymentRequest.getLanguage(), is("en"));
        assertThat(createPaymentRequest.getDelayedCapture(), is(false));
    }

    @Test
    public void parsePaymentRequest_withAgreementId_shouldParseSuccessfully() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"agreement_id\": \"abcdef1234567890abcedf1234\"\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        CreatePaymentRequest createPaymentRequest = parsePaymentRequest(jsonNode);

        assertThat(createPaymentRequest, is(notNullValue()));
        assertThat(createPaymentRequest.getAmount(), is(1000));
        assertThat(createPaymentRequest.getReference(), is("Some reference"));
        assertThat(createPaymentRequest.getDescription(), is("Some description"));
        assertThat(createPaymentRequest.getAgreementId(), is("abcdef1234567890abcedf1234"));
        assertThat(createPaymentRequest.getReturnUrl(), is(nullValue()));
    }

    @Test
    public void parsePaymentRefundRequest_shouldParseSuccessfully() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        CreatePaymentRefundRequest createPaymentRefundRequest = parseRefundRequest(jsonNode);

        assertThat(createPaymentRefundRequest, is(notNullValue()));
        assertThat(createPaymentRefundRequest.getAmount(), is(1000));
    }

    @Test
    public void parsePaymentRequest_whenReferenceFieldIsNotAString() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": 1234,\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: reference. Must be a valid string format"));

        parsePaymentRequest(jsonNode);
    }

    @Test
    public void parsePaymentRequest_whenDescriptionFieldIsNotAString() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": 1234,\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: description. Must be a valid string format"));

        parsePaymentRequest(jsonNode);
    }

    @Test
    public void parsePaymentRequest_whenLanguageFieldIsNotAString() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"language\": 0\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: language. Must be \"en\" or \"cy\""));

        parsePaymentRequest(jsonNode);
    }

    @Test
    public void parsePaymentRequest_whenDelayedCaptureFieldIsNotABoolean() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"delayed_capture\": \"true\"\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: delayed_capture. Must be true or false"));

        parsePaymentRequest(jsonNode);
    }

    @Test
    public void parsePaymentRequest_whenReturnUrlIsNotAString_shouldOverrideFormattingErrorMessage() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": 1234\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: return_url. Must be a valid URL format"));

        parsePaymentRequest(jsonNode);
    }

    @Test
    public void parsePaymentRequest_whenReferenceFieldIsNullValue() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": null,\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": 1234\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: reference"));

        parsePaymentRequest(jsonNode);
    }

    @Test
    public void parsePaymentRequest_whenDescriptionFieldIsNullValue() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": null,\n" +
                "  \"return_url\": 1234\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: description"));

        parsePaymentRequest(jsonNode);
    }

    @Test
    public void parsePaymentRequest_whenLanguageFieldIsNullValue() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": 1234,\n" +
                "  \"language\": null\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: language. Must be \"en\" or \"cy\""));

        parsePaymentRequest(jsonNode);
    }

    @Test
    public void parsePaymentRefundRequest_whenAmountFieldIsNullValue() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": null\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        expectedException.expect(aBadRequestExceptionWithError("P0601", "Missing mandatory attribute: amount"));

        parseRefundRequest(jsonNode);
    }

    @Test
    public void parsePaymentRequest_whenAmountFieldIsMissing() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": 1234\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: amount"));

        parsePaymentRequest(jsonNode);
    }

    @Test
    public void parsePaymentRequest_whenReferenceFieldIsMissing() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": 1234\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: reference"));

        parsePaymentRequest(jsonNode);
    }

    @Test
    public void parsePaymentRequest_whenDescriptionFieldIsMissing() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"return_url\": 1234\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: description"));

        parsePaymentRequest(jsonNode);
    }

    @Test
    public void parsePaymentRefundRequest_whenAmountFieldIsMissing() throws Exception {
        // language=JSON
        String payload = "{}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        expectedException.expect(aBadRequestExceptionWithError("P0601", "Missing mandatory attribute: amount"));

        parseRefundRequest(jsonNode);
    }

    @Test
    public void parsePaymentRequest_withNoAgreementId_whenReturnUrlIsMissing() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\"\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(payload);

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: return_url"));

        parsePaymentRequest(jsonNode);
    }
    
}
