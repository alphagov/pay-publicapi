package uk.gov.pay.api.managed;

import io.dropwizard.lifecycle.Managed;
import io.lettuce.core.RedisClient;

public class LettuceClientManager implements Managed {
    private RedisClient redisClient;

    public LettuceClientManager(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    @Override
    public void start() throws Exception {}

    @Override
    public void stop() throws Exception {
        redisClient.shutdown();
    }
}
