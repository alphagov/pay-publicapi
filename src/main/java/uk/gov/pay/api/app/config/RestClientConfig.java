package uk.gov.pay.api.app.config;

import io.dropwizard.Configuration;

public class RestClientConfig extends Configuration {
    private String disabledSecureConnection;

    public Boolean isDisabledSecureConnection() {
        return "true".equals(disabledSecureConnection);
    }

}
