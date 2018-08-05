package uk.gov.pay.api.filter.ratelimit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RedisRateLimiterTest {

    @Mock
    JedisPool jedisPool;
    @Mock
    Jedis jedis;

    private static final String POST = "POST";

    private RedisRateLimiter redisRateLimiter;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        when(jedisPool.getResource()).thenReturn(jedis);
    }

    @Test
    public void rateLimiterSetTo_1CallPerSecond_shouldAllowSingleCall() throws Exception {

        String key = "Key1";
        redisRateLimiter = new RedisRateLimiter(1, 1, 1000, jedisPool);

        when(jedis.incr(anyString())).thenReturn(1L, 2L);
        redisRateLimiter.checkRateOf("Key1", POST);
    }

    @Test
    public void rateLimiterSetTo_2CallsPerSecond_shouldAllow2ConsecutiveCallsWithSameKeys() throws Exception {

        String key = "Key2";
        redisRateLimiter = new RedisRateLimiter(2, 2, 1000, jedisPool);

        when(jedis.incr(anyString())).thenReturn(1L, 2L, 3L);

        redisRateLimiter.checkRateOf(key, POST);
        redisRateLimiter.checkRateOf(key, POST);

    }

    @Test
    public void rateLimiterSetTo_2CallsPerSecond_shouldAFailWhen3ConsecutiveCallsWithSameKeysAreMade() throws Exception {

        String key = "Key3";
        redisRateLimiter = new RedisRateLimiter(2, 2, 1000, jedisPool);

        when(jedis.incr(anyString())).thenReturn(1L, 2L, 3L);

        redisRateLimiter.checkRateOf(key, POST);
        redisRateLimiter.checkRateOf(key, POST);

        expectedException.expect(RateLimitException.class);
        redisRateLimiter.checkRateOf(key, POST);

    }
}
