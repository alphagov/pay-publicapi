package uk.gov.pay.api.app.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.Duration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static java.lang.String.format;

public class RedisConfiguration {

    @Valid
    @NotNull
    @JsonProperty("endpoint")
    private String endpoint;

    @Valid
    @JsonProperty("ssl")
    private boolean ssl;

    @Valid
    @JsonProperty("commandTimeout")
    private Duration commandTimeout;

    @Valid
    @JsonProperty("connectTimeout")
    private Duration connectTimeout;

    @Valid
    @NotNull
    @JsonProperty("exponentialReconnectDelayLowerBound")
    private Duration exponentialReconnectDelayLowerBound;

    @Valid
    @NotNull
    @JsonProperty("exponentialReconnectDelayUpperBound")
    private Duration exponentialReconnectDelayUpperBound;

    @Valid
    @NotNull
    @JsonProperty("reconnectDelayExponentBase")
    private long reconnectDelayExponentBase;

    public String getUrl() {
        return format("%s://%s", ssl ? "rediss" : "redis", endpoint);
    }

    public Long getCommandTimeout() {
        return commandTimeout.toMilliseconds();
    }

    public Long getConnectTimeout() {
        return connectTimeout.toMilliseconds();
    }

    public Long getExponentialReconnectDelayLowerBound() {
        return exponentialReconnectDelayLowerBound.toMilliseconds();
    }

    public Long getExponentialReconnectDelayUpperBound() {
        return exponentialReconnectDelayUpperBound.toMilliseconds();
    }

    public long getReconnectDelayExponentBase() {
        return reconnectDelayExponentBase;
    }
}
