package uk.gov.pay.api.service;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.ledger.SearchDisputeResponseFromLedger;
import uk.gov.pay.api.model.search.PaginationDecorator;
import uk.gov.pay.api.model.search.dispute.DisputeForSearchResult;
import uk.gov.pay.api.model.search.dispute.DisputeSearchResults;

import javax.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

public class SearchDisputesService {
    private static final String DISPUTES_PATH = "/v1/disputes";
    private final LedgerService ledgerService;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final PaginationDecorator paginationDecorator;

    @Inject
    public SearchDisputesService(LedgerService ledgerService,
                                PublicApiUriGenerator publicApiUriGenerator,
                                PaginationDecorator paginationDecorator) {

        this.ledgerService = ledgerService;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.paginationDecorator = paginationDecorator;
    }

    public DisputeSearchResults searchDisputes(Account account, DisputesSearchParams params) {
        SearchDisputeResponseFromLedger disputesFromLedger = ledgerService.searchDisputes(account, params.getParamsAsMap());
        return processLedgerResponse(disputesFromLedger);
    }

    private DisputeSearchResults processLedgerResponse(SearchDisputeResponseFromLedger searchResponse) {
        List<DisputeForSearchResult> results = searchResponse.getDisputes()
                .stream()
                .map(dispute -> DisputeForSearchResult.valueOf(dispute,
                        publicApiUriGenerator.getPaymentURI(dispute.getParentTransactionId())))
                        .collect(Collectors.toList());

        reWriteSearchLinks(searchResponse);

        return new DisputeSearchResults(searchResponse.getTotal(), searchResponse.getCount(), searchResponse.getPage(),
                results, paginationDecorator.transformLinksToPublicApiUri(searchResponse.getLinks(), DISPUTES_PATH));
    }

    private void reWriteSearchLinks(SearchDisputeResponseFromLedger searchResponse) {
        var links = searchResponse.getLinks();
        if (links.getSelf() != null) {
            links.withSelfLink(links.getSelf().getHref().replace("state", "status"));
        }
        if (links.getFirstPage() != null) {
            links.withFirstLink(links.getFirstPage().getHref().replaceAll("state", "status"));
        }
        if (links.getLastPage() != null) {
            links.withLastLink(links.getLastPage().getHref().replaceAll("state", "status"));
        }
        if (links.getPrevPage() != null) {
            links.withPrevLink(links.getPrevPage().getHref().replaceAll("state", "status"));
        }
        if (links.getLastPage() != null) {
            links.withLastLink(links.getLastPage().getHref().replaceAll("state", "status"));
        }
    }
}
