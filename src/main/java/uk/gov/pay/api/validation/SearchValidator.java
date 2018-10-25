package uk.gov.pay.api.validation;

import java.util.List;

import static org.eclipse.jetty.util.StringUtil.isNotBlank;


class SearchValidator {

    static void validatePageIfNotNull(String pageNumber, List<String> validationErrors) {
        if (isNotBlank(pageNumber) && (!NumericValidator.isValid(pageNumber) || Integer.valueOf(pageNumber) < 1)) {
            validationErrors.add("page");
        }
    }

    static void validateDisplaySizeIfNotNull(String displaySize, List<String> validationErrors) {
        if (isNotBlank(displaySize) && (!NumericValidator.isValid(displaySize) || Integer.valueOf(displaySize) < 1 || Integer.valueOf(displaySize) > 500)) {
            validationErrors.add("display_size");
        }
    }

    static void validateToDate(String toDate, List<String> validationErrors) {
        if (!DateValidator.validate(toDate)) {
            validationErrors.add("to_date");
        }
    }

    static void validateFromDate(String fromDate, List<String> validationErrors) {
        if (!DateValidator.validate(fromDate)) {
            validationErrors.add("from_date");
        }
    }
}
