package uk.gov.pay.api.service;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;

public class ConnectorUriGenerator {
    private final PublicApiConfig configuration;

    @Inject
    public ConnectorUriGenerator(PublicApiConfig configuration) {
        this.configuration = configuration;
    }

    public String chargesURI(Account account) {
        return buildConnectorUri(account, String.format("/v1/api/accounts/%s/charges", account.getAccountId()));
    }

    public String chargesURI(Account account, List<Pair<String, String>> queryParams) {
        return buildConnectorUri(account, String.format("/v1/api/accounts/%s/charges", account.getAccountId()), queryParams);
    }
    
    public String chargeURI(Account account, String chargeId) {
        String path = String.format("/v1/api/accounts/%s/charges/%s", account.getAccountId(), chargeId);
        return buildConnectorUri(account, path);
    }

    public String chargeEventsURI(Account account, String paymentId) {
        String path = String.format("/v1/api/accounts/%s/charges/%s/events", account.getAccountId(), paymentId);
        return buildConnectorUri(account, path);
    }

    private String buildConnectorUri(Account account, String path) {
        return buildConnectorUri(account, path, Collections.emptyList());
    }
    
    private String buildConnectorUri(Account account, String path, List<Pair<String, String>> queryParams) {
        UriBuilder builder = UriBuilder.fromPath(connectorBaseUrlForAccount(account)).path(path);

        queryParams.forEach(pair -> {
            if (isNotBlank(pair.getRight())) {
                builder.queryParam(pair.getKey(), pair.getValue());
            }
        });

        return builder.toString();
    }

    private String connectorBaseUrlForAccount(Account account) {
        return isDirectDebitAccount(account) ? configuration.getConnectorDDUrl() : configuration.getConnectorUrl();
    }

    private boolean isDirectDebitAccount(Account account) {
        return account.getPaymentType().equals(DIRECT_DEBIT);
    }

    public String cancelURI(Account account, String paymentId) {
        String path = String.format("/v1/api/accounts/%s/charges/%s/cancel", account.getAccountId(), paymentId);
        return buildConnectorUri(account, path, Collections.emptyList()).toString();
    }
}
