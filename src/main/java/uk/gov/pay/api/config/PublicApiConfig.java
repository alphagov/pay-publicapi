package uk.gov.pay.api.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PublicApiConfig extends Configuration {
    @NotNull
    private String connectorUrl;

    @Valid
    @NotNull
    @JsonProperty("jerseyClient")
    private JerseyClientConfiguration jerseyClient;


    public String getConnectorUrl() {
        return connectorUrl;
    }

    public JerseyClientConfiguration getClientConfiguration() {
        return jerseyClient;
    }
}
