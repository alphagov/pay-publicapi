package uk.gov.pay.api.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CreateCardPaymentRequest;
import uk.gov.pay.api.model.PrefilledCardholderDetails;
import uk.gov.pay.commons.model.SupportedLanguage;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.pay.api.matcher.BadRequestExceptionMatcher.aBadRequestExceptionWithError;
import static uk.gov.pay.commons.model.Source.CARD_PAYMENT_LINK;

@RunWith(JUnitParamsRunner.class)
public class CreateCardPaymentRequestDeserializerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private DeserializationContext ctx;

    private JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());
    private CreateCardPaymentRequestDeserializer deserializer;

    @Before
    public void setup() {
        deserializer = new CreateCardPaymentRequestDeserializer();
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

        CreateCardPaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage(), is(Optional.empty()));
        assertThat(paymentRequest.getDelayedCapture(), is(Optional.empty()));
        assertThat(paymentRequest.getEmail(), is(Optional.empty()));
        assertThat(paymentRequest.getPrefilledCardholderDetails(), is(Optional.empty()));
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

        CreateCardPaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage(), is(Optional.of(SupportedLanguage.ENGLISH)));
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

        CreateCardPaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage(), is(Optional.of(SupportedLanguage.WELSH)));
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

        CreateCardPaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getDelayedCapture(), is(Optional.of(Boolean.TRUE)));
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

        CreateCardPaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getDelayedCapture(), is(Optional.of(Boolean.FALSE)));
    }

    @Parameters({"true", "false"})
    @Test
    public void deserialize_shouldDeserializeARequestWithMotoFieldSuccessfully(String value) throws Exception {
        // language=JSON
        String validJson = "{\n" +
                "  \"amount\": 27432,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"moto\":" + value + "\n" +
                "}";

        CreateCardPaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getMoto(), is(Optional.of(Boolean.parseBoolean(value))));
    }

    @Test
    public void deserialize_shouldThrowBadRequestException_whenJsonIsNotWellFormed() throws Exception {
        String invalidJson = "{" +
                "  \"amount\" : " +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"https://somewhere.gov.uk/rainbow/1\"" +
                "}";

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(invalidJson), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0197", "Unable to parse JSON"));
    }

    @Test
    public void deserialize_shouldThrowBadRequestException_whenAmountIsMissing() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: amount"));
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0101", "Missing mandatory attribute: amount"));
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: amount. Must be a valid numeric format"));
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: return_url. Must be a valid URL format"));
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReturnUrlIsMissing() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\"\n" +
                "}";

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0101",
                "Missing mandatory attribute: return_url"));
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0101",
                "Missing mandatory attribute: return_url"));
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReferenceIsMissing() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0101",
                "Missing mandatory attribute: reference"));
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0101",
                "Missing mandatory attribute: reference"));
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: reference. Must be a valid string format"));
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDescriptionIsMissing() throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 666,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\"\n" +
                "}";

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0101",
                "Missing mandatory attribute: description"));
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0101",
                "Missing mandatory attribute: description"));
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: description. Must be a valid string format"));
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: language. Must be \"en\" or \"cy\""));
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: language. Must be \"en\" or \"cy\""));
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: language. Must be \"en\" or \"cy\""));
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: delayed_capture. Must be true or false"));
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: delayed_capture. Must be true or false"));
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: delayed_capture. Must be true or false"));
    }

    @Test
    @Parameters({"null", "\"true\"", "0"})
    public void deserialize_shouldThrowValidationException_whenMotoIsNotABoolean(@Nullable String value) throws Exception {
        // language=JSON
        String json = "{\n" +
                "  \"amount\": 1337,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "  \"moto\": " + value + "\n" +
                "}";

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: moto. Must be true or false"));
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

        CreateCardPaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(payload), ctx);
        assertThat(paymentRequest.getAmount(), is(1000));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage(), is(Optional.empty()));
        assertThat(paymentRequest.getDelayedCapture(), is(Optional.empty()));
        assertThat(paymentRequest.getEmail(), is(Optional.of("j.bogs@example.org")));

        assertThat(paymentRequest.getPrefilledCardholderDetails().isPresent(), is(true));
        PrefilledCardholderDetails prefilledCardholderDetails = paymentRequest.getPrefilledCardholderDetails().get();
        assertThat(prefilledCardholderDetails.getCardholderName().isPresent(), is(true));
        assertThat(prefilledCardholderDetails.getCardholderName().get(), is("J Bogs"));
        assertThat(prefilledCardholderDetails.getBillingAddress().isPresent(), is(true));
        Address billingAddress = prefilledCardholderDetails.getBillingAddress().get();
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

        CreateCardPaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(payload), ctx);
        assertThat(paymentRequest.getAmount(), is(1000));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getEmail(), is(Optional.of("j.bogs@example.org")));
        assertThat(paymentRequest.getPrefilledCardholderDetails().isPresent(), is(true));
        assertThat(paymentRequest.getPrefilledCardholderDetails().get().getCardholderName().isPresent(), is(true));
        assertThat(paymentRequest.getPrefilledCardholderDetails().get().getCardholderName().get(), is("J Bogs"));
        assertThat(paymentRequest.getPrefilledCardholderDetails().get().getBillingAddress().isPresent(), is(false));
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

        CreateCardPaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(payload), ctx);
        assertThat(paymentRequest.getAmount(), is(1000));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage(), is(Optional.empty()));
        assertThat(paymentRequest.getDelayedCapture(), is(Optional.empty()));
        assertThat(paymentRequest.getPrefilledCardholderDetails().isPresent(), is(true));
        assertThat(paymentRequest.getPrefilledCardholderDetails().get().getCardholderName(), is(Optional.empty()));
        assertThat(paymentRequest.getPrefilledCardholderDetails().get().getBillingAddress().isPresent(), is(true));
        Address billingAddress = paymentRequest.getPrefilledCardholderDetails().get().getBillingAddress().get();
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

        CreateCardPaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(payload), ctx);
        assertThat(paymentRequest.getAmount(), is(1000));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(paymentRequest.getLanguage(), is(Optional.empty()));
        assertThat(paymentRequest.getDelayedCapture(), is(Optional.empty()));

        assertThat(paymentRequest.getPrefilledCardholderDetails().isPresent(), is(true));
        assertThat(paymentRequest.getPrefilledCardholderDetails().get().getCardholderName().isPresent(), is(false));
        assertThat(paymentRequest.getPrefilledCardholderDetails().get().getBillingAddress().isPresent(), is(true));
        Address billingAddress = paymentRequest.getPrefilledCardholderDetails().get().getBillingAddress().get();
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

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: line1. Field must be a string"));
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

    @Test
    public void shouldDeserializeARequestAndSetSourceCorrectly() throws Exception {
        // language=JSON
        String payload = "{\n" +
                "  \"amount\": 1000,\n" +
                "  \"reference\": \"Some reference\",\n" +
                "  \"description\": \"Some description\",\n" +
                "  \"return_url\": \"https://somewhere.gov.uk/rainbow/1\",\n" +
                "\"internal\": {\n" +
                "\"source\": \"CARD_PAYMENT_LINK\"\n" +
                "}" + "}";

        CreateCardPaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(payload), ctx);
        assertThat(paymentRequest.getInternal().get().getSource().get(), is(CARD_PAYMENT_LINK));
    }

    @After
    public void tearDown() {
        verifyNoInteractions(ctx);
    }
}
