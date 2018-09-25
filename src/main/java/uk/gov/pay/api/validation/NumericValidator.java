package uk.gov.pay.api.validation;

import static org.eclipse.jetty.util.StringUtil.isBlank;

class NumericValidator {
    static boolean isValid(String value) {
        return isBlank(value) || value.matches("[0-9]+");
    }
}
