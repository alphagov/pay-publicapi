package uk.gov.pay.api.filter.ratelimit;

import com.google.inject.Inject;
import com.google.inject.OutOfScopeException;
import io.lettuce.core.api.StatefulRedisConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.filter.RateLimiterKey;

import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

public class RedisRateLimiter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRateLimiter.class);

    private RateLimitManager rateLimitManager;
    private final int perMillis;
    private StatefulRedisConnection redisConnection;

    @Inject
    public RedisRateLimiter(RateLimitManager rateLimitManager, int perMillis, StatefulRedisConnection redisConnection) {
        this.rateLimitManager = rateLimitManager;
        this.perMillis = perMillis;
        this.redisConnection = redisConnection;
    }

    /**
     * @throws RateLimitException
     */
    synchronized void checkRateOf(String accountId, RateLimiterKey key)
            throws RedisException, RateLimitException {

        Long count;

        try {
            count = updateAllowance(key.getKey());
        } catch (Exception e) {
            // Exception possible if redis is unavailable or perMillis is too high        
            throw new RedisException();
        }

        if (count != null) {
            int allowedNumberOfRequests = rateLimitManager.getAllowedNumberOfRequests(key, accountId);
            if (count > allowedNumberOfRequests) {
                LOGGER.info(String.format("RedisRateLimiter - Rate limit exceeded for account [%s] and method [%s] - count: %d, rate allowed: %d",
                        accountId, key.getKeyType(), count, allowedNumberOfRequests));
                throw new RateLimitException();
            }
        }
    }

    synchronized private Long updateAllowance(String key) {
        String derivedKey = getKeyForWindow(key);

        Long count = redisConnection.sync().incr(derivedKey);
        if (count == 1L) {
            redisConnection.sync().expire(derivedKey, perMillis / 1000);
        }
        return count;
    }

    /**
     * Derives Key (Service Key + Window) to use in Redis for noOfReq limiting.
     * Recommended to use perMillis to lowest granularity. i.e, to seconds. 1000, 2000
     * <p>
     * Depends on perMillis
     * <p>
     * - perMillis < 1000 : Window considered for milliseconds
     * - perMillis >=1000 && <=60000 : Window considered for seconds(s)
     *
     * @return new key based on perMillis (works for second/minute/hour windows only)
     */
    private String getKeyForWindow(String key) throws OutOfScopeException {

        LocalDateTime now = LocalDateTime.now();

        int window;

        if (perMillis >= 1 && perMillis < 1000) {
            window = (now.get(ChronoField.MILLI_OF_DAY) / perMillis) + 1;
        } else if (perMillis >= 1000 && perMillis <= 60000) {
            window = now.get(ChronoField.SECOND_OF_MINUTE) / (perMillis / 1000);
        } else {
            throw new OutOfScopeException("perMillis specified is not currently supported");
        }

        return key + window;
    }

}
