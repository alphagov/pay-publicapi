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
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LocalRateLimiterTest {

    private static final String POST = "POST";
    private static final String accountId = "account-id";
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    LocalRateLimiter localRateLimiter;

    @Captor
    ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor;
    private Appender<ILoggingEvent> mockAppender;

    @Before
    public void setup() {
        Logger root = (Logger) LoggerFactory.getLogger(LocalRateLimiter.class);
        mockAppender = mock(Appender.class);
        root.setLevel(Level.INFO);
        root.addAppender(mockAppender);
    }

    @Test
    public void rateLimiterSetTo_2CallsPerSecond_shouldAllow2ConsecutiveCallsWithSameKeys() throws Exception {

        String key = "key1";
        localRateLimiter = new LocalRateLimiter(2, 2, 1000);

        localRateLimiter.checkRateOf(accountId, key, POST);
        localRateLimiter.checkRateOf(accountId, key, POST);
    }

    @Test(expected = RateLimitException.class)
    public void rateLimiterSetTo_1CallPer300Millis_shouldAFailWhen2ConsecutiveCallsWithSameKeysAreMade() throws RateLimitException {

        String key = "key2";
        localRateLimiter = new LocalRateLimiter(1, 1, 300);

        try {
            localRateLimiter.checkRateOf(accountId, key, POST);
            localRateLimiter.checkRateOf(accountId, key, POST);
        } catch (RateLimitException e) {
            verify(mockAppender, times(1)).doAppend(loggingEventArgumentCaptor.capture());
            List<LoggingEvent> loggingEvents = loggingEventArgumentCaptor.getAllValues();
            assertEquals("LocalRateLimiter - Rate limit exceeded for account [account-id] and method [POST] - count: 2, rate allowed: 1",
                    loggingEvents.get(0).getFormattedMessage());

            throw e;
        }
    }

    @Test
    public void rateLimiterSetTo_3CallsPerSecond_shouldAllowMakingOnly3CallsWithSameKey() throws Exception {

        String key = "key3";
        localRateLimiter = new LocalRateLimiter(3, 3, 1000);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        List<Callable<String>> tasks = Arrays.asList(
                () -> {
                    localRateLimiter.checkRateOf(accountId, key, POST);
                    return "task1";
                },
                () -> {
                    localRateLimiter.checkRateOf(accountId, key, POST);
                    return "task2";
                },
                () -> {
                    localRateLimiter.checkRateOf(accountId, key, POST);
                    return "task3";
                },
                () -> {
                    localRateLimiter.checkRateOf(accountId, key, POST);
                    return "task4";
                }
        );

        List<String> successfulTasks = executor.invokeAll(tasks)
                .stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        assertThat(e, is(instanceOf(ExecutionException.class)));
                        ExecutionException ex = (ExecutionException) e;
                        assertThat(ex.getCause(), is(instanceOf(RateLimitException.class)));
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        assertThat(successfulTasks.size(), is(3));
    }


}
