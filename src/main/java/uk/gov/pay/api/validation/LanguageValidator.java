package uk.gov.pay.api.validation;

import uk.gov.pay.commons.model.SupportedLanguage;

import java.util.Arrays;

public class LanguageValidator {

    public static final String ERROR_MESSAGE = "Must be \"en\" or \"cy\"";

    static boolean isValid(String value) {
        return Arrays.stream(SupportedLanguage.values()).anyMatch(supportedLanguage -> supportedLanguage.toString().equals(value));
    }

}
