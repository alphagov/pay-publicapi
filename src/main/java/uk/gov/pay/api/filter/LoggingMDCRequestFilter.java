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
import static uk.gov.pay.logging.LoggingKeys.PAYMENT_EXTERNAL_ID;
import static uk.gov.pay.logging.LoggingKeys.MANDATE_EXTERNAL_ID;
import static uk.gov.pay.logging.LoggingKeys.REFUND_EXTERNAL_ID;

@Provider
@Priority(Priorities.USER)
public class LoggingMDCRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        MDC.put(GATEWAY_ACCOUNT_ID, getAccountId(requestContext));
        getPathParameterFromRequest("paymentId", requestContext)
                .ifPresent(paymentId -> MDC.put(PAYMENT_EXTERNAL_ID, paymentId));
        getPathParameterFromRequest("mandateId", requestContext)
                .ifPresent(mandateId -> MDC.put(MANDATE_EXTERNAL_ID, mandateId));
        getPathParameterFromRequest("refundId", requestContext)
                .ifPresent(refundId -> MDC.put(REFUND_EXTERNAL_ID, refundId));
    }

    private Optional<String> getPathParameterFromRequest(String parameterName, ContainerRequestContext requestContext) {
        return Optional.ofNullable(requestContext.getUriInfo().getPathParameters().getFirst(parameterName));
    }

    private String getAccountId(ContainerRequestContext requestContext) {
        return Optional.ofNullable(requestContext.getSecurityContext().getUserPrincipal())
                .map(Principal::getName)
                .orElse(StringUtils.EMPTY);
    }
}
