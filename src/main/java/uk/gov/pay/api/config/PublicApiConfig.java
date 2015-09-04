package uk.gov.pay.api.config;

import io.dropwizard.Configuration;

import javax.validation.constraints.NotNull;

public class PublicApiConfig extends Configuration {
    @NotNull
    private String connectorUrl;

    public String getConnectorUrl() {
        return connectorUrl;
    }

}
