package uk.gov.pay.api.filter;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.pay.api.app.config.PublicApiConfig;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.utils.ApiKeyGenerator.apiKeyValueOf;

@ExtendWith(MockitoExtension.class)
public class AuthorizationValidationFilterTest {

    private static final String SECRET_KEY = "mysupersecret";
    private AuthorizationValidationFilter authorizationValidationFilter;

    @Mock
    private PublicApiConfig mockConfiguration;
    @Mock
    private HttpServletRequest mockRequest;
    @Mock
    private HttpServletResponse mockResponse;
    @Mock
    private FilterChain mockFilterChain;

    @BeforeEach
    public void setup() {
        when(mockConfiguration.getApiKeyHmacSecret()).thenReturn(SECRET_KEY);
        
        authorizationValidationFilter = new AuthorizationValidationFilter(mockConfiguration);
    }

    @Test
    public void shouldProcessFilterChain_whenAuthorizationHeaderIsValid() throws Exception {

        String validToken = "asdfghdasd";
        String authorization = "Bearer " + apiKeyValueOf(validToken, SECRET_KEY);

        when(mockRequest.getHeader("Authorization")).thenReturn(authorization);

        authorizationValidationFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void shouldRejectRequest_with401ResponseError_whenAuthorizationHeaderIsInvalid() throws Exception {

        String invalidApiKey = "asdfghdasdakjshdkjwhdjweghrhjgwerguweurweruhiweuiweriuui";
        String authorization = "Bearer " + invalidApiKey;

        when(mockRequest.getHeader("Authorization")).thenReturn(authorization);

        authorizationValidationFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verifyNoInteractions(mockFilterChain);
        verify(mockResponse).sendError(401, "Unauthorized");
    }

    @Test
    public void shouldRejectRequest_with401ResponseError_whenAuthorizationHeaderIsNotPresent() throws Exception {

        when(mockRequest.getHeader("Authorization")).thenReturn(null);

        authorizationValidationFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verifyNoInteractions(mockFilterChain);
        verify(mockResponse).sendError(401, "Unauthorized");
    }

    @Test
    public void shouldRejectRequest_with401ResponseError_whenAuthorizationHeaderHasInvalidFormat() throws Exception {

        String validToken = "asdfghdasd";
        String authorization = "Bearer" + apiKeyValueOf(validToken, SECRET_KEY);

        when(mockRequest.getHeader("Authorization")).thenReturn(authorization);

        authorizationValidationFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verifyNoInteractions(mockFilterChain);
        verify(mockResponse).sendError(401, "Unauthorized");
    }

    @Test
    public void shouldRejectRequest_with401ResponseError_whenAuthorizationHeaderHasNotMinimumLengthExpected() throws Exception {

        String apiKey = RandomStringUtils.randomAlphanumeric(32);
        String authorization = "Bearer " + apiKey;

        when(mockRequest.getHeader("Authorization")).thenReturn(authorization);

        authorizationValidationFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verifyNoInteractions(mockFilterChain);
        verify(mockResponse).sendError(401, "Unauthorized");
    }
}
