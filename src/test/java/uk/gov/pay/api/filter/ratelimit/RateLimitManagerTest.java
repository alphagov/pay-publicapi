package uk.gov.pay.api.filter.ratelimit;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.pay.api.app.config.RateLimiterConfig;
import uk.gov.pay.api.filter.RateLimiterKey;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;


@RunWith(JUnitParamsRunner.class)
public class RateLimitManagerTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    
    @Mock
    private RateLimiterConfig rateLimiterConfig;

    
    private RateLimitManager rateLimitManager;
    
    @Before
    public void setUp() {
        when(rateLimiterConfig.getElevatedAccounts()).thenReturn(List.of("1"));
    }

    @Test
    public void returnsNumberOfAllowedRequestsForNoAccount() {
        var rateLimiterKey = new RateLimiterKey("path", "key-type", "PUT");
        when(rateLimiterConfig.getNoOfReq()).thenReturn(1);

        rateLimitManager = new RateLimitManager(rateLimiterConfig);
        assertThat(rateLimitManager.getAllowedNumberOfRequests(rateLimiterKey, ""), is(1));
    }

    @Test
    public void returnsNumberOfAllowedRequestsForPostForAccount1() {
        var rateLimiterKey = new RateLimiterKey("path", "key-type", "POST");
        when(rateLimiterConfig.getNoOfPostReqForElevatedAccounts()).thenReturn(4);

        rateLimitManager = new RateLimitManager(rateLimiterConfig);
        assertThat(rateLimitManager.getAllowedNumberOfRequests(rateLimiterKey, "1"), is(4));
    }

    @Test
    public void returnsNumberOfAllowedRequestsForGetForAccount1() {
        var rateLimiterKey = new RateLimiterKey("path", "key-type", "GET");
        when(rateLimiterConfig.getNoOfReqForElevatedAccounts()).thenReturn(3);

        rateLimitManager = new RateLimitManager(rateLimiterConfig);
        assertThat(rateLimitManager.getAllowedNumberOfRequests(rateLimiterKey, "1"), is(3));
    }

    @Test
    public void returnsNumberOfAllowedRequestsForPostForAccount2() {
        var rateLimiterKey = new RateLimiterKey("path", "key-type", "POST");
        when(rateLimiterConfig.getNoOfReqForPost()).thenReturn(2);

        rateLimitManager = new RateLimitManager(rateLimiterConfig);
        assertThat(rateLimitManager.getAllowedNumberOfRequests(rateLimiterKey, "2"), is(2));
    }

    @Test
    public void returnsNumberOfAllowedRequestsForGetForAccount2() {
        var rateLimiterKey = new RateLimiterKey("path", "key-type", "GET");
        when(rateLimiterConfig.getNoOfReq()).thenReturn(1);

        rateLimitManager = new RateLimitManager(rateLimiterConfig);
        assertThat(rateLimitManager.getAllowedNumberOfRequests(rateLimiterKey, "2"), is(1));
    }
}
