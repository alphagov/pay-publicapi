package uk.gov.pay.api.filter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;
import java.security.Principal;
import java.util.Optional;

import static uk.gov.pay.logging.LoggingKeys.GATEWAY_ACCOUNT_ID;

@Provider
@Priority(Priorities.USER)
public class GatewayAccountIdFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        MDC.put(GATEWAY_ACCOUNT_ID, getAccountId(requestContext));
    }

    private String getAccountId(ContainerRequestContext requestContext) {
        return Optional.ofNullable(requestContext.getSecurityContext().getUserPrincipal())
                .map(Principal::getName)
                .orElse(StringUtils.EMPTY);
    }
}
