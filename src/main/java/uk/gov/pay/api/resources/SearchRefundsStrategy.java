package uk.gov.pay.api.resources;


import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.search.card.SearchRefundsResults;
import uk.gov.pay.api.service.RefundsParams;
import uk.gov.pay.api.service.SearchRefundsService;

public class SearchRefundsStrategy extends LedgerOrConnectorStrategyTemplate<SearchRefundsResults> {

    private final Account account;
    private RefundsParams refundsParams;
    private final SearchRefundsService searchRefundsService;

    public SearchRefundsStrategy(String strategy, Account account, RefundsParams refundsParams,
                                 SearchRefundsService searchRefundsService) {
        super(strategy);
        this.account = account;
        this.refundsParams = refundsParams;
        this.searchRefundsService = searchRefundsService;
    }

    @Override
    protected SearchRefundsResults executeLedgerOnlyStrategy() {
        return searchRefundsService.searchLedgerRefunds(account, refundsParams);
    }

    @Override
    protected SearchRefundsResults executeFutureBehaviourStrategy() {
        return executeLedgerOnlyStrategy();
    }

    @Override
    protected SearchRefundsResults executeDefaultStrategy() {
        return searchRefundsService.searchConnectorRefunds(account, refundsParams);
    }
}
