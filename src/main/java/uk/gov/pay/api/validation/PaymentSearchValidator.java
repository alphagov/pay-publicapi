package uk.gov.pay.api.validation;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.PaymentValidationException;
import uk.gov.pay.api.service.PaymentSearchParams;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.join;
import static org.eclipse.jetty.util.StringUtil.isBlank;
import static uk.gov.pay.api.model.CreateCardPaymentRequest.EMAIL_MAX_LENGTH;
import static uk.gov.pay.api.model.CreateCardPaymentRequest.REFERENCE_MAX_LENGTH;
import static uk.gov.pay.api.model.CreateDirectDebitPaymentRequest.MANDATE_ID_MAX_LENGTH;
import static uk.gov.pay.api.model.PaymentError.Code.SEARCH_PAYMENTS_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;
import static uk.gov.pay.api.validation.MaxLengthValidator.isInvalid;
import static uk.gov.pay.api.validation.SearchValidator.validateDisplaySizeIfNotNull;
import static uk.gov.pay.api.validation.SearchValidator.validateFromDate;
import static uk.gov.pay.api.validation.SearchValidator.validateFromSettledDate;
import static uk.gov.pay.api.validation.SearchValidator.validatePageIfNotNull;
import static uk.gov.pay.api.validation.SearchValidator.validateToDate;
import static uk.gov.pay.api.validation.SearchValidator.validateToSettledDate;


public class PaymentSearchValidator {
    private static final int FIRST_DIGITS_CARD_NUMBER_LENGTH = 6;
    private static final int LAST_DIGITS_CARD_NUMBER_LENGTH = 4;

    // we should really find a way to not have this anywhere but in the connector...
    private static final Set<String> VALID_CARD_PAYMENT_STATES =
            new HashSet<>(Arrays.asList("created", "started", "submitted", "success", "failed", "cancelled", "error"));

    private static final Set<String> VALID_DIRECT_DEBIT_STATES =
            new HashSet<>(Arrays.asList("started", "pending", "success", "failed", "cancelled"));

    public static void validateSearchParameters(Account account, PaymentSearchParams searchParams) {
        validateSearchParameters(account, searchParams.getState(), searchParams.getReference(),
                searchParams.getEmail(), searchParams.getCardBrand(), searchParams.getFromDate(),
                searchParams.getToDate(), searchParams.getPageNumber(), searchParams.getDisplaySize(),
                searchParams.getAgreementId(), searchParams.getFirstDigitsCardNumber(),
                searchParams.getLastDigitsCardNumber(), searchParams.getFromSettledDate(),
                searchParams.getToSettledDate());
    }

    public static void validateSearchParameters(Account account,
                                                String state,
                                                String reference,
                                                String email,
                                                String cardBrand,
                                                String fromDate,
                                                String toDate,
                                                String pageNumber,
                                                String displaySize,
                                                String mandate_id,
                                                String firstDigitsCardNumber,
                                                String lastDigitsCardNumber,
                                                String fromSettledDate,
                                                String toSettledDate) {
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
            validateMandateId(mandate_id, validationErrors);
            validateFirstDigitsCardNumber(firstDigitsCardNumber, validationErrors);
            validateLastDigitsCardNumber(lastDigitsCardNumber, validationErrors);
            validateFromSettledDate(fromSettledDate, validationErrors);
            validateToSettledDate(toSettledDate, validationErrors);
        } catch (Exception e) {
            throw new PaymentValidationException(aPaymentError(SEARCH_PAYMENTS_VALIDATION_ERROR, join(validationErrors, ", "), e.getMessage()));
        }
        if (!validationErrors.isEmpty()) {
            throw new PaymentValidationException(aPaymentError(SEARCH_PAYMENTS_VALIDATION_ERROR, join(validationErrors, ", ")));
        }
    }

    private static void validateMandateId(String mandate_id, List<String> validationErrors) {
        if (isInvalid(mandate_id, MANDATE_ID_MAX_LENGTH)) {
            validationErrors.add("mandate_id");
        }
    }

    private static void validateFirstDigitsCardNumber(String firstDigitsCardNumber, List<String> validationErrors) {
        if (!ExactLengthOrEmptyValidator.isValid(firstDigitsCardNumber, FIRST_DIGITS_CARD_NUMBER_LENGTH) || !NumericValidator.isValidOrNull(firstDigitsCardNumber)) {
            validationErrors.add("first_digits_card_number");
        }
    }

    private static void validateLastDigitsCardNumber(String lastDigitsCardNumber, List<String> validationErrors) {
        if (!ExactLengthOrEmptyValidator.isValid(lastDigitsCardNumber, LAST_DIGITS_CARD_NUMBER_LENGTH) || !NumericValidator.isValidOrNull(lastDigitsCardNumber)) {
            validationErrors.add("last_digits_card_number");
        }
    }

    private static void validateReference(String reference, List<String> validationErrors) {
        if (isInvalid(reference, REFERENCE_MAX_LENGTH)) {
            validationErrors.add("reference");
        }
    }

    private static void validateEmail(String email, List<String> validationErrors) {
        if (isInvalid(email, EMAIL_MAX_LENGTH)) {
            validationErrors.add("email");
        }
    }

    private static void validateCardBrand(String cardBrand, List<String> validationErrors) {
        if (isInvalid(cardBrand, 20)) {
            validationErrors.add("card_brand");
        }
    }

    private static void validateState(Account account, String state, List<String> validationErrors) {
        if (!validateState(account, state)) {
            validationErrors.add("state");
        }
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
