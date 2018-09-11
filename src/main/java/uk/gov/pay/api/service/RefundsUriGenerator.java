package uk.gov.pay.api.service;

import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;

import static java.lang.String.format;

public class RefundsUriGenerator {
    private final PublicApiConfig configuration;

    @Inject
    public RefundsUriGenerator(PublicApiConfig configuration) {
        this.configuration = configuration;
    }

    public String refundsURIWithParams(Account account, Map<String, String> queryParams) {
        return buildConnectorUri(format("/v1/refunds/account/%s", account.getAccountId()), queryParams);
    }

    private String buildConnectorUri(String path, Map<String, String> params) {
        UriBuilder builder = UriBuilder.fromPath(configuration.getConnectorUrl()).path(path);
        params.entrySet().stream()
                .filter(k -> k.getValue() != null)
                .forEach(k -> builder.queryParam(k.getKey(), k.getValue()));
        return builder.toString();
    }
}
