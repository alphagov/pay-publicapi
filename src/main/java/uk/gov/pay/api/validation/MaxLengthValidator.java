package uk.gov.pay.api.validation;

import static org.apache.commons.lang3.StringUtils.isBlank;

class MaxLengthValidator {
    static boolean isValid(String value, int maxLength) {
        return isBlank(value) || value.length() <= maxLength;
    }
}
