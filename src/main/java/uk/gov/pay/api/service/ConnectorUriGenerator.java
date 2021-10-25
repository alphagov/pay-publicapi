package uk.gov.pay.api.service;

import com.google.common.collect.Maps;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.service.payments.commons.model.ApiResponseDateTimeFormatter.ISO_INSTANT_MILLISECOND_PRECISION;

public class ConnectorUriGenerator {
    private final PublicApiConfig configuration;

    @Inject
    public ConnectorUriGenerator(PublicApiConfig configuration) {
        this.configuration = configuration;
    }

    String chargesURI(Account account) {
        return buildConnectorUri(format("/v1/api/accounts/%s/charges", account.getAccountId()));
    }

    String chargeURI(Account account, String chargeId) {
        String path = format("/v1/api/accounts/%s/charges/%s", account.getAccountId(), chargeId); //TODO rename to /payments instead /charges
        return buildConnectorUri(path);
    }

    public String chargeEventsURI(Account account, String paymentId) {
        String path = format("/v1/api/accounts/%s/charges/%s/events", account.getAccountId(), paymentId);
        return buildConnectorUri(path);
    }

    String cancelURI(Account account, String paymentId) {
        String path = format("/v1/api/accounts/%s/charges/%s/cancel", account.getAccountId(), paymentId);
        return buildConnectorUri(path, Maps.newHashMap());
    }

    public String telephoneChargesURI(Account account) {
        return buildConnectorUri(format("/v1/api/accounts/%s/telephone-charges", account.getAccountId()));
    }

    String captureURI(Account account, String chargeId) {
        String path = format("/v1/api/accounts/%s/charges/%s/capture", account.getAccountId(), chargeId);
        return buildConnectorUri(path);
    }

    String refundsForPaymentURI(String accountId, String chargeId) {
        String path = format("/v1/api/accounts/%s/charges/%s/refunds", accountId, chargeId);
        return buildConnectorUri(path);
    }

    String refundForPaymentURI(String accountId, String chargeId, String refundId) {
        String path = format("/v1/api/accounts/%s/charges/%s/refunds/%s", accountId, chargeId, refundId);
        return buildConnectorUri(path);
    }
    
    private String buildConnectorUri(String path) {
        return buildConnectorUri(path, Collections.emptyMap());
    }

    private String buildConnectorUri(String path, Map<String, String> params) {
        UriBuilder builder = UriBuilder.fromPath(configuration.getConnectorUrl()).path(path);
        params.entrySet().stream()
                .filter(k -> k.getValue() != null)
                .forEach(k -> builder.queryParam(k.getKey(), k.getValue()));
        return builder.toString();
    }
}
