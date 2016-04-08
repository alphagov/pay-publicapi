package uk.gov.pay.api.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import uk.gov.pay.api.exception.PaymentError;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.validation.URLValidator;

import javax.ws.rs.BadRequestException;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.pay.api.exception.PaymentError.ErrorCode.P0101;
import static uk.gov.pay.api.exception.PaymentError.ErrorCode.P0102;
import static uk.gov.pay.api.exception.PaymentError.invalidAttributeValue;
import static uk.gov.pay.api.json.PaymentParser.*;

public class CreatePaymentRequestDeserializer extends StdDeserializer<CreatePaymentRequest> {

    private static final Logger LOGGER = getLogger(CreatePaymentRequestDeserializer.class);

    private URLValidator urlValidator;

    public CreatePaymentRequestDeserializer(URLValidator urlValidator) {
        super(CreatePaymentRequest.class);
        this.urlValidator = urlValidator;
    }

    @Override
    public CreatePaymentRequest deserialize(JsonParser parser, DeserializationContext context) {

        JsonNode rootNode;

        try {
            rootNode = parser.readValueAsTree();
        } catch (IOException e) {
            LOGGER.info("Error parsing CreatePayment request: {}", e.getMessage());
            throw new BadRequestException(PaymentError.unparseableJSON().asResponse());
        }

        int amount = parseAmount(rootNode);
        String returnUrl = parseReturnUrl(rootNode);
        String reference = parseString(rootNode, "reference");
        String description = parseString(rootNode, "description");

        return new CreatePaymentRequest(amount, returnUrl, reference, description);
    }

    private int parseAmount(JsonNode rootNode) {
        int amount = parseInteger(rootNode, "amount");
        check(amount > 0, invalidAttributeValue(P0101, "amount", "Must be greater than or equal to 1"));
        check(amount <= 10000000, invalidAttributeValue(P0102, "amount", "Must be less than or equal to 10000000"));
        return amount;
    }

    private String parseReturnUrl(JsonNode rootNode) {
        String returnUrl = parseString(rootNode, "return_url");
        check(returnUrl.length() <= 2000, invalidAttributeValue(P0102, "return_url", "Length must be less than or equals 2000 characters"));
        check(urlValidator.isValid(returnUrl), invalidAttributeValue(P0102, "return_url", "Must be a valid URL format"));
        return returnUrl;
    }
}
