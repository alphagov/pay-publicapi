package uk.gov.pay.api.validation;

import org.apache.commons.validator.routines.UrlValidator;

import static org.apache.commons.validator.routines.UrlValidator.*;

public enum URLValidator {
    SECURITY_ENABLED {
        private final UrlValidator URL_VALIDATOR = new UrlValidator(new String[]{"https"});

        @Override
        public boolean isValid(String value) {
            return URL_VALIDATOR.isValid(value);
        }

    }, SECURITY_DISABLED {
        private final UrlValidator URL_VALIDATOR = new UrlValidator(new String[]{"http", "https"}, ALLOW_LOCAL_URLS);

        @Override
        public boolean isValid(String value) {
            return URL_VALIDATOR.isValid(value);
        }
    };

    public static URLValidator urlValidatorValueOf(boolean isDisabledSecureConnection) {
        return isDisabledSecureConnection ? SECURITY_DISABLED : SECURITY_ENABLED;
    }

    public abstract boolean isValid(String value);
}
