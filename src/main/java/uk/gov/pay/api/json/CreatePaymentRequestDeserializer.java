package uk.gov.pay.api.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.validation.PaymentRequestValidator;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.pay.api.json.JsonParser.parseInteger;
import static uk.gov.pay.api.json.JsonParser.parseString;
import static uk.gov.pay.api.model.CreatePaymentRequest.*;
import static uk.gov.pay.api.model.PaymentError.Code.P0100;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;
import static uk.gov.pay.api.model.PaymentError.invalidURLFormatAttributeValue;

public class CreatePaymentRequestDeserializer extends StdDeserializer<CreatePaymentRequest> {

    private static final Logger LOGGER = getLogger(CreatePaymentRequestDeserializer.class);

    private PaymentRequestValidator validator;

    public CreatePaymentRequestDeserializer(PaymentRequestValidator validator) {
        super(CreatePaymentRequest.class);
        this.validator = validator;
    }

    @Override
    public CreatePaymentRequest deserialize(JsonParser parser, DeserializationContext context) {

        JsonNode rootNode;

        try {
            rootNode = parser.readValueAsTree();
        } catch (IOException e) {
            LOGGER.info("Error parsing CreatePayment request: {}", e.getMessage());
            throw new BadRequestException(aPaymentError(P0100, "Unable to parse JSON"));
        }

        int amount = parseInteger(rootNode, AMOUNT_FIELD_NAME);
        String returnUrl = parseString(rootNode, RETURN_URL_FIELD_NAME, invalidURLFormatAttributeValue(RETURN_URL_FIELD_NAME));
        String reference = parseString(rootNode, REFERENCE_FIELD_NAME);
        String description = parseString(rootNode, DESCRIPTION_FIELD_NAME);

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(amount, returnUrl, reference, description);

        validator.validate(paymentRequest);

        return paymentRequest;
    }
}
