package uk.gov.pay.api.ledger.service;

import uk.gov.pay.api.app.config.PublicApiConfig;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;

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
        UriBuilder builder = UriBuilder.fromPath(configuration.getLedgerUrl()).path(path);
        params.entrySet().stream()
                .filter(k -> k.getValue() != null)
                .forEach(k -> builder.queryParam(k.getKey(), k.getValue()));
        return builder.toString();
    }
}
