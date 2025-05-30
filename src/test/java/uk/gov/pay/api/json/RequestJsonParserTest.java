package uk.gov.pay.api.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.exception.PaymentValidationException;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CreateCardPaymentRequest;
import uk.gov.pay.api.model.CreatePaymentRefundRequest;
import uk.gov.pay.api.model.PrefilledCardholderDetails;
import uk.gov.service.payments.commons.model.SupportedLanguage;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThrows;
import static uk.gov.pay.api.json.RequestJsonParser.parsePaymentRequest;
import static uk.gov.pay.api.json.RequestJsonParser.parseRefundRequest;
import static uk.gov.pay.api.matcher.BadRequestExceptionMatcher.aBadRequestExceptionWithError;
import static uk.gov.service.payments.commons.model.AuthorisationMode.MOTO_API;
import static uk.gov.service.payments.commons.model.Source.CARD_AGENT_INITIATED_MOTO;
import static uk.gov.service.payments.commons.model.Source.CARD_API;
import static uk.gov.service.payments.commons.model.Source.CARD_PAYMENT_LINK;

class RequestJsonParserTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsePaymentRequest_withReturnUrl_shouldParseSuccessfully() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        CreateCardPaymentRequest createPaymentRequest = parsePaymentRequest(jsonNode);

        assertThat(createPaymentRequest, is(notNullValue()));
        assertThat(createPaymentRequest.getAmount(), is(1000));
        assertThat(createPaymentRequest.getReference(), is("Some reference"));
        assertThat(createPaymentRequest.getDescription(), is("Some description"));
        assertThat(createPaymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
    }

    @Test
    void parsePaymentRequest_withoutReturnUrl_shouldParseSuccessfully() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        CreateCardPaymentRequest createPaymentRequest = parsePaymentRequest(jsonNode);

        assertThat(createPaymentRequest, is(notNullValue()));
        assertThat(createPaymentRequest.getAmount(), is(1000));
        assertThat(createPaymentRequest.getReference(), is("Some reference"));
        assertThat(createPaymentRequest.getDescription(), is("Some description"));
        assertThat(createPaymentRequest.getReturnUrl(), is(nullValue()));
    }

    @Test
    void parsePaymentRequest_withReturnUrlAndLanguageAndDelayedCaptureAndMoto_shouldParseSuccessfully() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "language": "en",
                  "delayed_capture": true,
                  "moto": true
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        CreateCardPaymentRequest createPaymentRequest = parsePaymentRequest(jsonNode);

        assertThat(createPaymentRequest, is(notNullValue()));
        assertThat(createPaymentRequest.getAmount(), is(1000));
        assertThat(createPaymentRequest.getReference(), is("Some reference"));
        assertThat(createPaymentRequest.getDescription(), is("Some description"));
        assertThat(createPaymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(createPaymentRequest.getLanguage(), is(Optional.of(SupportedLanguage.ENGLISH)));
        assertThat(createPaymentRequest.getDelayedCapture(), is(Optional.of(true)));
        assertThat(createPaymentRequest.getMoto(), is(Optional.of(true)));
    }

    @Test
    void parsePaymentRefundRequest_shouldParseSuccessfully() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        CreatePaymentRefundRequest createPaymentRefundRequest = parseRefundRequest(jsonNode);

        assertThat(createPaymentRefundRequest, is(notNullValue()));
        assertThat(createPaymentRefundRequest.getAmount(), is(1000));
    }

    @Test
    void parsePaymentRequest_whenReferenceFieldIsNotAString() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": 1234,
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: reference. Must be a valid string format"));
    }

    @Test
    void parsePaymentRequest_whenReferenceFieldContainsIllegalCharacters() throws Exception {
        String payload = null;
        for (Character illegalChar : RequestJsonParser.NAXSI_NOT_ALLOWED_CHARACTERS) {
            payload = String.format("""
                    {
                    "amount": 1000,
                    "reference": "Reference with %s character",
                    "description": "Some description",
                    "return_url": "https://somewhere.gov.uk/rainbow/1"
                    }
                    """, illegalChar);

            JsonNode jsonNode = objectMapper.readTree(payload);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> parsePaymentRequest(jsonNode));

            assertThat(exception, aBadRequestExceptionWithError("P0102",
                    "Invalid attribute value: reference. Must be a valid string format"));
        }
    }

    @Test
    void parsePaymentRequest_whenDescriptionFieldIsNotAString() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": 1234,
                  "return_url": "https://somewhere.gov.uk/rainbow/1"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: description. Must be a valid string format"));
    }

    @Test
    void parsePaymentRequest_whenDescriptionFieldContainsIllegalCharacters() throws Exception {
        String payload = null;
        for (Character illegalChar : RequestJsonParser.NAXSI_NOT_ALLOWED_CHARACTERS) {
            payload = String.format("""
                    {
                    "amount": 1000,
                    "reference": "Valid reference",
                    "description": "Description with %s character",
                    "return_url": "https://somewhere.gov.uk/rainbow/1"
                    }
                    """, illegalChar);

            JsonNode jsonNode = objectMapper.readTree(payload);

            BadRequestException exception = assertThrows(BadRequestException.class,
                    () -> parsePaymentRequest(jsonNode));

            assertThat(exception, aBadRequestExceptionWithError("P0102",
                    "Invalid attribute value: description. Must be a valid string format"));
        }
    }

    @Test
    void parsePaymentRequest_whenLanguageFieldIsNotAString() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "language": 0
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: language. Must be \"en\" or \"cy\""));
    }

    @Test
    void parsePaymentRequest_whenDelayedCaptureFieldIsNotABoolean() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "delayed_capture": "true"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: delayed_capture. Must be true or false"));
    }

    @Test
    void parsePaymentRequest_whenReturnUrlIsNotAString_shouldOverrideFormattingErrorMessage() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": 1234
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: return_url. Must be a valid URL format"));
    }

    @Test
    void parsePaymentRequest_whenReferenceFieldIsNullValue() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": null,
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0101",
                "Missing mandatory attribute: reference"));
    }

    @Test
    void parsePaymentRequest_whenDescriptionFieldIsNullValue() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": null,
                  "return_url": "https://somewhere.gov.uk/rainbow/1"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0101",
                "Missing mandatory attribute: description"));
    }

    @Test
    void parsePaymentRequest_whenLanguageFieldIsNullValue() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "language": null
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: language. Must be \"en\" or \"cy\""));
    }

    @Test
    void parsePaymentRefundRequest_whenAmountFieldIsNullValue() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": null
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parseRefundRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0601",
                "Missing mandatory attribute: amount"));
    }

    @Test
    void parsePaymentRequest_whenAmountFieldIsMissing() throws Exception {
        // language=JSON
        String payload = """
                {
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": 1234
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0101",
                "Missing mandatory attribute: amount"));
    }

    @Test
    void parsePaymentRequest_whenReferenceFieldIsMissing() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "description": "Some description",
                  "return_url": 1234
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0101",
                "Missing mandatory attribute: reference"));
    }

    @Test
    void parsePaymentRequest_whenDescriptionFieldIsMissing() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "return_url": 1234
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0101",
                "Missing mandatory attribute: description"));
    }

    @Test
    void parsePaymentRefundRequest_whenAmountFieldIsMissing() throws Exception {
        // language=JSON
        String payload = """
                {}
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parseRefundRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0601",
                "Missing mandatory attribute: amount"));
    }

    @Test
    void parsePaymentRequest_withAllPrefilledCardholderDetails_shouldParseSuccessfully() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "email": "j.bogs@example.org",
                  "prefilled_cardholder_details": {
                    "cardholder_name": "J Bogs",
                    "billing_address": {
                      "line1": "address line 1",
                      "line2": "address line 2",
                      "city": "address city",
                      "postcode": "AB1 CD2",
                      "country": "GB"
                    }
                  }
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        CreateCardPaymentRequest createPaymentRequest = parsePaymentRequest(jsonNode);

        assertThat(createPaymentRequest, is(notNullValue()));
        assertThat(createPaymentRequest.getAmount(), is(1000));
        assertThat(createPaymentRequest.getReference(), is("Some reference"));
        assertThat(createPaymentRequest.getDescription(), is("Some description"));
        assertThat(createPaymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(createPaymentRequest.getEmail(), is(Optional.of("j.bogs@example.org")));
        assertThat(createPaymentRequest.getPrefilledCardholderDetails(), is(notNullValue()));
        PrefilledCardholderDetails cardholderDetails = createPaymentRequest.getPrefilledCardholderDetails().get();
        assertThat(cardholderDetails.getCardholderName().isPresent(), is(true));
        assertThat(cardholderDetails.getCardholderName().get(), is("J Bogs"));
        assertThat(cardholderDetails.getBillingAddress().isPresent(), is(true));
        Address billingAddress = cardholderDetails.getBillingAddress().get();
        assertThat(billingAddress.getLine1(), is("address line 1"));
        assertThat(billingAddress.getLine2(), is("address line 2"));
        assertThat(billingAddress.getCity(), is("address city"));
        assertThat(billingAddress.getPostcode(), is("AB1 CD2"));
        assertThat(billingAddress.getCountry(), is("GB"));
    }

    @Test
    void parsePaymentRequest_withSomePrefilledCardholderDetails_shouldParseSuccessfully() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "email": null,
                  "prefilled_cardholder_details": {
                    "cardholder_name": null,
                    "billing_address": {
                      "line1": "address line 1",
                      "line2": null,
                      "city": "address city",
                      "postcode": "AB1 CD2",
                      "country": "GB"
                    }
                  }
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        CreateCardPaymentRequest createPaymentRequest = parsePaymentRequest(jsonNode);

        assertThat(createPaymentRequest, is(notNullValue()));
        assertThat(createPaymentRequest.getAmount(), is(1000));
        assertThat(createPaymentRequest.getReference(), is("Some reference"));
        assertThat(createPaymentRequest.getDescription(), is("Some description"));
        assertThat(createPaymentRequest.getReturnUrl(), is("https://somewhere.gov.uk/rainbow/1"));
        assertThat(createPaymentRequest.getEmail(), is(Optional.empty()));
        assertThat(createPaymentRequest.getPrefilledCardholderDetails(), is(notNullValue()));
        assertThat(createPaymentRequest.getPrefilledCardholderDetails().isPresent(), is(true));
        PrefilledCardholderDetails cardholderDetails = createPaymentRequest.getPrefilledCardholderDetails().get();
        assertThat(cardholderDetails.getCardholderName().isPresent(), is(false));
        assertThat(cardholderDetails.getBillingAddress().isPresent(), is(true));
        Address billingAddress = cardholderDetails.getBillingAddress().get();
        assertThat(billingAddress.getLine1(), is("address line 1"));
        assertThat(billingAddress.getLine2(), is(nullValue()));
        assertThat(billingAddress.getCity(), is("address city"));
        assertThat(billingAddress.getPostcode(), is("AB1 CD2"));
        assertThat(billingAddress.getCountry(), is("GB"));
    }

    @Test
    void parsePaymentRequest_withEmailFieldIsNotAString() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "email": false,
                  "prefilled_cardholder_details": {
                    "cardholder_name": "J Bogs",
                    "billing_address": {
                      "line1": "address line 1",
                      "line2": "address line 2",
                      "city": "address city",
                      "postcode": "AB1 CD2",
                      "country": "GB"
                    }
                  }
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: email. Field must be a string"));
    }

    @Test
    void parsePaymentRequest_withLine1FieldIsNotAString() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "email": "j.bogs@example.com",
                  "prefilled_cardholder_details": {
                    "cardholder_name": "J Bogs",
                    "billing_address": {
                      "line1": 182,
                      "line2": "address line 2",
                      "city": "address city",
                      "postcode": "AB1 CD2",
                      "country": "GB"
                    }
                  }
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: line1. Field must be a string"));
    }

    @Test
    void parsePaymentRequest_shouldSetSourceToDefaultIfNotInPayload() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 27432,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "language": "en"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        CreateCardPaymentRequest paymentRequest = parsePaymentRequest(jsonNode);
        assertThat(paymentRequest.getInternal().get().getSource().get(), is(CARD_API));
    }

    @Test
    void parsePaymentRequest_shouldParseCardPaymentLinkSourceCorrectly() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "internal": {
                    "source": "CARD_PAYMENT_LINK"
                  }
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);
        CreateCardPaymentRequest paymentRequest = parsePaymentRequest(jsonNode);
        assertThat(paymentRequest.getInternal().get().getSource().get(), is(CARD_PAYMENT_LINK));
    }

    @Test
    void parsePaymentRequest_shouldCardAgentInitiatedMotoSourceCorrectly() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "internal": {
                    "source": "CARD_AGENT_INITIATED_MOTO"
                  }
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);
        CreateCardPaymentRequest paymentRequest = parsePaymentRequest(jsonNode);
        assertThat(paymentRequest.getInternal().get().getSource().get(), is(CARD_AGENT_INITIATED_MOTO));
    }

    @Test
    void parsePaymentRequest_shouldThrowValidationException_whenSourceIsInvalidType() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "internal": {
                    "source": true
                  }
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: source. Accepted values are only CARD_PAYMENT_LINK, CARD_AGENT_INITIATED_MOTO"));
    }

    @Test
    void parsePaymentRequest_shouldThrowValidationException_whenSourceIsValidEnumTypeButNotAccepted() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "internal": {
                    "source": "CARD_EXTERNAL_TELEPHONE"
                  }
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException, aBadRequestExceptionWithError("P0102",
                "Invalid attribute value: source. Accepted values are only CARD_PAYMENT_LINK, CARD_AGENT_INITIATED_MOTO"));
    }

    @Test
    void parsePaymentRequest_shouldParseValidAuthorisationMode() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "authorisation_mode": "moto_api"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);
        CreateCardPaymentRequest paymentRequest = parsePaymentRequest(jsonNode);
        assertThat(paymentRequest.getAuthorisationMode().get(), is(MOTO_API));
    }

    @Test
    void parsePaymentRequest_shouldThrowValidationException_whenAuthorisationModeIsNotValidEnumValue() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "authorisation_mode": "foo"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(paymentValidationException.getRequestError().getCode(), is("P0102"));
        assertThat(paymentValidationException.getRequestError().getDescription(), is("Invalid attribute value: authorisation_mode. Must be one of web, moto_api, agreement"));
    }

    @Test
    void parsePaymentRequest_shouldThrowValidationException_whenAuthorisationModeIsValidEnumValueButNotAccepted() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "authorisation_mode": "external"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        PaymentValidationException paymentValidationException = assertThrows(PaymentValidationException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(paymentValidationException.getRequestError().getCode(), is("P0102"));
        assertThat(paymentValidationException.getRequestError().getDescription(), is("Invalid attribute value: authorisation_mode. Must be one of web, moto_api, agreement"));
    }

    @Test
    void parsePaymentRequest_shouldParseAgreementId_whenAuthorisationModeAgreement() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "authorisation_mode": "agreement",
                  "agreement_id": "abcdefghijklmnopqrstuvwxyz"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);
        CreateCardPaymentRequest paymentRequest = parsePaymentRequest(jsonNode);
        assertThat(paymentRequest.getAgreementId().get(), is("abcdefghijklmnopqrstuvwxyz"));
    }

    @Test
    void parsePaymentRequest_shouldThrowValidationException_whenAgreementIdIsInvalidType_butAuthorisationModeIsAgreement() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "authorisation_mode": "agreement",
                  "agreement_id": true
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException.getRequestError().getCode(), is("P0102"));
        assertThat(badRequestException.getRequestError().getDescription(), is("Invalid attribute value: agreement_id. Must be a valid string format"));
    }

    @Test
    void parsePaymentRequest_shouldThrowValidationException_whenAgreementIdIsProvidedAndAuthorisationModeIsNotAgreement() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "authorisation_mode": "web",
                  "agreement_id": "abcdefgklmnopqrstuvwxyz"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException.getRequestError().getCode(), is("P0104"));
        assertThat(badRequestException.getRequestError().getDescription(), is("Unexpected attribute: agreement_id"));
    }

    @Test
    void parsePaymentRequest_shouldThrowValidationException_whenAgreementIdIsProvidedAndAuthorisationModeIsNotSpecified() throws Exception {
        // language=JSON
        String payload = """
                {
                  "amount": 1000,
                  "reference": "Some reference",
                  "description": "Some description",
                  "return_url": "https://somewhere.gov.uk/rainbow/1",
                  "agreement_id": "abcdefgklmnopqrstuvwxyz"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(payload);

        BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> parsePaymentRequest(jsonNode));
        assertThat(badRequestException.getRequestError().getCode(), is("P0104"));
        assertThat(badRequestException.getRequestError().getDescription(), is("Unexpected attribute: agreement_id"));
    }
}
