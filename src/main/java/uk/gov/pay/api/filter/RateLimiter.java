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
    private final int auditRate;
    private final int perMillis;
    private final Cache<String, RateLimit> cache;
    private final Cache<String, RateLimit> auditCache;

    public RateLimiter(int rate, int auditRate, int perMillis) {
        this.rate = rate;
        this.auditRate = auditRate;
        this.perMillis = perMillis;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(perMillis, TimeUnit.MILLISECONDS)
                .build();

        this.auditCache = CacheBuilder.newBuilder()
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

    void auditRateOf(String key) {
        try {
            auditCache.get(key, () -> new RateLimit(auditRate, perMillis)).updateAllowance();
        } catch (RateLimitException e) {
            LOGGER.info(String.format(
                    "Rate limit reached for rate limit key %s using rate of %d requests per %d ms",
                    key,
                    auditRate,
                    perMillis)
            );
        } catch (ExecutionException e) {
            //ExecutionException is thrown when the valueLoader throws a checked exception.
            //We just create a new instance so no exceptions will be thrown, this should never happen.
            LOGGER.error("Unexpected error creating a Rate Limiter object in cache", e);
        }
    }
}
