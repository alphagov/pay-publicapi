package uk.gov.pay.api.filter;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.UriInfo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class RateLimiterKeyTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    
    @Mock
    private ContainerRequestContext containerRequestContext;
    
    @Mock
    private UriInfo uriInfo;

    @Before
    public void setUp() {
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);
        when(containerRequestContext.getHeaderString("Authorization")).thenReturn("headerValue");
    }
    
    @Test
    @Parameters({
            "/v1/payments,POST,POST-create_payment,POST-create_payment-headerValue",
            "/v1/payments/paymentId/capture,POST,POST-capture_payment,POST-capture_payment-headerValue",
            "/v1/payments/paymentId/cancel,POST,POST,POST-headerValue",
            "/v1/payments,GET,GET,GET-headerValue"
    })
    public void returnsRateLimiterKey(String path, String method, String expectedKeyType, String expectedKey) {
        when(uriInfo.getPath()).thenReturn(path);
        when(containerRequestContext.getMethod()).thenReturn(method);

        var rateLimiterKey = RateLimiterKey.from(containerRequestContext);
        assertThat(rateLimiterKey.getKey(), is(expectedKey));
        assertThat(rateLimiterKey.getKeyType(), is(expectedKeyType));
    }
}
