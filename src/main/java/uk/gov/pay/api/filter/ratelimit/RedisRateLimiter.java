package uk.gov.pay.api.filter.ratelimit;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.OutOfScopeException;
import io.dropwizard.core.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.RateLimiterConfig;
import uk.gov.pay.api.filter.RateLimiterKey;
import uk.gov.pay.api.managed.RedisClientManager;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.concurrent.Callable;

@Singleton
public class RedisRateLimiter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRateLimiter.class);
    private final MetricRegistry metricsRegistry;

    private RateLimitManager rateLimitManager;
    private RedisClientManager redisClientManager;

    @Inject
    public RedisRateLimiter(RateLimiterConfig rateLimiterConfig, RedisClientManager redisClientManager, Environment environment) {
        this.rateLimitManager = new RateLimitManager(rateLimiterConfig);
        this.redisClientManager = redisClientManager;
        this.metricsRegistry = environment.metrics();
    }

    void checkRateOf(String accountId, RateLimiterKey key)
            throws RedisException, RateLimitException {

        Long count;

        int rateLimitInterval = rateLimitManager.getRateLimitInterval(accountId);
        count = updateAllowance(key.getKey(), rateLimitInterval);

        if (count != null) {
            int allowedNumberOfRequests = rateLimitManager.getAllowedNumberOfRequests(key, accountId);
            if (count > allowedNumberOfRequests) {
                LOGGER.info(String.format("RedisRateLimiter - Rate limit exceeded for account [%s] and method [%s] - count: %d, rate allowed: %d",
                        accountId, key.getKeyType(), count, allowedNumberOfRequests));
                throw new RateLimitException();
            }
        }
    }

    /**
     * @param key the {@link RateLimiterKey#getKey()} key
     * @param rateLimitInterval in milliseconds
     * @return the count so far within the rateLimitInterval
     * @throws RedisException
     */
    private Long updateAllowance(String key, int rateLimitInterval) throws RedisException {
        String derivedKey = getKeyForWindow(key, rateLimitInterval);
        
        Long count = 0L;
        try {
            count = time("redis.incr", () -> redisClientManager.getRedisConnection().sync().incr(derivedKey));
        } catch (Exception e) {
            LOGGER.info(String.format("Failed to increment redis key %s. Cause of error: %s", derivedKey, e));
            throw new RedisException();
        }
        
        try {
            // expire key after a (rateLimitInterval / 1000) so they are not on redis forever
            time("redis.expire", () -> redisClientManager.getRedisConnection().sync().expire(derivedKey, rateLimitInterval / 1000));
        } catch (Exception e) {
            LOGGER.info(String.format("Failed to expire redis key %s. Cause of error: %s", derivedKey, e));
            throw new RedisException();
        }
        
        return count;
    }

    private <T> T time(String metricName, Callable<T> callable) throws Exception {
        return this.metricsRegistry.timer(metricName).time(callable);
    }

    /**
     * Derives Key (Service Key + Window) to use in Redis for noOfReq limiting.
     * Recommended to use perMillis to lowest granularity. i.e, to seconds. 1000, 2000
     * <p>
     * Depends on perMillis
     * <p>
     * - perMillis < 1000 : Window considered for milliseconds
     * - perMillis >=1000 && <60000 : Window considered for seconds(s)
     * - perMillis >=60000 && <3600000 : Window considered for minute(s)
     *
     * @return key based on {@link RateLimiterKey#getKey()} and current second/minute/hour window 
     */
    private String getKeyForWindow(String key, int rateLimitInterval) throws OutOfScopeException {

        LocalDateTime now = LocalDateTime.now();

        int window;

        if (rateLimitInterval >= 1 && rateLimitInterval < 1000) {
            window = (now.get(ChronoField.MILLI_OF_DAY) / rateLimitInterval) + 1;
        } else if (rateLimitInterval >= 1000 && rateLimitInterval < 60000) {
            window = now.get(ChronoField.SECOND_OF_MINUTE) / (rateLimitInterval / 1000);
        } else if (rateLimitInterval >= 60000 && rateLimitInterval < 3600000) {
            window = now.get(ChronoField.MINUTE_OF_HOUR) / (rateLimitInterval / 1000);
        } else {
            throw new OutOfScopeException("Rate limit interval specified is not currently supported");
        }

        return key + window;
    }
}
