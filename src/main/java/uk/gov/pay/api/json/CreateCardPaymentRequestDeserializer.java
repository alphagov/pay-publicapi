package uk.gov.pay.api.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.CreateCardPaymentRequest;

import java.io.IOException;

import static uk.gov.pay.api.json.RequestJsonParser.parsePaymentRequest;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_PARSING_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class CreateCardPaymentRequestDeserializer extends StdDeserializer<CreateCardPaymentRequest> {

    public CreateCardPaymentRequestDeserializer() {
        super(CreateCardPaymentRequest.class);
    }

    @Override
    public CreateCardPaymentRequest deserialize(JsonParser parser, DeserializationContext context) {
        try {
            JsonNode json = parser.readValueAsTree();
            return parsePaymentRequest(json);
        } catch (IOException e) {
            throw new BadRequestException(aPaymentError(CREATE_PAYMENT_PARSING_ERROR));
        }
    }
}
