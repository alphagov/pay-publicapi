package uk.gov.pay.api.filter.ratelimit;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RateLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiter.class);

    private final LocalRateLimiter localRateLimiter;
    private final RedisRateLimiter redisRateLimiter;

    @Inject
    public RateLimiter(LocalRateLimiter localRateLimiter, RedisRateLimiter redisRateLimiter) {

        this.localRateLimiter = localRateLimiter;
        this.redisRateLimiter = redisRateLimiter;
    }

    public void checkRateOf(String key, String method) throws RateLimitException {
        try {
            redisRateLimiter.checkRateOf(key, method);
            LOGGER.info("Rate limit applied using RedisRateLimiter");
        } catch (RedisException e) {
            LOGGER.warn("Exception occurred checking rate limits using RedisRateLimiter, falling back to LocalRateLimiter");

            localRateLimiter.checkRateOf(key, method);
        }
    }

    public void auditRateOf(String key) {
        localRateLimiter.auditRateOf(key);
    }


}
