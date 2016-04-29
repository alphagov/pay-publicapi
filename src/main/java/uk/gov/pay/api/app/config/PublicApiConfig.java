package uk.gov.pay.api.app.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PublicApiConfig extends Configuration {
    @NotNull
    private String connectorUrl;
    @NotNull
    private String publicAuthUrl;

    @Valid
    @NotNull
    @JsonProperty("jerseyClientConfig")
    private RestClientConfig restClientConfig;

    @JsonProperty("rateLimiter")
    private RateLimiterConfig rateLimiterConfig;

    public RestClientConfig getRestClientConfig() {
        return restClientConfig;
    }

    public String getConnectorUrl() {
        return connectorUrl;
    }

    public String getPublicAuthUrl() {
        return publicAuthUrl;
    }

    public RateLimiterConfig getRateLimiterConfig() {
        return rateLimiterConfig;
    }
}
