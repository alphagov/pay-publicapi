package uk.gov.pay.api.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RateLimiterFilterTest {

    private RateLimiterFilter rateLimiterFilter;

    private RateLimiter rateLimiter;
    
    @Mock
    private HttpServletRequest mockPostRequest;
    @Mock
    private HttpServletRequest mockGetRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockFilterChain;
    
    String authorization = "Bearer whateverAuthorizationToken";

    @Before
    public void setup() {
        rateLimiter = new RateLimiter(1, 1, 1, 100);
        rateLimiterFilter = new RateLimiterFilter(rateLimiter, new ObjectMapper());

        when(mockPostRequest.getHeader("Authorization")).thenReturn(authorization);
        when(mockPostRequest.getMethod()).thenReturn("POST");

        when(mockGetRequest.getHeader("Authorization")).thenReturn(authorization);
        when(mockGetRequest.getMethod()).thenReturn("GET");
    }

    @Test
    public void shouldProcessFilterChain_whenRequestsAreWithinTheRate() throws Exception {
        rateLimiterFilter.doFilter(mockPostRequest, mockResponse, mockFilterChain);
        rateLimiterFilter.doFilter(mockGetRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockPostRequest, mockResponse);
        verify(mockFilterChain, times(1)).doFilter(mockGetRequest, mockResponse);
    }

    @Test
    public void testCacheExpiry() throws Exception {
        rateLimiterFilter.doFilter(mockPostRequest, mockResponse, mockFilterChain);
        Thread.sleep(100);
        rateLimiterFilter.doFilter(mockPostRequest, mockResponse, mockFilterChain);
        
        verify(mockFilterChain, times(2)).doFilter(mockPostRequest, mockResponse);
    }
    
    @Test
    public void shouldRejectGetRequest_with429ResponseError_whenRequestsExceedRateLimit() throws Exception {
        PrintWriter mockPrinter = mock(PrintWriter.class);
        when(mockResponse.getWriter()).thenReturn(mockPrinter);

        rateLimiterFilter.doFilter(mockGetRequest, mockResponse, mockFilterChain);
        rateLimiterFilter.doFilter(mockGetRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockGetRequest, mockResponse);
        verify(mockResponse).setStatus(429);
        verify(mockResponse).setContentType("application/json");
        verify(mockResponse).setCharacterEncoding("utf-8");
        verify(mockResponse).getWriter();
        verify(mockPrinter).print("{\"code\":\"P0900\",\"description\":\"Too many requests\"}");
        verifyNoMoreInteractions(mockResponse);
    }
    
    @Test
    public void shouldRejectPostRequest_with429ResponseError_whenRequestsExceedRateLimit() throws Exception {
        PrintWriter mockPrinter = mock(PrintWriter.class);
        when(mockResponse.getWriter()).thenReturn(mockPrinter);

        rateLimiterFilter.doFilter(mockPostRequest, mockResponse, mockFilterChain);
        rateLimiterFilter.doFilter(mockPostRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain, times(1)).doFilter(mockPostRequest, mockResponse);
        verify(mockResponse).setStatus(429);
        verify(mockResponse).setContentType("application/json");
        verify(mockResponse).setCharacterEncoding("utf-8");
        verify(mockResponse).getWriter();
        verify(mockPrinter).print("{\"code\":\"P0900\",\"description\":\"Too many requests\"}");
        verifyNoMoreInteractions(mockResponse);
    }

    @Test
    public void shouldLogRequest_ForAuditRate() throws Exception {

        RateLimiter rateLimiter = mock(RateLimiter.class);
        rateLimiterFilter = new RateLimiterFilter(rateLimiter, new ObjectMapper());
        
        rateLimiterFilter.doFilter(mockPostRequest, mockResponse, mockFilterChain);

        verify(rateLimiter).auditRateOf("POST-" + authorization);
    }
}
