package uk.gov.pay.api.validation;

import java.util.List;

import static org.eclipse.jetty.util.StringUtil.isNotBlank;


public abstract class SearchValidator {

    protected static void validatePageIfNotNull(String pageNumber, List<String> validationErrors) {
        if (isNotBlank(pageNumber) && (!NumericValidator.isValid(pageNumber) || Integer.valueOf(pageNumber) < 1)) {
            validationErrors.add("page");
        }
    }

    protected static void validateDisplaySizeIfNotNull(String displaySize, List<String> validationErrors) {
        if (isNotBlank(displaySize) && (!NumericValidator.isValid(displaySize) || Integer.valueOf(displaySize) < 1 || Integer.valueOf(displaySize) > 500)) {
            validationErrors.add("display_size");
        }
    }
}
