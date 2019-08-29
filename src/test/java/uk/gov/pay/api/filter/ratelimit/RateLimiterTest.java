package uk.gov.pay.api.filter.ratelimit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RateLimiterTest {

    private static final String POST = "POST";
    private static final String accountId = "account-id";
    @Mock
    LocalRateLimiter localRateLimiter;
    @Mock
    RedisRateLimiter redisRateLimiter;
    RateLimiter rateLimiter;

    @Before
    public void setup() {
        rateLimiter = new RateLimiter(localRateLimiter, redisRateLimiter);
    }

    @Test
    public void shouldInvokeRedisRateLimiter_whenRedisDbIsAvaiable() throws Exception {
        String key = "key1";

        rateLimiter.checkRateOf(accountId, key, POST);
        rateLimiter.checkRateOf(accountId, key, POST);

        verify(redisRateLimiter, times(2)).checkRateOf(accountId, key, "POST");
    }

    @Test
    public void shouldInvokeLocalRateLimiter_whenRedisIsNotAvaiable() throws Exception {
        String key = "key2";

        doThrow(new RedisException()).when(redisRateLimiter).checkRateOf(accountId, key, POST);

        rateLimiter.checkRateOf(accountId, key, POST);
        rateLimiter.checkRateOf(accountId, key, POST);

        verify(localRateLimiter, times(2)).checkRateOf(key, POST);
    }
}
