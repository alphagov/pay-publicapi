package uk.gov.pay.api.validation;

import uk.gov.pay.api.exception.PaymentValidationException;

import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;
import static uk.gov.pay.api.model.PaymentError.Code.SEARCH_PAYMENTS_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;


public class DirectDebitEventSearchValidator {

    public static void validateSearchParameters(String fromDate, String toDate, Integer displaySize) {
        List<String> validationErrors = new LinkedList<>();
        try {
            validateAfterDate(fromDate, validationErrors);
            validateBeforeDate(toDate, validationErrors);
            validateDisplaySizeIfNotNull(displaySize, validationErrors);
        } catch (Exception e) {
            throw new PaymentValidationException(aPaymentError(SEARCH_PAYMENTS_VALIDATION_ERROR, join(validationErrors, ", "), e.getMessage()));
        }
        if (!validationErrors.isEmpty()) {
            throw new PaymentValidationException(aPaymentError(SEARCH_PAYMENTS_VALIDATION_ERROR, join(validationErrors, ", ")));
        }
    }
    
    private static void validateDisplaySizeIfNotNull(Integer displaySize, List<String> validationErrors) {
        if (displaySize != null && (displaySize < 1 || displaySize > 500)) {
            validationErrors.add("display_size");
        }
    }

    private static void validateBeforeDate(String toDate, List<String> validationErrors) {
        if (!validateDate(toDate)) {
            validationErrors.add("from_date");
        }
    }

    private static void validateAfterDate(String fromDate, List<String> validationErrors) {
        if (!validateDate(fromDate)) {
            validationErrors.add("to_date");
        }
    }

    private static boolean validateDate(String value) {
        return DateValidator.isValid(value);
    }
}
