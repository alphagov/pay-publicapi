package uk.gov.pay.api.app.config;

import io.dropwizard.Configuration;

public class RateLimiterConfig extends Configuration {

    private int rate;
    private int perMillis;

    public int getRate() {
        return rate;
    }

    public int getPerMillis() {
        return perMillis;
    }
}
