package uk.gov.pay.api.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.CreatePaymentRefundRequest;
import uk.gov.pay.api.validation.PaymentRefundRequestValidator;

import java.io.IOException;

import static uk.gov.pay.api.json.RequestJsonParser.paymentRefundRequestValueOf;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_REFUND_PARSING_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class CreatePaymentRefundRequestDeserializer extends StdDeserializer<CreatePaymentRefundRequest> {

    private PaymentRefundRequestValidator validator;

    public CreatePaymentRefundRequestDeserializer(PaymentRefundRequestValidator validator) {
        super(CreatePaymentRefundRequest.class);
        this.validator = validator;
    }

    @Override
    public CreatePaymentRefundRequest deserialize(JsonParser parser, DeserializationContext context) {

        CreatePaymentRefundRequest paymentRefundRequest;

        try {
            paymentRefundRequest = paymentRefundRequestValueOf(parser.readValueAsTree());
        } catch (IOException e) {
            throw new BadRequestException(aPaymentError(CREATE_PAYMENT_REFUND_PARSING_ERROR));
        }

        validator.validate(paymentRefundRequest);
        return paymentRefundRequest;
    }
}
