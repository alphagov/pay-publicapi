package uk.gov.pay.api.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.validation.URLValidator;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.util.Optional;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.pay.api.matcher.BadRequestExceptionMatcher.aBadRequestExceptionWithError;

@RunWith(MockitoJUnitRunner.class)
public class CreatePaymentRequestDeserializerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private DeserializationContext ctx;

    private JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());
    private CreatePaymentRequestDeserializer deserializer;

    @Before
    public void setup() {
        URLValidator urlValidator = URLValidator.urlValidatorValueOf(true);
        deserializer = new CreatePaymentRequestDeserializer();
    }

    @Test
    public void deserialize_shouldDeserializeARequestWithReturnUrlSuccessfully() throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"amount\": 27432,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        CreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage(), is(nullValue()));
        assertThat(paymentRequest.getDelayedCapture(), is(nullValue()));
        assertThat(paymentRequest.getEmail(), is(nullValue()));
        assertThat(paymentRequest.getPrefilledCardholderDetails(), is(nullValue()));
    }

    @Test
    public void deserialize_shouldDeserializeARequestWithEnglishLanguageSuccessfully() throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"amount\": 27432,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"language\": \"en\"\n" +
                "}";

        CreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage(), is(SupportedLanguage.ENGLISH));
    }

    @Test
    public void deserialize_shouldDeserializeARequestWithWelshLanguageSuccessfully() throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"amount\": 27432,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"language\": \"cy\"\n" +
                "}";

        CreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage(), is(SupportedLanguage.WELSH));
    }

    @Test
    public void deserialize_shouldDeserializeARequestWithDelayedCaptureEqualsTrueSuccessfully() throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"amount\": 27432,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"delayed_capture\": true\n" +
                "}";

        CreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getDelayedCapture(), is(Boolean.TRUE));
    }

    @Test
    public void deserialize_shouldDeserializeARequestWithDelayedCaptureEqualsFalseSuccessfully() throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"amount\": 27432,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"delayed_capture\": false\n" +
                "}";

        CreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getDelayedCapture(), is(Boolean.FALSE));
    }

    @Test
    public void deserialize_shouldDeserializeARequestWithAnAgreementIdSuccessfully() throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"agreement_id\": \"abc123\",\n" +
                "  \"amount\": 27432,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\"\n" +
                "}";

        CreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is(nullValue()));
        assertThat(paymentRequest.getAgreementId(), is("abc123"));
    }

    @Test
    public void deserialize_shouldThrowBadRequestException_whenJsonIsNotWellFormed() throws Exception {
        String invalidJson = "{" +
                "  \"amount\" : " +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"https://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(BadRequestException.class);
        expectedException.expect(aBadRequestExceptionWithError("P0197", "Unable to parse JSON"));

        deserializer.deserialize(jsonFactory.createParser(invalidJson), ctx);
    }

    @Test
    public void deserialize_shouldThrowBadRequestException_whenAmountIsMissing() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: amount"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_asAmountIsMissing_whenAmountIsNullValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": null,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: amount"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenAmountIsNotInteger() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": \"\",\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: amount. Must be a valid numeric format"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReturnUrlIsNotAStringValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1000000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": 1\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: return_url. Must be a valid URL format"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReturnUrlIsMissing() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: return_url"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_AsReturnUrlIsMissing_whenReturnUrlIsNullValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": null\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: return_url"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReferenceIsMissing() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: reference"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_AsReferenceIsMissing_whenReferenceIsNullValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": null,\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: reference"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReferenceIsNotAString() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": 123,\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: reference. Must be a valid string format"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDescriptionIsMissing() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: description"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_asDescriptionIsMissing_whenDescriptionIsNullValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": null,\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: description"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDescriptionIsNotAString() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": 432,\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: description. Must be a valid string format"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_AsAgreementIdIsMissing_whenAgreementIdIsNullValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"agreement_id\": null\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: agreement_id"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenAgreementIdIsNotAString() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"agreement_id\": 1234\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: agreement_id. Must be a valid agreement ID"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenLanguageIsNotAString() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1337,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"language\": 1234\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: language. Must be \"en\" or \"cy\""));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenLanguageIsNullValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1337,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"language\": null\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: language. Must be \"en\" or \"cy\""));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenLanguageIsEmptyString() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1337,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"language\": \"\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: language. Must be \"en\" or \"cy\""));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDelayedCaptureIsNotABoolean() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1337,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"delayed_capture\": \"true\"\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: delayed_capture. Must be true or false"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDelayedCaptureIsNullValue() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1337,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"delayed_capture\": null\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: delayed_capture. Must be true or false"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDelayedCaptureIsNumeric() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1337,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"delayed_capture\": 0\n" +
                "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: delayed_capture. Must be true or false"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }
    
    @Test
    public void shouldDeserializeARequestWithPrefilledCardholderDetailsSuccessfully() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "\"email\": \"j.bogs@example.org\",\n" +
                "\"prefilled_cardholder_details\": {\n" +
                "\"cardholder_name\": \"J Bogs\",\n" +
                "\"billing_address\": {\n" +
                "\"line1\": \"address line 1\",\n" +
                "\"line2\": null,\n" +
                "\"city\": \"address city\",\n" +
                "\"postcode\": \"AB1 CD2\",\n" +
                "\"country\": \"GB\"\n" +
                "}" + "}" + "}";

        CreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(payload), ctx);
        assertThat(paymentRequest.getAmount(), is(1000));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage(), is(nullValue()));
        assertThat(paymentRequest.getDelayedCapture(), is(nullValue()));
        assertThat(paymentRequest.getEmail(), is("j.bogs@example.org"));
        assertThat(paymentRequest.getPrefilledCardholderDetails().getCardholderName().isPresent(), is(true));
        assertThat(paymentRequest.getPrefilledCardholderDetails().getCardholderName().get(), is("J Bogs"));
        assertThat(paymentRequest.getPrefilledCardholderDetails().getBillingAddress().isPresent(), is(true));
        Address billingAddress = paymentRequest.getPrefilledCardholderDetails().getBillingAddress().get();
        assertThat(billingAddress.getLine1(), is("address line 1"));
        assertThat(billingAddress.getLine2(), is(nullValue()));
        assertThat(billingAddress.getPostcode(), is("AB1 CD2"));
        assertThat(billingAddress.getCity(), is("address city"));
        assertThat(billingAddress.getCountry(), is("GB"));
    }

    @Test
    public void shouldDeserializeARequestWithCardholderNameAndNoBillingAddressSuccessfully() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "\"email\": \"j.bogs@example.org\",\n" +
                "\"prefilled_cardholder_details\": {\n" +
                "\"cardholder_name\": \"J Bogs\"\n" +
                "}" + "}";

        CreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(payload), ctx);
        assertThat(paymentRequest.getAmount(), is(1000));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getEmail(), is("j.bogs@example.org"));
        assertThat(paymentRequest.getPrefilledCardholderDetails().getCardholderName().get(), is("J Bogs"));
        assertThat(paymentRequest.getPrefilledCardholderDetails().getBillingAddress().isPresent(), is(false));
    }

    @Test
    public void shouldDeserializeARequestWithBillingAddressSuccessfully() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "\"email\": null,\n" +
                "\"prefilled_cardholder_details\": {\n" +
                "\"billing_address\": {\n" +
                "\"line1\": \"address line 1\",\n" +
                "\"line2\": \"address line 2\",\n" +
                "\"city\": \"address city\",\n" +
                "\"postcode\": \"AB1 CD2\",\n" +
                "\"country\": null\n" +
                "}" + "}" + "}";

        CreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(payload), ctx);
        assertThat(paymentRequest.getAmount(), is(1000));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage(), is(nullValue()));
        assertThat(paymentRequest.getDelayedCapture(), is(nullValue()));
        assertThat(paymentRequest.getPrefilledCardholderDetails().getCardholderName(), is(Optional.empty()));
        assertThat(paymentRequest.getPrefilledCardholderDetails().getBillingAddress().isPresent(), is(true));
        Address billingAddress = paymentRequest.getPrefilledCardholderDetails().getBillingAddress().get();
        assertThat(billingAddress.getLine1(), is("address line 1"));
        assertThat(billingAddress.getLine2(), is("address line 2"));
        assertThat(billingAddress.getPostcode(), is("AB1 CD2"));
        assertThat(billingAddress.getCity(), is("address city"));
        assertThat(billingAddress.getCountry(), is(nullValue()));
    }

    @Test
    public void shouldDeserializeARequestWithEmptyCountrySuccessfully() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "\"email\": null,\n" +
                "\"prefilled_cardholder_details\": {\n" +
                "\"billing_address\": {\n" +
                "\"line1\": \"address line 1\",\n" +
                "\"line2\": \"address line 2\",\n" +
                "\"city\": \"address city\",\n" +
                "\"postcode\": \"AB1 CD2\",\n" +
                "\"country\": \"\"\n" +
                "}" + "}" + "}";

        CreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(payload), ctx);
        assertThat(paymentRequest.getAmount(), is(1000));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage(), is(nullValue()));
        assertThat(paymentRequest.getDelayedCapture(), is(nullValue()));
        assertThat(paymentRequest.getPrefilledCardholderDetails().getCardholderName().isPresent(), is(false));
        assertThat(paymentRequest.getPrefilledCardholderDetails().getBillingAddress().isPresent(), is(true));
        Address billingAddress = paymentRequest.getPrefilledCardholderDetails().getBillingAddress().get();
        assertThat(billingAddress.getLine1(), is("address line 1"));
        assertThat(billingAddress.getLine2(), is("address line 2"));
        assertThat(billingAddress.getPostcode(), is("AB1 CD2"));
        assertThat(billingAddress.getCity(), is("address city"));
        assertThat(billingAddress.getCountry(), is(""));
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenLine1IsNumeric() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "\"email\": null,\n" +
                "\"prefilled_cardholder_details\": {\n" +
                "\"billing_address\": {\n" +
                "\"line1\": 172,\n" +
                "\"line2\": \"address line 2\",\n" +
                "\"city\": \"address city\",\n" +
                "\"postcode\": \"AB1 CD2\",\n" +
                "\"country\": null\n" +
                "}" + "}" + "}";

        expectedException.expect(aBadRequestExceptionWithError("P0102", "Invalid attribute value: line1. Field must be a string"));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldNotThrowValidationException_whenCountryIsEmptyString() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "\"prefilled_cardholder_details\": {\n" +
                "\"billing_address\": {\n" +
                "\"country\": \"\"\n" +
                "}" + "}" + "}";
        deserializer.deserialize(jsonFactory.createParser(json), ctx);
        assertThat(true, is(true));
    }


    @After
    public void tearDown() {
        verifyZeroInteractions(ctx);
    }
}
