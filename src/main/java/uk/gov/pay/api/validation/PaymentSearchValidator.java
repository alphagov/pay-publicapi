package uk.gov.pay.api.validation;

import org.apache.commons.lang3.StringUtils;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.ValidationException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.join;
import static org.eclipse.jetty.util.StringUtil.isBlank;
import static org.eclipse.jetty.util.StringUtil.isNotBlank;
import static uk.gov.pay.api.model.PaymentError.Code.SEARCH_PAYMENTS_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;
import static uk.gov.pay.api.validation.MaxLengthValidator.isValid;
import static uk.gov.pay.api.validation.PaymentRequestValidator.AGREEMENT_ID_MAX_LENGTH;
import static uk.gov.pay.api.validation.PaymentRequestValidator.CARD_BRAND_MAX_LENGTH;
import static uk.gov.pay.api.validation.PaymentRequestValidator.EMAIL_MAX_LENGTH;
import static uk.gov.pay.api.validation.PaymentRequestValidator.REFERENCE_MAX_LENGTH;


public class PaymentSearchValidator {
    // we should really find a way to not have this anywhere but in the connector...
    public static final Set<String> VALID_CARD_PAYMENT_STATES =
            new HashSet<>(Arrays.asList("created", "started", "submitted", "success", "failed", "cancelled", "error"));

    public static final Set<String> VALID_DIRECT_DEBIT_STATES =
            new HashSet<>(Arrays.asList("started", "pending", "success", "failed", "cancelled"));

    public static void validateSearchParameters(Account account, String state, String reference, String email, String cardBrand,
                                                String fromDate, String toDate, String pageNumber,
                                                String displaySize, String agreementId) {
        List<String> validationErrors = new LinkedList<>();
        try {
            validateState(account, state, validationErrors);
            validateReference(reference, validationErrors);
            validateEmail(email, validationErrors);
            validateCardBrand(cardBrand, validationErrors);
            validateFromDate(fromDate, validationErrors);
            validateToDate(toDate, validationErrors);
            validatePageIfNotNull(pageNumber, validationErrors);
            validateDisplaySizeIfNotNull(displaySize, validationErrors);
            validateAgreement(agreementId, validationErrors);
        } catch (Exception e) {
            throw new ValidationException(aPaymentError(SEARCH_PAYMENTS_VALIDATION_ERROR, join(validationErrors, ", "), e.getMessage()));
        }
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(aPaymentError(SEARCH_PAYMENTS_VALIDATION_ERROR, join(validationErrors, ", ")));
        }
    }

    private static void validateAgreement(String agreement, List<String> validationErrors) {
        if (!isValid(agreement, AGREEMENT_ID_MAX_LENGTH)){
                validationErrors.add("agreement");
        }
    }

    private static void validatePageIfNotNull(String pageNumber, List<String> validationErrors) {
        if (isNotBlank(pageNumber) && (!StringUtils.isNumeric(pageNumber) || Integer.valueOf(pageNumber) < 1)) {
            validationErrors.add("page");
        }
    }

    private static void validateDisplaySizeIfNotNull(String displaySize, List<String> validationErrors) {
        if (isNotBlank(displaySize) && (!StringUtils.isNumeric(displaySize) || Integer.valueOf(displaySize) < 1 || Integer.valueOf(displaySize) > 500)) {
            validationErrors.add("display_size");
        }
    }

    private static void validateToDate(String toDate, List<String> validationErrors) {
        if (!validateDate(toDate)) {
            validationErrors.add("to_date");
        }
    }

    private static void validateFromDate(String fromDate, List<String> validationErrors) {
        if (!validateDate(fromDate)) {
            validationErrors.add("from_date");
        }
    }

    private static void validateReference(String reference, List<String> validationErrors) {
        if (!isValid(reference, REFERENCE_MAX_LENGTH)) {
            validationErrors.add("reference");
        }
    }

    private static void validateEmail(String email, List<String> validationErrors) {
        if (!isValid(email, EMAIL_MAX_LENGTH)) {
            validationErrors.add("email");
        }
    }

    private static void validateCardBrand(String cardBrand, List<String> validationErrors) {
        if (!isValid(cardBrand, CARD_BRAND_MAX_LENGTH)) {
            validationErrors.add("card_brand");
        }
    }

    private static void validateState(Account account, String state, List<String> validationErrors) {
        if (!validateState(account, state)) {
            validationErrors.add("state");
        }
    }

    private static boolean validateDate(String value) {
        return DateValidator.validate(value);
    }

    private static boolean validateState(Account account, String state) {
        return isBlank(state) || 
                (isDirectDebitAccount(account) ? VALID_DIRECT_DEBIT_STATES.contains(state) :
                VALID_CARD_PAYMENT_STATES.contains(state));
    }

    private static boolean isDirectDebitAccount(Account account) {
        return account.getPaymentType().equals(DIRECT_DEBIT);
    }
}
