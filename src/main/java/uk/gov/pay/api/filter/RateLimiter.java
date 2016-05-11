package uk.gov.pay.api.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RateLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiter.class);

    private final int rate;
    private final int perMillis;
    private final Cache<String, RateLimit> cache;

    public RateLimiter(int rate, int perMillis) {
        this.rate = rate;
        this.perMillis = perMillis;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(perMillis, TimeUnit.MILLISECONDS)
                .build();
    }

    void checkRateOf(String key) throws RateLimitException {
        try {
            cache.get(key, () -> new RateLimit(rate, perMillis)).updateAllowance();
        } catch (ExecutionException e) {
            //ExecutionException is thrown when the valueLoader throws a checked exception.
            //We just create a new instance so no exceptions will be thrown, this should never happen.
            LOGGER.error("Unexpected error creating a Rate Limiter object in cache", e);
        }
    }
}
