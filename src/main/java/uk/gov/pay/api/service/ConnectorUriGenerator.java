package uk.gov.pay.api.service;

import com.google.common.collect.Maps;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.CreateCardPaymentRequest;
import uk.gov.pay.api.model.CreatePaymentRequest;
import uk.gov.pay.api.model.TokenPaymentType;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;
import static uk.gov.pay.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class ConnectorUriGenerator {
    private final PublicApiConfig configuration;

    @Inject
    public ConnectorUriGenerator(PublicApiConfig configuration) {
        this.configuration = configuration;
    }

    String chargesURI(Account account, TokenPaymentType tokenPaymentType) {
        String chargePath = "/v1/api/accounts/%s/charges";
        if (account.getPaymentType().equals(DIRECT_DEBIT) && DIRECT_DEBIT.equals(tokenPaymentType)) {
            chargePath += "/collect";
        }
        return buildConnectorUri(account, format(chargePath, account.getAccountId()));
    }

    public String chargesURIWithParams(Account account, Map<String, String> queryParams) {
        return buildConnectorUri(account, format("/v1/api/accounts/%s/charges", account.getAccountId()), queryParams);
    }

    public String refundsURIWithParams(Account account, Map<String, String> queryParams) {
        String path = format("/v1/api/accounts/%s/refunds", account.getAccountId());
        return buildConnectorUri(account, path, queryParams);
    }

    String chargeURI(Account account, String chargeId) {
        String path = format("/v1/api/accounts/%s/charges/%s", account.getAccountId(), chargeId);
        return buildConnectorUri(account, path);
    }

    public String chargeEventsURI(Account account, String paymentId) {
        String path = format("/v1/api/accounts/%s/charges/%s/events", account.getAccountId(), paymentId);
        return buildConnectorUri(account, path);
    }

    public String directDebitTransactionsURI(Account account, Map<String, String> queryParams) {
        String path = String.format("/v1/api/accounts/%s/transactions/view", account.getAccountId());
        return buildConnectorUri(account, path, queryParams);
    }

    private String buildConnectorUri(Account account, String path) {
        return buildConnectorUri(account, path, Collections.emptyMap());
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

    String cancelURI(Account account, String paymentId) {
        String path = format("/v1/api/accounts/%s/charges/%s/cancel", account.getAccountId(), paymentId);
        return buildConnectorUri(account, path, Maps.newHashMap());
    }

    public String eventsURI(Account account, Optional<ZonedDateTime> toDate, Optional<ZonedDateTime> fromDate, Integer page, Integer displaySize, String agreementId, String paymentId) {

        Map<String, String> params = new LinkedHashMap<>();

        toDate.map(ISO_INSTANT_MILLISECOND_PRECISION::format).ifPresent(formattedDate -> params.put("to_date", formattedDate));

        fromDate.map(ISO_INSTANT_MILLISECOND_PRECISION::format).ifPresent(formattedDate -> params.put("from_date", formattedDate));

        if (agreementId != null)
            params.put("mandate_external_id", agreementId);

        if (paymentId != null)
            params.put("transaction_external_id", paymentId);

        params.put("page", Optional.ofNullable(page).orElse(1).toString());
        params.put("display_size", Optional.ofNullable(displaySize).orElse(500).toString());

        return buildConnectorUri(account, "/v1/events", params);
    }

    String captureURI(Account account, String chargeId) {
        String path = format("/v1/api/accounts/%s/charges/%s/capture", account.getAccountId(), chargeId);
        return buildConnectorUri(account, path);
    }
}
