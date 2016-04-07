package uk.gov.pay.api.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.slf4j.Logger;
import uk.gov.pay.api.exception.PaymentError;
import uk.gov.pay.api.model.CreatePaymentRequest;

import javax.ws.rs.BadRequestException;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;
import static uk.gov.pay.api.exception.PaymentError.ErrorCode.P0101;
import static uk.gov.pay.api.exception.PaymentError.ErrorCode.P0102;
import static uk.gov.pay.api.exception.PaymentError.invalidAttributeValue;
import static uk.gov.pay.api.json.PaymentParser.*;

public class CreatePaymentRequestDeserializer extends StdDeserializer<CreatePaymentRequest> {

    public static final Logger LOGGER = getLogger(CreatePaymentRequestDeserializer.class);

    protected CreatePaymentRequestDeserializer() {
        super(CreatePaymentRequest.class);
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
        String returnUrl = parseString(rootNode, "return_url");
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
}
