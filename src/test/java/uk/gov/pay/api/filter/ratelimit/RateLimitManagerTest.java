package uk.gov.pay.api.filter.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.pay.api.app.config.RateLimiterConfig;
import uk.gov.pay.api.filter.RateLimiterKey;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RateLimitManagerTest {

    @Mock
    private RateLimiterConfig rateLimiterConfig;

    private RateLimitManager rateLimitManager;
    
    @BeforeEach
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
