package uk.gov.pay.api.config;

import io.dropwizard.Configuration;

public class JerseyClientConfig extends Configuration {

    private String disabledSecureConnection;
    private String trustStoreFlie;
    private String trustStorePassword;
    private String keyStoreFile;
    private String keyStorePassword;

    public String getTrustStoreFlie() {
        return trustStoreFlie;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public Boolean isDisabledSecureConnection() {
        return "true".equals(disabledSecureConnection);
    }

}
