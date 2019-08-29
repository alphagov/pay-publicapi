package uk.gov.pay.api.filter.ratelimit;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RedisRateLimiterTest {

    private static final String POST = "POST";
    private static final String accountId = "account-id";
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    JedisPool jedisPool;
    @Mock
    Jedis jedis;
    @Captor
    ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor;
    private RedisRateLimiter redisRateLimiter;
    private Appender<ILoggingEvent> mockAppender;

    @Before
    public void setup() {
        when(jedisPool.getResource()).thenReturn(jedis);

        Logger root = (Logger) LoggerFactory.getLogger(RedisRateLimiter.class);
        mockAppender = mock(Appender.class);
        root.setLevel(Level.INFO);
        root.addAppender(mockAppender);
    }

    @Test
    public void rateLimiterSetTo_1CallPerSecond_shouldAllowSingleCall() throws Exception {
        redisRateLimiter = new RedisRateLimiter(1, 1, 1000, jedisPool);

        when(jedis.incr(anyString())).thenReturn(1L, 2L);
        redisRateLimiter.checkRateOf(accountId, "Key1", POST);
    }

    @Test
    public void rateLimiterSetTo_2CallsPerSecond_shouldAllow2ConsecutiveCallsWithSameKeys() throws Exception {
        String key = "Key2";
        redisRateLimiter = new RedisRateLimiter(2, 2, 1000, jedisPool);

        when(jedis.incr(anyString())).thenReturn(1L, 2L, 3L);

        redisRateLimiter.checkRateOf(accountId, key, POST);
        redisRateLimiter.checkRateOf(accountId, key, POST);

    }

    @Test(expected = RateLimitException.class)
    public void rateLimiterSetTo_2CallsPerSecond_shouldAFailWhen3ConsecutiveCallsWithSameKeysAreMade() throws Exception {
        String key = "Key3";
        redisRateLimiter = new RedisRateLimiter(2, 2, 1000, jedisPool);

        when(jedis.incr(anyString())).thenReturn(1L, 2L, 3L);

        try {
            redisRateLimiter.checkRateOf(accountId, key, POST);
            redisRateLimiter.checkRateOf(accountId, key, POST);
            redisRateLimiter.checkRateOf(accountId, key, POST);
        } catch (RedisException | RateLimitException e) {
            verify(mockAppender, times(1)).doAppend(loggingEventArgumentCaptor.capture());
            List<LoggingEvent> loggingEvents = loggingEventArgumentCaptor.getAllValues();
            assertEquals("rate limit exceeded for account [account-id] and method [POST] - count: 3, rate allowed: 2",
                    loggingEvents.get(0).getFormattedMessage());
            
            throw e;
        }
    }
}
