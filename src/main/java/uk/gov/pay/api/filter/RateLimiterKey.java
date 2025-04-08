package uk.gov.pay.api.filter;

import uk.gov.pay.api.utils.PathHelper;

import jakarta.ws.rs.container.ContainerRequestContext;

public class RateLimiterKey {

    private final String method;
    private final String key;
    private final String keyType;

    private RateLimiterKey(String key, String keyType, String method) {
        this.key = key;
        this.keyType = keyType;
        this.method = method;
    }

    public static RateLimiterKey from(ContainerRequestContext requestContext, String accountId) {
        final String method = requestContext.getMethod();

        StringBuilder builder = new StringBuilder(method);

        final String pathType = PathHelper.getPathType(requestContext.getUriInfo().getPath(), method);
        if (!pathType.isBlank()) {
            builder.append("-").append(pathType);
        }

        final String keyType = builder.toString();
        builder.append("-").append(accountId);

        return new RateLimiterKey(builder.toString(), keyType, method);
    }

    public String getKey() {
        return key;
    }

    public String getKeyType() {
        return keyType;
    }

    public String getMethod() {
        return method;
    }
}
