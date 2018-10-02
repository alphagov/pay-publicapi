package uk.gov.pay.api.middleware;

import io.swagger.inflector.models.RequestContext;
import io.swagger.inflector.models.ResponseContext;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.auth.AccountAuthenticator;
import uk.gov.pay.api.model.ValidCreatePaymentRequest;
import uk.gov.pay.api.resources.PaymentsResource;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Optional;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

@Singleton
public class Payments {
    
    private final PaymentsResource paymentsResource;
    private final AccountAuthenticator accountAuthenticator;

    @Inject
    public Payments(PaymentsResource paymentsResource, AccountAuthenticator accountAuthenticator) {
        this.paymentsResource = paymentsResource;
        this.accountAuthenticator = accountAuthenticator;
    }

    public ResponseContext createNewPayment(RequestContext request, ValidCreatePaymentRequest validCreatePaymentRequest) {
        String token = request.getHeaders().getFirst(AUTHORIZATION);
        Optional<Account> account = accountAuthenticator.authenticate(getCredentials(token));
        return paymentsResource.createNewPayment(account.get(), validCreatePaymentRequest);
    }

    private String getCredentials(String header) {
        if (header == null) {
            return null;
        }

        final int space = header.indexOf(' ');
        if (space <= 0) {
            return null;
        }

        final String method = header.substring(0, space);
        if (!"Bearer".equalsIgnoreCase(method)) {
            return null;
        }

        return header.substring(space + 1);
    }
}
