package uk.gov.pay.api.validation;

import uk.gov.pay.api.model.CreatePaymentRequest;

public class PaymentRequestValidator {

    static final int CARD_BRAND_MAX_LENGTH = 20;
    public static final int AGREEMENT_ID_MAX_LENGTH = 26;
    
    public void validate(CreatePaymentRequest paymentRequest) {
    }
    
}
