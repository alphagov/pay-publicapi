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
        when(rateLimiterConfig.getNoOfReq()).thenReturn(1);
        when(rateLimiterConfig.getNoOfReqForPost()).thenReturn(2);
        when(rateLimiterConfig.getNoOfReqForElevatedAccounts()).thenReturn(3);
        when(rateLimiterConfig.getNoOfPostReqForElevatedAccounts()).thenReturn(4);
        
        rateLimitManager = new RateLimitManager(rateLimiterConfig);
    }

    @Test
    @Parameters({
            "1,POST,4",
            "1,GET,3",
            "2,POST,2",
            "2,GET,1",
            ",PUT,1"
    })
    public void returnsNumberOfAllowedRequests(String account, String method, int expectedNumberOfAllowedRequests) {
        var rateLimiterKey = new RateLimiterKey("path", "key-type", method);
        
        assertThat(rateLimitManager.getAllowedNumberOfRequests(rateLimiterKey, account), is(expectedNumberOfAllowedRequests));
    }
}
