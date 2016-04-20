package uk.gov.pay.api.validation;

import uk.gov.pay.api.exception.ValidationException;
import uk.gov.pay.api.model.ExternalChargeStatus;
import uk.gov.pay.api.utils.DateTimeUtils;

import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;
import static org.eclipse.jetty.util.StringUtil.isBlank;
import static uk.gov.pay.api.model.PaymentError.Code.SEARCH_PAYMENTS_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;
import static uk.gov.pay.api.resources.PaymentsResource.*;
import static uk.gov.pay.api.validation.MaxLengthValidator.isValid;
import static uk.gov.pay.api.validation.PaymentRequestValidator.REFERENCE_MAX_LENGTH;

public class PaymentSearchValidator {

    public static void validateSearchParameters(String status, String reference, String fromDate, String toDate) {
        List<String> validationErrors = new LinkedList<>();

        validateStatus(status, validationErrors);
        validateReference(reference, validationErrors);
        validateFromDate(fromDate, validationErrors);
        validateToDate(toDate, validationErrors);

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(aPaymentError(SEARCH_PAYMENTS_VALIDATION_ERROR, join(validationErrors, ", ")));
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

    private static void validateStatus(String status, List<String> validationErrors) {
        if (!validateStatus(status)) {
            validationErrors.add(STATUS_KEY);
        }
    }

    private static boolean validateDate(String value) {
        return isBlank(value) || DateTimeUtils.toUTCZonedDateTime(value).isPresent();
    }

    private static boolean validateStatus(String value) {
        return isBlank(value) || ExternalChargeStatus.mapFromStatus(value).isPresent();
    }
}
