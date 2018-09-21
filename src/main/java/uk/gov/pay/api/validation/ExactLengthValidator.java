package uk.gov.pay.api.validation;

import static org.eclipse.jetty.util.StringUtil.isBlank;

class ExactLengthValidator {
    static boolean isValid(String value, int length) {
        return isBlank(value) || value.length() == length;
    }
}
