package uk.gov.pay.api.service;

import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import java.net.URI;

import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;

public class ConnectorUriGenerator {
    private final PublicApiConfig configuration;

    @Inject
    public ConnectorUriGenerator(PublicApiConfig configuration) {
        this.configuration = configuration;
    }

    public String chargesURI(Account account) {
        return UriBuilder
                .fromPath(connectorBaseUrlForAccount(account))
                .path(String.format("/v1/api/accounts/%s/charges", account.getAccountId()))
                .toString();
    }

    private String connectorBaseUrlForAccount(Account account) {
        return isDirectDebitAccount(account) ? configuration.getConnectorDDUrl() : configuration.getConnectorUrl();
    }

    private boolean isDirectDebitAccount(Account account) {
        return account.getPaymentType().equals(DIRECT_DEBIT);
    }

    public URI getPaymentEventsURI(String baseUrl, String charge_id) {
        return null;
    }
}
