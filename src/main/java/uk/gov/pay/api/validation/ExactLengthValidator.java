package uk.gov.pay.api.validation;


import static org.apache.commons.lang3.StringUtils.isBlank;

class ExactLengthValidator {
    static boolean isValid(String value, int length) {
        return isBlank(value) || value.length() == length;
    }
}
