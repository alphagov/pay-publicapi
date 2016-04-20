package uk.gov.pay.api.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.validation.PaymentRequestValidator;

import java.io.IOException;

import static uk.gov.pay.api.json.PaymentRequestJsonParser.paymentRequestValueOf;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_PARSING_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class CreatePaymentRequestDeserializer extends StdDeserializer<CreatePaymentRequest> {

    private PaymentRequestValidator validator;

    public CreatePaymentRequestDeserializer(PaymentRequestValidator validator) {
        super(CreatePaymentRequest.class);
        this.validator = validator;
    }

    @Override
    public CreatePaymentRequest deserialize(JsonParser parser, DeserializationContext context) {

        CreatePaymentRequest paymentRequest;

        try {
            paymentRequest = paymentRequestValueOf(parser.readValueAsTree());
        } catch (IOException e) {
            throw new BadRequestException(aPaymentError(CREATE_PAYMENT_PARSING_ERROR));
        }

        validator.validate(paymentRequest);
        return paymentRequest;
    }
}
