package uk.gov.pay.api.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.validation.PaymentRequestValidator;
import uk.gov.pay.api.validation.URLValidator;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.gov.pay.api.matcher.BadRequestExceptionMatcher.aBadRequestExceptionContaining;
import static uk.gov.pay.api.matcher.ValidationExceptionMatcher.aValidationExceptionContaining;
import static uk.gov.pay.api.model.PaymentError.Code.*;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

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
        deserializer = new CreatePaymentRequestDeserializer(new PaymentRequestValidator(urlValidator));
    }

    @Test
    public void deserialize_shouldDeserializeARequestSuccessfully() throws Exception {

        String validJson = "{" +
                "  \"amount\" : 27432," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        CreatePaymentRequest paymentRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRequest.getAmount(), is(27432));
        assertThat(paymentRequest.getReference(), is("Some reference"));
        assertThat(paymentRequest.getDescription(), is("Some description"));
        assertThat(paymentRequest.getReturnUrl(), is("http://somewhere.gov.uk/rainbow/1"));
    }

    @Test
    public void deserialize_shouldThrowBadRequestException_whenJsonIsNotWellFormed() throws Exception {

        String invalidJson = "{" +
                "  \"amount\" : " +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(BadRequestException.class);
        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0100, "Unable to parse JSON")));

        deserializer.deserialize(jsonFactory.createParser(invalidJson), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenAmountIsMissing() throws Exception {

        String json = "{" +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0103, "Missing mandatory attribute: amount")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_asAmountIsMissing_whenAmountIsNullValue() throws Exception {

        String json = "{" +
                "  \"amount\" : null," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0103, "Missing mandatory attribute: amount")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenAmountIsNotInteger() throws Exception {

        String json = "{" +
                "  \"amount\" : \"\"," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0100, "Invalid attribute value: amount. Must be a valid numeric format")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenAmountIsLessThanMinimum() throws Exception {

        String json = "{" +
                "  \"amount\" : 0," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(aValidationExceptionContaining(aPaymentError(P0101, "Invalid attribute value: amount. Must be greater than or equal to 1")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenAmountIsMoreThanMaximum() throws Exception {

        String json = "{" +
                "  \"amount\" : 10000001," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(aValidationExceptionContaining(aPaymentError(P0102, "Invalid attribute value: amount. Must be less than or equal to 10000000")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReturnUrlIsNotAnStringValue() throws Exception {

        String json = "{" +
                "  \"amount\" : 1000000," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : 1" +
                "}";

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0100, "Invalid attribute value: return_url. Must be a valid URL format")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReturnUrlLengthIsMoreThan2000CharactersLength() throws Exception {

        String json = "{" +
                "  \"amount\" : 1000000," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"" + RandomStringUtils.randomAlphanumeric(2001) + "\"" +
                "}";

        expectedException.expect(aValidationExceptionContaining(aPaymentError(P0102, "Invalid attribute value: return_url. Must be less than or equal to 2000 characters length")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReturnUrlIsAMalformedUrl() throws Exception {

        String json = "{" +
                "  \"amount\" : 666," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"" + RandomStringUtils.randomAlphanumeric(50) + "\"" +
                "}";

        expectedException.expect(aValidationExceptionContaining(aPaymentError(P0102, "Invalid attribute value: return_url. Must be a valid URL format")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReturnUrlIsMissing() throws Exception {

        String json = "{" +
                "  \"amount\" : 666," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"" +
                "}";

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0103, "Missing mandatory attribute: return_url")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_AsReturnUrlIsMissing_whenReturnUrlIsNullValue() throws Exception {

        String json = "{" +
                "  \"amount\" : 666," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : null" +
                "}";

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0103, "Missing mandatory attribute: return_url")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReferenceIsMissing() throws Exception {

        String json = "{" +
                "  \"amount\" : 666," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0103, "Missing mandatory attribute: reference")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_AsReferenceIsMissing_whenReferenceIsNullValue() throws Exception {

        String json = "{" +
                "  \"amount\" : 666," +
                "  \"reference\" : null," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0103, "Missing mandatory attribute: reference")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReferenceIsNotAnString() throws Exception {

        String json = "{" +
                "  \"amount\" : 666," +
                "  \"reference\" : 123," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0100, "Invalid attribute value: reference. Must be a valid string format")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenReferenceIsMoreThan255CharactersLength() throws Exception {

        String json = "{" +
                "  \"amount\" : 666," +
                "  \"reference\" : \"" + RandomStringUtils.randomAlphanumeric(256) + "\"," +
                "  \"description\" : \"Some description\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(aValidationExceptionContaining(aPaymentError(P0102, "Invalid attribute value: reference. Must be less than or equal to 255 characters length")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDescriptionIsMissing() throws Exception {

        String json = "{" +
                "  \"amount\" : 666," +
                "  \"reference\" : \"Some reference\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0103, "Missing mandatory attribute: description")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_asDescriptionIsMissing_whenDescriptionIsNullValue() throws Exception {

        String json = "{" +
                "  \"amount\" : 666," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : null," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0103, "Missing mandatory attribute: description")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDescriptionIsNotAnString() throws Exception {

        String json = "{" +
                "  \"amount\" : 666," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : 432," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(aBadRequestExceptionContaining(aPaymentError(P0100, "Invalid attribute value: description. Must be a valid string format")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @Test
    public void deserialize_shouldThrowValidationException_whenDescriptionIsMoreThan255CharactersLength() throws Exception {

        String json = "{" +
                "  \"amount\" : 666," +
                "  \"reference\" : \"Some reference\"," +
                "  \"description\" : \"" + RandomStringUtils.randomAlphanumeric(256) + "\"," +
                "  \"return_url\" : \"http://somewhere.gov.uk/rainbow/1\"" +
                "}";

        expectedException.expect(aValidationExceptionContaining(aPaymentError(P0102, "Invalid attribute value: description. Must be less than or equal to 255 characters length")));

        deserializer.deserialize(jsonFactory.createParser(json), ctx);
    }

    @After
    public void tearDown() {
        verifyZeroInteractions(ctx);
    }
}
