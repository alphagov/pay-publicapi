package uk.gov.pay.api.filter.ratelimit;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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

public class LocalRateLimiterTest {

    private static final String POST = "POST";

    LocalRateLimiter localRateLimiter;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void rateLimiterSetTo_2CallsPerSecond_shouldAllow2ConsecutiveCallsWithSameKeys() throws Exception {

        String key = "key1";
        localRateLimiter = new LocalRateLimiter(2, 2, 1000);

        localRateLimiter.checkRateOf(key, POST);
        localRateLimiter.checkRateOf(key, POST);
    }

    @Test
    public void rateLimiterSetTo_1CallPer300Millis_shouldAFailWhen2ConsecutiveCallsWithSameKeysAreMade() throws Exception {

        String key = "key2";
        localRateLimiter = new LocalRateLimiter(1, 1, 300);

        localRateLimiter.checkRateOf(key, POST);

        expectedException.expect(RateLimitException.class);
        localRateLimiter.checkRateOf(key, POST);
    }

    @Test
    public void rateLimiterSetTo_3CallsPerSecond_shouldAllowMakingOnly3CallsWithSameKey() throws Exception {

        String key = "key3";
        localRateLimiter = new LocalRateLimiter(3, 3, 1000);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        List<Callable<String>> tasks = Arrays.asList(
                () -> {
                    localRateLimiter.checkRateOf(key, POST);
                    return "task1";
                },
                () -> {
                    localRateLimiter.checkRateOf(key, POST);
                    return "task2";
                },
                () -> {
                    localRateLimiter.checkRateOf(key, POST);
                    return "task3";
                },
                () -> {
                    localRateLimiter.checkRateOf(key, POST);
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
