package uk.gov.pay.api.filter;

import com.google.common.io.BaseEncoding;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;

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

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static net.logstash.logback.argument.StructuredArguments.kv;

public class AuthorizationValidationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationValidationFilter.class);
    
    private static final int HMAC_SHA1_LENGTH = 32;
    private static final String BEARER_PREFIX = "Bearer ";

    private String apiKeyHmacSecret;

    @Inject
    public AuthorizationValidationFilter(PublicApiConfig configuration) {
        this.apiKeyHmacSecret = configuration.getApiKeyHmacSecret();
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        final String authorization = ((HttpServletRequest) request).getHeader("Authorization");

        if (isValidAuthorizationHeader(authorization, request)) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).sendError(UNAUTHORIZED.getStatusCode(), UNAUTHORIZED.getReasonPhrase());
        }
    }

    @Override
    public void destroy() {
    }

    private boolean isValidAuthorizationHeader(String authorization, ServletRequest request) {
        return authorization != null
                && authorization.startsWith(BEARER_PREFIX)
                && isValidTokenIntegrity(authorization.substring(BEARER_PREFIX.length()), request);
    }

    private boolean isValidTokenIntegrity(String apiKey, ServletRequest request) {
        boolean isValid = false;
        if (apiKey.length() >= HMAC_SHA1_LENGTH + 1) {
            int initHmacIndex = apiKey.length() - HMAC_SHA1_LENGTH;
            String hmacFromApiKey = apiKey.substring(initHmacIndex);
            String tokenFromApiKey = apiKey.substring(0, initHmacIndex);
            isValid = tokenMatchesHmac(tokenFromApiKey, hmacFromApiKey);
        }
        
        if (!isValid) {
            logger.warn("Attempt to authenticate using an API key with an invalid checksum",
                    kv("remote_address", request.getRemoteAddr()));
        }

        return isValid;
    }

    private boolean tokenMatchesHmac(String token, String currentHmac) {
        final String hmacCalculatedFromToken = BaseEncoding.base32Hex()
                .lowerCase().omitPadding()
                .encode(new HmacUtils(HmacAlgorithms.HMAC_SHA_1, apiKeyHmacSecret).hmac(token));
        return hmacCalculatedFromToken.equals(currentHmac);
    }
}
