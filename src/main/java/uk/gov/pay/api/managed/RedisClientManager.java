package uk.gov.pay.api.managed;

import io.dropwizard.lifecycle.Managed;
import io.lettuce.core.RedisClient;
import uk.gov.pay.api.filter.ratelimit.RedisRateLimiter;

public class RedisClientManager implements Managed {
    private RedisClient redisClient;
    private RedisRateLimiter redisRateLimiter;

    public RedisClientManager(RedisClient redisClient, RedisRateLimiter redisRateLimiter) {
        this.redisClient = redisClient;
        this.redisRateLimiter = redisRateLimiter;
    }

    @Override
    public void start() throws Exception {}

    @Override
    public void stop() throws Exception {
        redisRateLimiter.closeRedisConnection();
        redisClient.shutdown();
    }
}
