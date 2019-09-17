package uk.gov.pay.api.filter.ratelimit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.pay.api.filter.RateLimiterKey;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RateLimiterTest {

    private static final String POST = "POST";
    private static final String accountId = "account-id";
    @Mock
    LocalRateLimiter localRateLimiter;
    @Mock
    RedisRateLimiter redisRateLimiter;
    @Mock
    private RateLimiterKey rateLimiterKey;
    RateLimiter rateLimiter;

    @Before
    public void setup() {
        rateLimiter = new RateLimiter(localRateLimiter, redisRateLimiter);
    }

    @Test
    public void shouldInvokeRedisRateLimiter_whenRedisDbIsAvaiable() throws Exception {
        rateLimiter.checkRateOf(accountId, rateLimiterKey, POST);
        rateLimiter.checkRateOf(accountId, rateLimiterKey, POST);

        verify(redisRateLimiter, times(2)).checkRateOf(accountId, rateLimiterKey);
    }

    @Test
    public void shouldInvokeLocalRateLimiter_whenRedisIsNotAvaiable() throws Exception {
        String key = "key2";
        when(rateLimiterKey.getKey()).thenReturn(key);

        doThrow(new RedisException()).when(redisRateLimiter).checkRateOf(accountId, rateLimiterKey);

        rateLimiter.checkRateOf(accountId, rateLimiterKey, POST);
        rateLimiter.checkRateOf(accountId, rateLimiterKey, POST);

        verify(localRateLimiter, times(2)).checkRateOf(accountId, key, POST);
    }
}
