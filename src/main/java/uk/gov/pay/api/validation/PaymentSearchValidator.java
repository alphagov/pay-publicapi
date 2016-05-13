package uk.gov.pay.api.validation;

import uk.gov.pay.api.exception.ValidationException;
import uk.gov.pay.api.utils.DateTimeUtils;

import java.util.*;

import static org.apache.commons.lang3.StringUtils.join;
import static org.eclipse.jetty.util.StringUtil.isBlank;
import static uk.gov.pay.api.model.PaymentError.Code.SEARCH_PAYMENTS_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;
import static uk.gov.pay.api.resources.PaymentsResource.*;
import static uk.gov.pay.api.validation.MaxLengthValidator.isValid;
import static uk.gov.pay.api.validation.PaymentRequestValidator.REFERENCE_MAX_LENGTH;

public class PaymentSearchValidator {
    // we should really find a way to not have this anywhere but in the connector...
    // confirmed and captured should be removed once PP-541 ammendments are complete
    public static final Set<String> VALID_STATES =
        new HashSet<>(Arrays.asList("created", "started", "submitted", "success", "failed", "cancelled", "error", "confirmed", "captured"));

    public static void validateSearchParameters(String state, String reference, String fromDate, String toDate, String pageNumber, String displaySize) {
        List<String> validationErrors = new LinkedList<>();

        validateState(state, validationErrors);
        validateReference(reference, validationErrors);
        validateFromDate(fromDate, validationErrors);
        validateToDate(toDate, validationErrors);
        validatePageNum(pageNumber, validationErrors);
        validateDisplaySize(displaySize, validationErrors);

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(aPaymentError(SEARCH_PAYMENTS_VALIDATION_ERROR, join(validationErrors, ", ")));
        }
    }

    private static void validatePageNum(String pageNum, List<String> validationErrors) {
        if (pageNum!=null && Long.valueOf(pageNum) < 1) {
            validationErrors.add(PAGE);
        }
    }

    private static void validateDisplaySize(String displaySize, List<String> validationErrors) {
        if (displaySize!= null && Long.valueOf(displaySize) < 1) {
            validationErrors.add(DISPLAY_SIZE);
        }
    }

    private static void validateToDate(String toDate, List<String> validationErrors) {
        if (!validateDate(toDate)) {
            validationErrors.add(TO_DATE_KEY);
        }
    }

    private static void validateFromDate(String fromDate, List<String> validationErrors) {
        if (!validateDate(fromDate)) {
            validationErrors.add(FROM_DATE_KEY);
        }
    }

    private static void validateReference(String reference, List<String> validationErrors) {
        if (!isValid(reference, REFERENCE_MAX_LENGTH)) {
            validationErrors.add(REFERENCE_KEY);
        }
    }

    private static void validateState(String state, List<String> validationErrors) {
        if (!validateState(state)) {
            validationErrors.add(STATE_KEY);
        }
    }

    private static boolean validateDate(String value) {
        return isBlank(value) || DateTimeUtils.toUTCZonedDateTime(value).isPresent();
    }

    private static boolean validateState(String state) {
        return isBlank(state) || VALID_STATES.contains(state);
    }
}
