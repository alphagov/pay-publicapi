package uk.gov.pay.api.service;

import uk.gov.pay.api.agreement.model.Agreement;
import uk.gov.pay.api.agreement.model.AgreementLedgerResponse;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.ledger.model.AgreementSearchParams;
import uk.gov.pay.api.ledger.model.SearchResults;
import uk.gov.pay.api.model.search.PaginationDecorator;

import javax.inject.Inject;
import java.util.stream.Collectors;

public class SearchAgreementsService {

    private static final String AGREEMENTS_PATH = "/v1/agreements";
    private final LedgerService ledgerService;
    private final PaginationDecorator paginationDecorator;

    @Inject
    public SearchAgreementsService(LedgerService ledgerService,
                                   PaginationDecorator paginationDecorator) {
        this.ledgerService = ledgerService;
        this.paginationDecorator = paginationDecorator;
    }

    public SearchResults<Agreement> searchLedgerAgreements(Account account, AgreementSearchParams params) {
        SearchResults<AgreementLedgerResponse> ledgerResponse = ledgerService.searchAgreements(account, params);
        return processLedgerResponse(ledgerResponse);
    }

    private SearchResults<Agreement> processLedgerResponse(SearchResults<AgreementLedgerResponse> searchResults) {
        return new SearchResults<>(searchResults.getTotal(),
                searchResults.getCount(),
                searchResults.getPage(),
                searchResults.getResults().stream().map(Agreement::from).collect(Collectors.toUnmodifiableList()),
                paginationDecorator.transformLinksToPublicApiUri(searchResults.getLinks(), AGREEMENTS_PATH));
    }
}
