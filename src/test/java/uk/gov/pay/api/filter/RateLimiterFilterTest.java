package uk.gov.pay.api.filter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import java.io.PrintWriter;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RateLimiterFilterTest {

    private RateLimiterFilter rateLimiterFilter;

    @Mock
    private RateLimiter rateLimiter;
    
    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockFilterChain;

    @Before
    public void setup() {
        rateLimiterFilter = new RateLimiterFilter(rateLimiter, new ObjectMapper());
    }


    @Test
    public void shouldProcessFilterChain_whenRateLimiterDoesNotThrowARateLimiterException() throws Exception {

        String authorization = "Bearer whateverAuthorizationToken";
        when(mockRequest.getHeader("Authorization")).thenReturn(authorization);

        rateLimiterFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(rateLimiter).checkRateOf(authorization);
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void shouldRejectRequest_with429ResponseError__whenRateLimiterThrowsRateLimiterException() throws Exception {

        // given
        String authorization = "Bearer whateverAuthorizationToken";
        when(mockRequest.getHeader("Authorization")).thenReturn(authorization);

        doThrow(RateLimitException.class).when(rateLimiter).checkRateOf(authorization);

        PrintWriter mockPrinter = mock(PrintWriter.class);
        when(mockResponse.getWriter()).thenReturn(mockPrinter);

        // when
        rateLimiterFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        // then
        verifyZeroInteractions(mockFilterChain);
        verify(mockResponse).setStatus(429);
        verify(mockResponse).setContentType("application/json");
        verify(mockResponse).setCharacterEncoding("utf-8");
        verify(mockResponse).getWriter();
        verify(mockPrinter).print("{\"code\":\"P0900\",\"description\":\"Too many requests\"}");
        verifyNoMoreInteractions(mockResponse);
    }

    @Test
    public void shouldLogRequest_ForAuditRate() throws Exception {

        // given
        String authorization = "Bearer whateverAuthorizationToken";

        when(mockRequest.getHeader("Authorization")).thenReturn(authorization);
        when(mockRequest.getMethod()).thenReturn("POST");
        
        // when
        rateLimiterFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        // then
        verify(rateLimiter).auditRateOf("POST-" + authorization);
    }
}
