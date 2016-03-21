package uk.gov.pay.api.validation;

import io.dropwizard.Configuration;
import uk.gov.pay.api.config.PublicApiConfig;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.pay.api.validation.URLValidator.urlValidatorValueOf;

/**
 * Validates a notEmpty URL using protocol http/https depending on isDisabledSecureConnection configuration value.
 */

public class URLConstraintValidator implements ConstraintValidator<URL, String>, ConfigurationAware {

    private PublicApiConfig config;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {

        boolean isValid = true;

        if (isNotEmpty(value)) {
            isValid = urlValidatorValueOf(isDisabledSecureConnection()).isValid(value);
        }

        return isValid;
    }

    private Boolean isDisabledSecureConnection() {
        return config.getRestClientConfig().isDisabledSecureConnection();
    }

    @Override
    public void initialize(URL url) {
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        config = ((PublicApiConfig) configuration);
    }
}
