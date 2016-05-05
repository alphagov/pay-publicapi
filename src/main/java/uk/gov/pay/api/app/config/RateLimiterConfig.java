package uk.gov.pay.api.app.config;

import io.dropwizard.Configuration;

import javax.validation.constraints.Min;

public class RateLimiterConfig extends Configuration {

    @Min(1)
    private int rate;

    @Min(500)
    private int perMillis;

    public int getRate() {
        return rate;
    }

    public int getPerMillis() {
        return perMillis;
    }
}
