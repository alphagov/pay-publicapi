package uk.gov.pay.api.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.pay.api.model.PaymentError.Code;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

/**
 * Allow only a certain number of requests from the same source (given by the Authorization Header)
 * within the given time configured in the RateLimiter. See {@link RateLimiter}
 * <p>
 * 429 Too Many Requests will be returned when rate limit is reached.
 */
public class RateLimiterFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterFilter.class);
    private static final int TOO_MANY_REQUESTS_STATUS_CODE = 429;
    private static final String UTF8_CHARACTER_ENCODING = "utf-8";

    private final RateLimiter rateLimiter;
    private ObjectMapper objectMapper;

    /**
     * @param rateLimiter Limiter in number of requests per given time coming from the same source (Authorization)
     */
    @Inject
    public RateLimiterFilter(RateLimiter rateLimiter, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.objectMapper = objectMapper;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        final String authorization = ((HttpServletRequest) request).getHeader("Authorization");
        final String method = ((HttpServletRequest) request).getMethod();

        rateLimiter.auditRateOf(method + "-" + authorization);

        try {
            rateLimiter.checkRateOf(method + "-" + authorization);
            chain.doFilter(request, response);
        } catch (RateLimitException e) {
            LOGGER.info("Rate limit reached for current service. Sending response '429 Too Many Requests'");
            setTooManyRequestsError((HttpServletResponse) response);
        } 
    }

    private void setTooManyRequestsError(HttpServletResponse response) throws IOException {
        response.setStatus(TOO_MANY_REQUESTS_STATUS_CODE);
        response.setContentType(APPLICATION_JSON);
        response.setCharacterEncoding(UTF8_CHARACTER_ENCODING);
        response.getWriter().print(objectMapper.writeValueAsString(aPaymentError(Code.TOO_MANY_REQUESTS_ERROR)));
    }

    @Override
    public void destroy() {}
}
