package uk.gov.pay.api.ledger.service;

import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;

import static java.lang.String.format;

public class LedgerUriGenerator {
    private final PublicApiConfig configuration;

    @Inject
    public LedgerUriGenerator(PublicApiConfig configuration) {
        this.configuration = configuration;
    }

    public String transactionsURIWithParams(Account account, Map<String, String> queryParams) {
        return buildLedgerUri(format("/v1/api/accounts/%s/transactions", account.getAccountId()), queryParams);
    }

    private String buildLedgerUri(String path, Map<String, String> params) {
        UriBuilder builder = UriBuilder.fromPath(configuration.getLedgerUrl()).path(path);
        params.entrySet().stream()
                .filter(k -> k.getValue() != null)
                .forEach(k -> builder.queryParam(k.getKey(), k.getValue()));
        return builder.toString();
    }
}
