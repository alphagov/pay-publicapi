package uk.gov.pay.api.filter;

import com.google.common.io.BaseEncoding;
import org.apache.commons.codec.digest.HmacUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

public class AuthorizationValidationFilter implements Filter {

    private static final int HMAC_SHA1_LENGTH = 32;
    private static final String BEARER_PREFIX = "Bearer ";

    private String apiKeyHmacSecret;

    public AuthorizationValidationFilter(String apiKeyHmacSecret) {
        this.apiKeyHmacSecret = apiKeyHmacSecret;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        final String authorization = ((HttpServletRequest) request).getHeader("Authorization");

        if (isValidAuthorizationHeader(authorization)) {
            chain.doFilter(request, response);

        } else {
            ((HttpServletResponse) response).sendError(UNAUTHORIZED.getStatusCode(), UNAUTHORIZED.getReasonPhrase());
        }
    }

    @Override
    public void destroy() {}

    private boolean isValidAuthorizationHeader(String authorization) {
        return authorization!= null
                && authorization.startsWith(BEARER_PREFIX)
                && isValidTokenIntegrity(authorization.substring(BEARER_PREFIX.length()));
    }

    private boolean isValidTokenIntegrity(String apiKey) {
        boolean isValid = false;
        if (apiKey.length() >= HMAC_SHA1_LENGTH + 1) {
            int initHmacIndex = apiKey.length() - HMAC_SHA1_LENGTH;
            String hmacFromApiKey = apiKey.substring(initHmacIndex);
            String tokenFromApiKey = apiKey.substring(0, initHmacIndex);
            isValid = tokenMatchesHmac(tokenFromApiKey, hmacFromApiKey);
        }
        return isValid;
    }

    private boolean tokenMatchesHmac(String token, String currentHmac) {
        final String hmacCalculatedFromToken = BaseEncoding.base32Hex()
                .lowerCase().omitPadding()
                .encode(HmacUtils.hmacSha1(apiKeyHmacSecret, token));
        return hmacCalculatedFromToken.equals(currentHmac);
    }
}
