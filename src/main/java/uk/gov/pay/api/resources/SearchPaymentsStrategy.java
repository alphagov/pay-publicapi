package uk.gov.pay.api.resources;

import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.service.PaymentSearchParams;
import uk.gov.pay.api.service.PaymentSearchService;

import javax.ws.rs.core.Response;

public class SearchPaymentsStrategy extends LedgerOrConnectorStrategyTemplate<Response> {
    private final Account account;
    private final PaymentSearchParams paymentSearchParams;
    private final PaymentSearchService paymentSearchService;

    public SearchPaymentsStrategy(PublicApiConfig configuration, String strategyName, Account account, PaymentSearchParams paymentSearchParams, PaymentSearchService paymentSearchService) {
        super(configuration, strategyName);
        this.account = account;
        this.paymentSearchParams = paymentSearchParams;
        this.paymentSearchService = paymentSearchService;
    }

    @Override
    protected Response executeLedgerOnlyStrategy() {
        return paymentSearchService.searchLedgerPayments(account, paymentSearchParams);
    }

    @Override
    protected Response executeFutureBehaviourStrategy() {
        return executeLedgerOnlyStrategy();
    }

    @Override
    protected Response executeDefaultStrategy() {
        return paymentSearchService.searchConnectorPayments(account, paymentSearchParams);
    }
}
