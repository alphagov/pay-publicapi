package uk.gov.pay.api.filter;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.fail;

public class RateLimiterTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void rateLimiterSetTo_1CallPerSecond_shouldAllowSingleCall() throws Exception {
        RateLimiter rateLimiter = new RateLimiter(1, 1, 1000);
        rateLimiter.checkRateOf("1");
    }

    @Test
    public void rateLimiterSetTo_2CallsPerSecond_shouldAllow2ConsecutiveCallsWithSameKeys() throws Exception {

        RateLimiter rateLimiter = new RateLimiter(2, 1, 1000);

        rateLimiter.checkRateOf("2");
        rateLimiter.checkRateOf("2");
    }

    @Test
    public void rateLimiterSetTo_2CallsPerSecond_shouldAFailWhen3ConsecutiveCallsWithSameKeysAreMade() throws Exception {
        RateLimiter rateLimiter = new RateLimiter(2, 1, 1000);

        rateLimiter.checkRateOf("3");
        rateLimiter.checkRateOf("3");

        expectedException.expect(RateLimitException.class);
        rateLimiter.checkRateOf("3");
    }

    @Test
    public void rateLimiterSetTo_2CallsPer300Millis_shouldAllowMaking3CallsWithinTheAllowedTimeWithSameKey() throws Exception {

        RateLimiter rateLimiter = new RateLimiter(2, 1, 300);

        rateLimiter.checkRateOf("4");
        rateLimiter.checkRateOf("4");

        Thread.sleep(300);
        rateLimiter.checkRateOf("4");
    }

    @Test
    public void rateLimiterSetTo_2CallsPer500Millis_shouldAllowMakingACallPerSecondWithSameKey() throws Exception {
        RateLimiter rateLimiter = new RateLimiter(2, 1, 500);

        rateLimiter.checkRateOf("5");
        Thread.sleep(250);
        rateLimiter.checkRateOf("5");
        Thread.sleep(250);
        rateLimiter.checkRateOf("5");
        Thread.sleep(250);
        rateLimiter.checkRateOf("5");
        Thread.sleep(250);
        rateLimiter.checkRateOf("5");
        Thread.sleep(250);
        rateLimiter.checkRateOf("5");
    }

    @Test
    public void rateLimiterSetTo_2CallsPer300Millis_shouldAllowMakingOnly2CallsWithSameKey() throws Exception {

        final RateLimiter rateLimiter = new RateLimiter(2, 1, 300);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        List<Callable<String>> tasks = Arrays.asList(
                () -> {
                    rateLimiter.checkRateOf("1");
                    return "task1";
                },
                () -> {
                    rateLimiter.checkRateOf("1");
                    return "task2";
                },
                () -> {
                    rateLimiter.checkRateOf("1");
                    return "task3";
                },
                () -> {
                    rateLimiter.checkRateOf("1");
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
                .filter(task -> task != null)
                .collect(Collectors.toList());

        assertThat(successfulTasks.size(), is(2));
    }

    @Test
    public void rateLimiterSetTo_2CallsPerSecond_shouldNotAllowMakingAThirdCallsWithSameKey() throws Exception {

        RateLimiter rateLimiter = new RateLimiter(2, 1, 1000);

        rateLimiter.checkRateOf("6");
        Thread.sleep(910);
        rateLimiter.checkRateOf("6");

        try {
            rateLimiter.checkRateOf("6");
            fail("Expected RateLimitException to be thrown");
        } catch (RateLimitException e) {

        }

        Thread.sleep(900);
        rateLimiter.checkRateOf("6");
    }

    @Test
    public void rateLimiter_shouldAuditWithoutThrowingException() throws Exception {

        RateLimiter rateLimiter = new RateLimiter(2, 1, 1000);

        rateLimiter.auditRateOf("6");
        Thread.sleep(910);
        rateLimiter.auditRateOf("6");
    }
}
