package uk.gov.pay.api.service.directdebit;

import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;

public class DirectDebitConnectorUriGenerator {
    private final PublicApiConfig configuration;

    @Inject
    public DirectDebitConnectorUriGenerator(PublicApiConfig configuration) {
        this.configuration = configuration;
    }

    String chargesURI(Account account) {
        String chargePath = "/v1/api/accounts/%s/charges/collect";
        return buildConnectorUri(account, format(chargePath, account.getAccountId()));
    }

    public String singleMandateURI(Account account, String externalMandateId) {
        return mandatesURI(account) + "/" + externalMandateId;
    }
    
    public String mandatesURI(Account account) {
        return buildConnectorUri(account, format("/v1/api/accounts/%s/mandates", account.getAccountId()));
    }

    public String directDebitPaymentsURI(Account account, Map<String, String> queryParams) {
        String path = String.format("/v1/api/accounts/%s/payments", account.getAccountId());
        return buildConnectorUri(account, path, queryParams);
    }

    private String buildConnectorUri(Account account, String path) {
        return buildConnectorUri(account, path, Collections.emptyMap());
    }

    private String buildConnectorUri(Account account, String path, Map<String, String> params) {
        UriBuilder builder = UriBuilder.fromPath(configuration.getConnectorDDUrl()).path(path);
        params.entrySet().stream()
                .filter(k -> k.getValue() != null)
                .forEach(k -> builder.queryParam(k.getKey(), k.getValue()));
        return builder.toString();
    }

}
