package uk.gov.pay.api.service;

import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriBuilder;
import java.util.Collections;
import java.util.Map;

import static java.lang.String.format;

public class ConnectorUriGenerator {
    private final PublicApiConfig configuration;

    @Inject
    public ConnectorUriGenerator(PublicApiConfig configuration) {
        this.configuration = configuration;
    }

    String chargesURI(Account account) {
        return buildConnectorUri(format("/v1/api/accounts/%s/charges", account.accountId()));
    }

    String chargeURI(Account account, String chargeId) {
        String path = format("/v1/api/accounts/%s/charges/%s", account.accountId(), chargeId); //TODO rename to /payments instead /charges
        return buildConnectorUri(path);
    }

    public String chargeEventsURI(Account account, String paymentId) {
        String path = format("/v1/api/accounts/%s/charges/%s/events", account.accountId(), paymentId);
        return buildConnectorUri(path);
    }

    String cancelURI(Account account, String paymentId) {
        String path = format("/v1/api/accounts/%s/charges/%s/cancel", account.accountId(), paymentId);
        return buildConnectorUri(path, Collections.emptyMap());
    }

    public String telephoneChargesURI(Account account) {
        return buildConnectorUri(format("/v1/api/accounts/%s/telephone-charges", account.accountId()));
    }

    String captureURI(Account account, String chargeId) {
        String path = format("/v1/api/accounts/%s/charges/%s/capture", account.accountId(), chargeId);
        return buildConnectorUri(path);
    }

    String refundForPaymentURI(String accountId, String chargeId, String refundId) {
        String path = format("/v1/api/accounts/%s/charges/%s/refunds/%s", accountId, chargeId, refundId);
        return buildConnectorUri(path);
    }

    String authorisationURI() {
        String path = "/v1/api/charges/authorise";
        return buildConnectorUri(path);
    }

    public String getAgreementURI(Account account) {
        String path = format("/v1/api/accounts/%s/agreements", account.accountId());
        return buildConnectorUri(path);
    }

    public String cancelAgreementURI(Account account, String agreementId) {
        String path = format("/v1/api/accounts/%s/agreements/%s/cancel", account.accountId(), agreementId);
        return buildConnectorUri(path, Collections.emptyMap());
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
