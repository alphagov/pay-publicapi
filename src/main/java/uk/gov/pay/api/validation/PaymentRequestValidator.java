package uk.gov.pay.api.validation;

import uk.gov.pay.api.exception.PaymentValidationException;
import uk.gov.pay.api.model.Address;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.PrefilledCardholderDetails;

import static java.lang.String.format;
import static uk.gov.pay.api.model.CreatePaymentRequest.AGREEMENT_ID_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.AMOUNT_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.DESCRIPTION_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.EMAIL_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.LANGUAGE_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_CITY_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_COUNTRY_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_LINE1_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_LINE2_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_ADDRESS_POSTCODE_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.PREFILLED_CARDHOLDER_NAME_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.REFERENCE_FIELD_NAME;
import static uk.gov.pay.api.model.CreatePaymentRequest.RETURN_URL_FIELD_NAME;
import static uk.gov.pay.api.model.PaymentError.Code.CREATE_PAYMENT_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

public class PaymentRequestValidator {

    static final String CONSTRAINT_GREATER_THAN_MESSAGE_INT_TEMPLATE = "Must be greater than or equal to %d";
    static final String CONSTRAINT_LESS_THAN_MESSAGE_INT_TEMPLATE = "Must be less than or equal to %d";
    private static final String CONSTRAINT_MESSAGE_STRING_TEMPLATE = "Must be less than or equal to %d characters length";
    private static final String CONSTRAINT_MESSAGE_EXACT_STRING_TEMPLATE = "Must be exactly %d characters length";
    private static final String URL_FORMAT_MESSAGE = "Must be a valid URL format";

    static final int AMOUNT_MAX_VALUE = 10000000;
    static final int AMOUNT_MIN_VALUE = 1;

    static final int DESCRIPTION_MAX_LENGTH = 255;
    static final int URL_MAX_LENGTH = 2000;
    static final int REFERENCE_MAX_LENGTH = 255;
    static final int EMAIL_MAX_LENGTH = 254;
    static final int CARD_BRAND_MAX_LENGTH = 20;
    static final int AGREEMENT_ID_MAX_LENGTH = 26;
    static final int CARDHOLDER_NAME_MAX_LENGTH = 255;
    static final int ADDRESS_LINE1_MAX_LENGTH = 255;
    static final int ADDRESS_LINE2_MAX_LENGTH = 255;
    static final int POSTCODE_MAX_LENGTH = 25;
    static final int CITY_MAX_LENGTH = 255;
    static final int COUNTRY_EXACT_LENGTH = 2;

    private URLValidator urlValidator;

    public PaymentRequestValidator(URLValidator urlValidator) {
        this.urlValidator = urlValidator;
    }

    public void validate(CreatePaymentRequest paymentRequest) {
        if (paymentRequest.hasAgreementId()) {
            validateAgreementId(paymentRequest.getAgreementId());
        }

        if (paymentRequest.hasReturnUrl()) {
            validateReturnUrl(paymentRequest.getReturnUrl());
        }

        if (paymentRequest.hasLanguage()) {
            validateLanguage(paymentRequest.getLanguage());
        }
        
        if (paymentRequest.hasEmail()) {
            validateEmail(paymentRequest.getEmail());
        }
        
        if (paymentRequest.hasPrefilledCardholderDetails()) {
            validatePrefilledCardholderDetails(paymentRequest.getPrefilledCardholderDetails());
        }

        validateAmount(paymentRequest.getAmount());
        validateReference(paymentRequest.getReference());
        validateDescription(paymentRequest.getDescription());
    }

    private void validateAgreementId(String agreementId) {
        validate(MaxLengthValidator.isValid(agreementId, AGREEMENT_ID_MAX_LENGTH),
                aPaymentError(AGREEMENT_ID_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, AGREEMENT_ID_MAX_LENGTH)));
    }

    private void validateAmount(int amount) {
        validate(amount >= AMOUNT_MIN_VALUE,
                aPaymentError(AMOUNT_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_GREATER_THAN_MESSAGE_INT_TEMPLATE, AMOUNT_MIN_VALUE)));

        validate(amount <= AMOUNT_MAX_VALUE,
                aPaymentError(AMOUNT_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_LESS_THAN_MESSAGE_INT_TEMPLATE, AMOUNT_MAX_VALUE)));
    }

    private void validateReturnUrl(String returnUrl) {
        validate(MaxLengthValidator.isValid(returnUrl, URL_MAX_LENGTH),
                aPaymentError(RETURN_URL_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, URL_MAX_LENGTH)));

        validate(urlValidator.isValid(returnUrl),
                aPaymentError(RETURN_URL_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, URL_FORMAT_MESSAGE));
    }

    private void validateReference(String reference) {
        validate(MaxLengthValidator.isValid(reference, REFERENCE_MAX_LENGTH),
                aPaymentError(REFERENCE_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, REFERENCE_MAX_LENGTH)));
    }

    private void validateDescription(String description) {
        validate(MaxLengthValidator.isValid(description, DESCRIPTION_MAX_LENGTH),
                aPaymentError(DESCRIPTION_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, DESCRIPTION_MAX_LENGTH)));
    }

    private void validateLanguage(String language) {
        validate(LanguageValidator.isValid(language),
                aPaymentError(LANGUAGE_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, LanguageValidator.ERROR_MESSAGE));
    }

    private static void validateEmail(String email) {
        validate(MaxLengthValidator.isValid(email, EMAIL_MAX_LENGTH),
                aPaymentError(EMAIL_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, EMAIL_MAX_LENGTH)));
    }

    private void validatePrefilledCardholderDetails(PrefilledCardholderDetails prefilledCardholderDetails) {
        if (prefilledCardholderDetails.getCardholderName().isPresent()) {
            validate(MaxLengthValidator.isValid(prefilledCardholderDetails.getCardholderName().get(), CARDHOLDER_NAME_MAX_LENGTH),
                    aPaymentError(PREFILLED_CARDHOLDER_NAME_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, CARDHOLDER_NAME_MAX_LENGTH)));
        }
        if (prefilledCardholderDetails.getBillingAddress().isPresent()) {
            Address billingAddress = prefilledCardholderDetails.getBillingAddress().get();
            if (billingAddress.getLine1() != null) {
                validate(MaxLengthValidator.isValid(billingAddress.getLine1(), ADDRESS_LINE1_MAX_LENGTH),
                        aPaymentError(PREFILLED_ADDRESS_LINE1_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, ADDRESS_LINE1_MAX_LENGTH)));
            }
            if (billingAddress.getLine2() != null) {
                validate(MaxLengthValidator.isValid(billingAddress.getLine2(), ADDRESS_LINE2_MAX_LENGTH),
                        aPaymentError(PREFILLED_ADDRESS_LINE2_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, ADDRESS_LINE2_MAX_LENGTH)));
            }
            if (billingAddress.getPostcode() != null) {
                validate(MaxLengthValidator.isValid(billingAddress.getPostcode(), POSTCODE_MAX_LENGTH),
                        aPaymentError(PREFILLED_ADDRESS_POSTCODE_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, POSTCODE_MAX_LENGTH)));
            }
            if (billingAddress.getCity() != null) {
                validate(MaxLengthValidator.isValid(billingAddress.getCity(), CITY_MAX_LENGTH),
                        aPaymentError(PREFILLED_ADDRESS_CITY_FIELD_NAME, CREATE_PAYMENT_VALIDATION_ERROR, format(CONSTRAINT_MESSAGE_STRING_TEMPLATE, CITY_MAX_LENGTH)));
            }
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
