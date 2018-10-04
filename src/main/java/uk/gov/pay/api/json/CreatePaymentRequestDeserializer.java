package uk.gov.pay.api.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.ValidCreatePaymentRequest;
import uk.gov.pay.api.validation.PaymentRequestValidator;

import java.io.IOException;
import java.util.Objects;

import static uk.gov.pay.api.json.RequestJsonParser.parsePaymentRequest;
import static uk.gov.pay.api.model.PaymentErrorBuilder.aPaymentError;
import static uk.gov.pay.api.model.PaymentErrorCodes.CREATE_PAYMENT_PARSING_ERROR;

public class CreatePaymentRequestDeserializer extends StdDeserializer<ValidCreatePaymentRequest> {

    private PaymentRequestValidator validator;

    public CreatePaymentRequestDeserializer(PaymentRequestValidator validator) {
        super(CreatePaymentRequest.class);
        this.validator = validator;
    }

    @Override
    public ValidCreatePaymentRequest deserialize(JsonParser parser, DeserializationContext context) {
        CreatePaymentRequest paymentRequest;
        try {
            JsonNode json = parser.readValueAsTree();
            paymentRequest = parsePaymentRequest(json);
        } catch (IOException e) {
            throw new BadRequestException(aPaymentError(CREATE_PAYMENT_PARSING_ERROR));
        }

        validator.validate(paymentRequest);

        return new ValidCreatePaymentRequest(paymentRequest);
    }
}
