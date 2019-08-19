package uk.gov.pay.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.ledger.SearchRefundsResponseFromLedger;
import uk.gov.pay.api.model.search.PaginationDecorator;
import uk.gov.pay.api.model.search.card.RefundForSearchRefundsResult;
import uk.gov.pay.api.model.search.card.SearchRefundsResponseFromConnector;
import uk.gov.pay.api.model.search.card.SearchRefundsResults;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.pay.api.validation.RefundSearchValidator.validateSearchParameters;

public class SearchRefundsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchRefundsService.class);
    private static final String REFUNDS_PATH = "/v1/refunds";
    private final PaginationDecorator paginationDecorator;
    private LedgerService ledgerService;
    private PublicApiUriGenerator publicApiUriGenerator;
    private ConnectorService connectorService;

    @Inject
    public SearchRefundsService(ConnectorService connectorService,
                                LedgerService ledgerService,
                                PublicApiUriGenerator publicApiUriGenerator,
                                PaginationDecorator paginationDecorator) {

        this.connectorService = connectorService;
        this.ledgerService = ledgerService;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.paginationDecorator = paginationDecorator;
    }

    public SearchRefundsResults searchConnectorRefunds(Account account, RefundsParams params) {
        validateSearchParameters(params);
        SearchRefundsResponseFromConnector searchRefundsResponseFromConnector
                = connectorService.searchRefunds(account, params.getParamsAsMap());
        return processConnectorResponse(searchRefundsResponseFromConnector);
    }

    public SearchRefundsResults searchLedgerRefunds(Account account, RefundsParams params) {
        validateSearchParameters(params);
        SearchRefundsResponseFromLedger refunds
                = ledgerService.searchRefunds(account, params.getParamsAsMap());
        return processLedgerResponse(refunds);
    }

    private SearchRefundsResults processLedgerResponse(SearchRefundsResponseFromLedger searchResponse) {
        List<RefundForSearchRefundsResult> results = searchResponse.getRefunds()
                .stream()
                .map(refund -> RefundForSearchRefundsResult.valueOf(
                        refund,
                        publicApiUriGenerator.getPaymentURI(refund.getParentTransactionId()),
                        publicApiUriGenerator.getRefundsURI(refund.getParentTransactionId(),
                                refund.getTransactionId()))
                )
                .collect(Collectors.toList());

        return new SearchRefundsResults(
                searchResponse.getTotal(),
                searchResponse.getCount(),
                searchResponse.getPage(),
                results,
                paginationDecorator.transformLinksToPublicApiUri(searchResponse.getLinks(), REFUNDS_PATH)
        );
    }

    private SearchRefundsResults processConnectorResponse(SearchRefundsResponseFromConnector searchResponse) {
        List<RefundForSearchRefundsResult> results = searchResponse.getRefunds()
                .stream()
                .map(refund -> RefundForSearchRefundsResult.valueOf(refund,
                        publicApiUriGenerator.getPaymentURI(refund.getChargeId()),
                        publicApiUriGenerator.getRefundsURI(refund.getChargeId(), refund.getRefundId())))
                .collect(Collectors.toList());

        return new SearchRefundsResults(
                searchResponse.getTotal(),
                searchResponse.getCount(),
                searchResponse.getPage(),
                results,
                paginationDecorator.transformLinksToPublicApiUri(searchResponse.getLinks(), REFUNDS_PATH)
        );
    }
}
