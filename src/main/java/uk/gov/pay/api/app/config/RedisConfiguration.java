package uk.gov.pay.api.app.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.Duration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

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
    @JsonProperty("reconnectDelayLowerBound")
    private Duration reconnectDelayLowerBound;

    @Valid
    @NotNull
    @JsonProperty("reconnectDelayUpperBound")
    private Duration reconnectDelayUpperBound;

    @Valid
    @NotNull
    @JsonProperty("reconnectDelayBase")
    private long reconnectDelayBase;

    public String getUrl() {
        return format("%s://%s", ssl ? "rediss" : "redis", endpoint);
    }

    public Long getCommandTimeout() {
        return commandTimeout.toMilliseconds();
    }

    public Long getConnectTimeout() {
        return connectTimeout.toMilliseconds();
    }

    public Long getReconnectDelayLowerBound() {
        return reconnectDelayLowerBound.toMilliseconds();
    }

    public Long getReconnectDelayUpperBound() {
        return reconnectDelayUpperBound.toMilliseconds();
    }

    public long getReconnectDelayBase() {
        return reconnectDelayBase;
    }
}
