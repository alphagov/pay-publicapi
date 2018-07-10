package uk.gov.pay.api.service;

import com.google.common.collect.Maps;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;

public class ConnectorUriGenerator {
    private final PublicApiConfig configuration;

    @Inject
    public ConnectorUriGenerator(PublicApiConfig configuration) {
        this.configuration = configuration;
    }

    public String chargesURI(Account account, String agreementId) {
        String chargePath = "/v1/api/accounts/%s/charges";
        if (account.getPaymentType().equals(DIRECT_DEBIT) && agreementId != null) {
            chargePath += "/collect";
        }
        return buildConnectorUri(account, format(chargePath, account.getAccountId()));
    }

    public String chargesURIWithParams(Account account, Map<String, String> queryParams) {
        return buildConnectorUri(account, format("/v1/api/accounts/%s/charges", account.getAccountId()), queryParams);
    }
    
    public String chargeURI(Account account, String chargeId) {
        String path = format("/v1/api/accounts/%s/charges/%s", account.getAccountId(), chargeId);
        return buildConnectorUri(account, path);
    }

    public String chargeEventsURI(Account account, String paymentId) {
        String path = format("/v1/api/accounts/%s/charges/%s/events", account.getAccountId(), paymentId);
        return buildConnectorUri(account, path);
    }
    
    public String directDebitTransactionsURI(Account account, List<Pair<String, String>> queryParams) {
        String path = String.format("/v1/api/accounts/%s/transactions/view", account.getAccountId());
        return buildConnectorUri(account, path, queryParams);
    }

    private String buildConnectorUri(Account account, String path) {
        return buildConnectorUri(account, path, Maps.newHashMap());
    }
    
    private String buildConnectorUri(Account account, String path, Map<String, String> params) {
        UriBuilder builder = UriBuilder.fromPath(connectorBaseUrlForAccount(account)).path(path);
        params.entrySet().stream()
                .filter(k -> k.getValue() != null)
                .forEach(k -> builder.queryParam(k.getKey(), k.getValue()));
        return builder.toString();
    }

    private String connectorBaseUrlForAccount(Account account) {
        return isDirectDebitAccount(account) ? configuration.getConnectorDDUrl() : configuration.getConnectorUrl();
    }

    private boolean isDirectDebitAccount(Account account) {
        return account.getPaymentType().equals(DIRECT_DEBIT);
    }

    public String cancelURI(Account account, String paymentId) {
        String path = format("/v1/api/accounts/%s/charges/%s/cancel", account.getAccountId(), paymentId);
        return buildConnectorUri(account, path, Maps.newHashMap());
    }

    public String eventsURI(Account account, ZonedDateTime beforeDate, ZonedDateTime afterDate, Integer page, Integer pageSize, String agreementId, String paymentId) {

        Map<String, String> params = new LinkedHashMap<>();
        
        if (beforeDate != null)
            params.put("before", beforeDate.format(DateTimeFormatter.ISO_INSTANT));

        if (afterDate != null)
            params.put("after", afterDate.format(DateTimeFormatter.ISO_INSTANT));

        if (agreementId != null)
            params.put("mandate_external_id", agreementId);

        if (paymentId != null)
            params.put("transaction_external_id", paymentId);

        params.put("page", Optional.ofNullable(page).orElse(1).toString());
        params.put("page_size", Optional.ofNullable(pageSize).orElse(500).toString());
        
        return buildConnectorUri(account, "/v1/events", params);
    }
}
