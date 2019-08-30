package uk.gov.pay.api.filter.ratelimit;

import com.google.inject.Inject;
import com.google.inject.OutOfScopeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.ws.rs.HttpMethod;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;

public class RedisRateLimiter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisRateLimiter.class);

    private final int noOfReq;
    private final int noOfReqForPost;
    private final int perMillis;
    private JedisPool jedisPool;

    @Inject
    public RedisRateLimiter(int noOfReq, int noOfReqForPost, int perMillis, JedisPool jedisPool) {
        this.noOfReq = noOfReq;
        this.noOfReqForPost = noOfReqForPost;
        this.perMillis = perMillis;
        this.jedisPool = jedisPool;
    }

    /**
     * @throws RateLimitException
     */
    synchronized void checkRateOf(String accountId, String key, String method)
            throws RedisException, RateLimitException {

        Long count;

        try {
            count = updateAllowance(key);
        } catch (Exception e) {
            // Exception possible if redis is unavailable or perMillis is too high        
            throw new RedisException();
        }

        if (count != null && count > getNoOfReqForMethod(method)) {
            LOGGER.info(String.format("RedisRateLimiter - Rate limit exceeded for account [%s] and method [%s] - count: %d, rate allowed: %d", accountId, method, count, getNoOfReqForMethod(method)));
            throw new RateLimitException();
        }
    }

    synchronized private Long updateAllowance(String key) {
        String derivedKey = getKeyForWindow(key);
        
        try (Jedis jedis = getResource()) {
            Long count = jedis.incr(derivedKey);
            
            if (count == 1) {
                jedis.expire(derivedKey, perMillis / 1000);
            }
            return count;
        }

    }

    private Jedis getResource() {
        return jedisPool.getResource();
    }

    private int getNoOfReqForMethod(String method) {
        if (HttpMethod.POST.equals(method)) {
            return noOfReqForPost;
        }
        return noOfReq;
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
