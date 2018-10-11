package uk.gov.pay.api.validation;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static org.eclipse.jetty.util.StringUtil.isNotBlank;


public abstract class SearchValidator {

    protected static void validatePageIfNotNull(String pageNumber, List<String> validationErrors) {
        if (isNotBlank(pageNumber) && (!StringUtils.isNumeric(pageNumber) || Integer.valueOf(pageNumber) < 1)) {
            validationErrors.add("page");
        }
    }

    protected static void validateDisplaySizeIfNotNull(String displaySize, List<String> validationErrors) {
        if (isNotBlank(displaySize) && (!StringUtils.isNumeric(displaySize) || Integer.valueOf(displaySize) < 1 || Integer.valueOf(displaySize) > 500)) {
            validationErrors.add("display_size");
        }
    }
}
