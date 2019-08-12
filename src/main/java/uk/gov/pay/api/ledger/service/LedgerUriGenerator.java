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

    public String transactionsURIWithParams(Map<String, String> queryParams) {
        return buildLedgerUri("/v1/transaction", queryParams);
    }

    private String buildLedgerUri(String path, Map<String, String> params) {
        var ledgerUrl = configuration.getLedgerUrl();
        UriBuilder builder = UriBuilder.fromPath(ledgerUrl).path(path);
        params.entrySet().stream()
                .filter(k -> k.getValue() != null)
                .forEach(k -> builder.queryParam(k.getKey(), k.getValue()));
        return builder.toString();
    }

    public String transactionURI(Account gatewayAccountId, String paymentId) {
        String path = format("/v1/transaction/%s", paymentId);
        return buildLedgerUri(path, Map.of("account_id", gatewayAccountId.getAccountId()));
    }

    public String transactionEventsURI(Account gatewayAccountId, String paymentId) {
        String path = format("/v1/transaction/%s/event", paymentId);
        return buildLedgerUri(path, Map.of("gateway_account_id", gatewayAccountId.getAccountId()));
    }
    
    public String transactionsForTransactionURI(String gatewayAccountId, String paymentId) {
        String path = format("/v1/transaction/%s/transaction", paymentId);
        return buildLedgerUri(path, Map.of("gateway_account_id", gatewayAccountId));
    }
}
