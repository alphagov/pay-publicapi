package uk.gov.pay.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.search.PaginationDecorator;
import uk.gov.pay.api.model.search.card.RefundForSearchRefundsResult;
import uk.gov.pay.api.model.search.card.SearchRefundsResponseFromConnector;
import uk.gov.pay.api.model.search.card.SearchRefundsResults;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.pay.api.validation.RefundSearchValidator.validateSearchParameters;

public class SearchRefundsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchRefundsService.class);
    private static final String REFUNDS_PATH = "/v1/refunds";

    private static final String PAGE = "page";
    private static final String DISPLAY_SIZE = "display_size";
    private static final String DEFAULT_PAGE = "1";
    private static final String DEFAULT_DISPLAY_SIZE = "500";
    private static final String FROM_DATE = "from_date";
    private static final String TO_DATE = "to_date";

    private final PublicApiUriGenerator publicApiUriGenerator;
    private final PaginationDecorator paginationDecorator;
    private ConnectorService connectorService;

    @Inject
    public SearchRefundsService(ConnectorService connectorService,
                                PublicApiUriGenerator publicApiUriGenerator,
                                PaginationDecorator paginationDecorator) {

        this.connectorService = connectorService;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.paginationDecorator = paginationDecorator;
    }

    public SearchRefundsResults searchConnectorRefunds(Account account, RefundsParams params) {
        validateSearchParameters(params);
        Map<String, String> queryParams = buildQueryString(params);
        return getConnectorSearchRefundsResponse(account, queryParams);
    }

    private Map<String, String> buildQueryString(RefundsParams params) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put(FROM_DATE, params.getFromDate());
        queryParams.put(TO_DATE, params.getToDate());
        queryParams.put(PAGE, Optional.ofNullable(params.getPage()).orElse(DEFAULT_PAGE));
        queryParams.put(DISPLAY_SIZE, Optional.ofNullable(params.getDisplaySize()).orElse(DEFAULT_DISPLAY_SIZE));
        return queryParams;
    }

    private SearchRefundsResults getConnectorSearchRefundsResponse(Account account, Map<String, String> queryParams) {
        SearchRefundsResponseFromConnector searchRefundsResponseFromConnector = connectorService.searchRefunds(account, queryParams);
        return processResponse(searchRefundsResponseFromConnector);
    }

    private SearchRefundsResults processResponse(SearchRefundsResponseFromConnector searchResponse) {
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
