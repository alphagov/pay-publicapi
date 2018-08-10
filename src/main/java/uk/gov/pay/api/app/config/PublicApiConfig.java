package uk.gov.pay.api.app.config;

import com.bendb.dropwizard.redis.JedisFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.cache.CacheBuilderSpec;
import io.dropwizard.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PublicApiConfig extends Configuration {

    @NotNull
    private String baseUrl;

    @NotNull
    private String connectorUrl;

    @NotNull
    private String connectorDDUrl;

    @NotNull
    private String publicAuthUrl;

    @NotNull
    private String graphiteHost;
    @NotNull
    private String graphitePort;

    @NotNull
    private Boolean allowHttpForReturnUrl;

    private String apiKeyHmacSecret;

    @NotNull
    private CacheBuilderSpec authenticationCachePolicy;
    
    @Valid
    @NotNull
    @JsonProperty("jerseyClientConfig")
    private RestClientConfig restClientConfig;

    @NotNull
    @JsonProperty
    private JedisFactory redis;
    
    @Valid
    @NotNull
    @JsonProperty("rateLimiter")
    private RateLimiterConfig rateLimiterConfig;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getConnectorUrl() {
        return connectorUrl;
    }
    public String getConnectorDDUrl() {
        return connectorDDUrl;
    }

    public String getPublicAuthUrl() {
        return publicAuthUrl;
    }

    public String getGraphiteHost() {
        return graphiteHost;
    }

    public String getGraphitePort() {
        return graphitePort;
    }

    public Boolean getAllowHttpForReturnUrl() {
        return allowHttpForReturnUrl;
    }

    public String getApiKeyHmacSecret() {
        return apiKeyHmacSecret;
    }

    public RestClientConfig getRestClientConfig() {
        return restClientConfig;
    }

    public RateLimiterConfig getRateLimiterConfig() {
        return rateLimiterConfig;
    }

    public CacheBuilderSpec getAuthenticationCachePolicy() { return authenticationCachePolicy; }

    public JedisFactory getJedisFactory() { return redis;  }
}
