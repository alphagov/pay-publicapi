package uk.gov.pay.api.validation;

import io.dropwizard.Configuration;
import uk.gov.pay.api.config.PublicApiConfig;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.MalformedURLException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Validates a notEmpty URL using protocol http/https depending on isDisabledSecureConnection configuration value.
 */

public class URLValidator implements ConstraintValidator<URL, CharSequence>, ConfigurationAware {

    private PublicApiConfig config;

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {

        if (isNotEmpty(value)) {
            java.net.URL url;
            try {
                url = new java.net.URL(value.toString());
            } catch (MalformedURLException malformedUrl) {
                return false;
            }
            return acceptedProtocols(config).contains(url.getProtocol());
        } else {
            return true;
        }
    }

    @Override
    public void initialize(URL url) {
    }

    @Override
    public void setConfiguration(Configuration configuration) {

        config = ((PublicApiConfig) configuration);
    }

    private List<String> acceptedProtocols(PublicApiConfig config) {
        List<String> acceptedProtocols = newArrayList("https");
        if (config.getRestClientConfig().isDisabledSecureConnection()) {
            acceptedProtocols.add("http");
        }
        return acceptedProtocols;
    }
}
