package uk.gov.pay.api.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.exception.PaymentValidationException;
import uk.gov.pay.api.model.CreatePaymentRefundRequest;
import uk.gov.pay.api.validation.PaymentRefundRequestValidator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.pay.api.matcher.BadRequestExceptionMatcher.aBadRequestExceptionWithError;
import static uk.gov.pay.api.matcher.PaymentValidationExceptionMatcher.aValidationExceptionContaining;

@ExtendWith(MockitoExtension.class)
class CreatePaymentRefundRequestDeserializerTest {

    @Mock
    private DeserializationContext ctx;

    private final JsonFactory jsonFactory = new JsonFactory(new ObjectMapper());
    private CreatePaymentRefundRequestDeserializer deserializer;

    @BeforeEach
    void setup() {
        deserializer = new CreatePaymentRefundRequestDeserializer(new PaymentRefundRequestValidator());
    }

    @Test
    void deserialize_shouldDeserializeARequestSuccessfully() throws Exception {

        String validJson = "{" +
                "  \"amount\" : 12345" +
                "}";

        CreatePaymentRefundRequest paymentRefundRequest = deserializer.deserialize(jsonFactory.createParser(validJson), ctx);

        assertThat(paymentRefundRequest.getAmount(), is(12345));
    }

    @Test
    void deserialize_shouldThrowBadRequestException_whenJsonIsNotWellFormed() {

        String invalidJson = "{" +
                "  \"amount\" : " +
                "}";

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(invalidJson), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0697", "Unable to parse JSON"));
    }

    @Test
    void deserialize_shouldThrowBadRequestException_whenAmountIsMissing() {

        String json = "{}";

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0601", "Missing mandatory attribute: amount"));
    }

    @Test
    void deserialize_shouldThrowValidationException_asAmountIsMissing_whenAmountIsNullValue() {

        String json = "{" +
                "  \"amount\" : null" +
                "}";

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0601", "Missing mandatory attribute: amount"));
    }

    @Test
    void deserialize_shouldThrowValidationException_whenAmountIsNotInteger() {

        String json = "{" +
                "  \"amount\" : \"\"" +
                "}";

        BadRequestException badRequestException = assertThrows(BadRequestException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(badRequestException, aBadRequestExceptionWithError("P0602",
                "Invalid attribute value: amount. Must be a valid numeric format"));
    }

    @Test
    void deserialize_shouldThrowValidationException_whenAmountIsLessThanMinimum() {

        String json = "{" +
                "  \"amount\" : 0" +
                "}";

        PaymentValidationException exception = assertThrows(PaymentValidationException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(exception, aValidationExceptionContaining("P0602",
                "Invalid attribute value: amount. Must be greater than or equal to 1"));
    }

    @Test
    void deserialize_shouldThrowValidationException_whenAmountIsMoreThanMaximum() {

        String json = "{" +
                "  \"amount\" : 10000001" +
                "}";

        PaymentValidationException exception = assertThrows(PaymentValidationException.class,
                () -> deserializer.deserialize(jsonFactory.createParser(json), ctx));

        assertThat(exception, aValidationExceptionContaining("P0602",
                "Invalid attribute value: amount. Must be less than or equal to 10000000"));
    }

    @AfterEach
    void tearDown() {
        verifyNoInteractions(ctx);
    }
}
