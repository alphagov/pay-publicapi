package uk.gov.pay.api.config;

import io.dropwizard.Configuration;

public class PublicApiConfig extends Configuration {
    private String connectorUrl;

    public String getConnectorUrl() {
        return connectorUrl;
    }
}
