package uk.gov.pay.api.config;

import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

public class PublicApiConfig extends Configuration {
    @NotNull
    private String connectorUrl;
    @NotNull
    private String publicAuthUrl;

    public String getConnectorUrl() {
        return connectorUrl;
    }

    public String getPublicAuthUrl() {
        return publicAuthUrl;
    }
}
