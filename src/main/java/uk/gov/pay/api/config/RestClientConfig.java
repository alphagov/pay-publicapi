package uk.gov.pay.api.config;

import io.dropwizard.Configuration;

public class RestClientConfig extends Configuration {

    private String disabledSecureConnection;
    private String keyStoreDir;
    private String keyStoreFile;
    private String keyStorePassword;

    public String getKeyStoreDir() {
        return keyStoreDir.endsWith("/") ? keyStoreDir : keyStoreDir.concat("/");
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
