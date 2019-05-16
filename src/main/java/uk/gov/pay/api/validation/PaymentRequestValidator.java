package uk.gov.pay.api.validation;

import uk.gov.pay.api.exception.PaymentValidationException;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.PrefilledCardholderDetails;

import static java.lang.String.format;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_CITY_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_COUNTRY_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_LINE1_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_LINE2_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_POSTCODE_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_CARDHOLDER_NAME_FIELD_NAME;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class PaymentRequestValidator {

    private static final String CONSTRAINT_MESSAGE_EXACT_STRING_TEMPLATE = "Must be exactly %d characters length";

    static final int CARD_BRAND_MAX_LENGTH = 20;
    public static final int AGREEMENT_ID_MAX_LENGTH = 26;
    static final int COUNTRY_EXACT_LENGTH = 2;
    
    public void validate(CreatePaymentRequest paymentRequest) {
        if (paymentRequest.hasPrefilledCardholderDetails()) 
            validatePrefilledCardholderDetails(paymentRequest.getPrefilledCardholderDetails());
    }
    
    private void validatePrefilledCardholderDetails(PrefilledCardholderDetails prefilledCardholderDetails) {
        if (prefilledCardholderDetails.getBillingAddress().isPresent()) {
            Address billingAddress = prefilledCardholderDetails.getBillingAddress().get();
            if (billingAddress.getCountry() != null) {
                validate(ExactLengthValidator.isValid(billingAddress.getCountry(), COUNTRY_EXACT_LENGTH),
                        aPaymentError(PREFILLED_ADDRESS_COUNTRY_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_EXACT_STRING_TEMPLATE, COUNTRY_EXACT_LENGTH)));
            }
        }
    }

    private static void validate(boolean condition, PaymentError error) {
        if (!condition) {
            throw new PaymentValidationException(error);
        }
    }

}
