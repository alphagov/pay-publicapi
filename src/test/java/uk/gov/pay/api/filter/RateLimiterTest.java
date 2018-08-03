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

public class RateLimiterTest {

    private static final String METHOD_POST = "POST";
    private static final String METHOD_GET = "GET";
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void rateLimiterSetTo_1CallPerSecond_shouldAllowSingleCall() throws Exception {
        RateLimiter rateLimiter = new RateLimiter(1, 1,1, 1000);
        rateLimiter.checkRateOf("1",METHOD_GET);
    }

    @Test
    public void rateLimiterSetTo_2CallsPerSecond_shouldAllow2ConsecutiveCallsWithSameKeys() throws Exception {

        RateLimiter rateLimiter = new RateLimiter(2, 1,1, 1000);

        rateLimiter.checkRateOf("2",METHOD_GET);
        rateLimiter.checkRateOf("2",METHOD_GET);
    }

    @Test
    public void rateLimiterSetTo_2CallsPerSecond_shouldAFailWhen3ConsecutiveCallsWithSameKeysAreMade() throws Exception {
        RateLimiter rateLimiter = new RateLimiter(2, 1,1, 1000);

        rateLimiter.checkRateOf("3",METHOD_GET);
        rateLimiter.checkRateOf("3",METHOD_GET);

        expectedException.expect(RateLimitException.class);
        rateLimiter.checkRateOf("3",METHOD_GET);
    }

    @Test
    public void rateLimiterSetTo_2CallsPer300Millis_shouldAllowMaking3CallsWithinTheAllowedTimeWithSameKey() throws Exception {

        RateLimiter rateLimiter = new RateLimiter(2, 1,1, 300);

        rateLimiter.checkRateOf("4",METHOD_GET);
        rateLimiter.checkRateOf("4",METHOD_GET);

        Thread.sleep(300);
        rateLimiter.checkRateOf("4",METHOD_GET);
    }

    @Test
    public void rateLimiterSetTo_2CallsPer500Millis_shouldAllowMakingACallPerSecondWithSameKey() throws Exception {
        RateLimiter rateLimiter = new RateLimiter(2, 1,1, 500);

        rateLimiter.checkRateOf("5",METHOD_GET);
        Thread.sleep(250);
        rateLimiter.checkRateOf("5",METHOD_GET);
        Thread.sleep(250);
        rateLimiter.checkRateOf("5",METHOD_GET);
        Thread.sleep(250);
        rateLimiter.checkRateOf("5", METHOD_GET);
        Thread.sleep(250);
        rateLimiter.checkRateOf("5", METHOD_GET);
        Thread.sleep(250);
        rateLimiter.checkRateOf("5", METHOD_GET);
    }

    @Test
    public void rateLimiterSetTo_2CallsPer300Millis_shouldAllowMakingOnly2CallsWithSameKey() throws Exception {

        final RateLimiter rateLimiter = new RateLimiter(2, 1,1, 300);

        ExecutorService executor = Executors.newFixedThreadPool(3);

        List<Callable<String>> tasks = Arrays.asList(
                () -> {
                    rateLimiter.checkRateOf("1", METHOD_GET);
                    return "task1";
                },
                () -> {
                    rateLimiter.checkRateOf("1", METHOD_GET);
                    return "task2";
                },
                () -> {
                    rateLimiter.checkRateOf("1", METHOD_GET);
                    return "task3";
                },
                () -> {
                    rateLimiter.checkRateOf("1", METHOD_GET);
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

        RateLimiter rateLimiter = new RateLimiter(2, 1,1, 1000);

        rateLimiter.checkRateOf("6", METHOD_GET);
        Thread.sleep(910);
        rateLimiter.checkRateOf("6", METHOD_GET);
        expectedException.expect(RateLimitException.class);
        rateLimiter.checkRateOf("6", METHOD_GET);
        
        Thread.sleep(900);
        rateLimiter.checkRateOf("6", METHOD_GET);
    }

    @Test
    public void rateLimiter_shouldAuditWithoutThrowingException() throws Exception {

        RateLimiter rateLimiter = new RateLimiter(2, 1,1, 1000);

        rateLimiter.auditRateOf("6");
        Thread.sleep(910);
        rateLimiter.auditRateOf("6");
    }
}
