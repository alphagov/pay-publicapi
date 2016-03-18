package uk.gov.pay.api.validation;

import io.dropwizard.Configuration;

public interface ConfigurationAware {

    void setConfiguration(Configuration configuration);
}
