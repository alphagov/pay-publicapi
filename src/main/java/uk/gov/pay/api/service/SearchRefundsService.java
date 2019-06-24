package uk.gov.pay.api.service;

import black.door.hate.HalRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.SearchRefundsException;
import uk.gov.pay.api.model.search.PaginationDecorator;
import uk.gov.pay.api.model.search.card.RefundForSearchRefundsResult;
import uk.gov.pay.api.model.search.card.SearchRefundsResponse;

import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_OK;
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

    private final ConnectorUriGenerator connectorUriGenerator;
    private final Client client;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final PaginationDecorator paginationDecorator;

    @Inject
    public SearchRefundsService(Client client,
                                ConnectorUriGenerator uriGenerator,
                                PublicApiUriGenerator publicApiUriGenerator,
                                PaginationDecorator paginationDecorator) {

        this.client = client;
        this.connectorUriGenerator = uriGenerator;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.paginationDecorator = paginationDecorator;
    }

    public Response getAllRefunds(Account account, RefundsParams params) {
        validateSearchParameters(params);
        Map<String, String> queryParams = buildQueryString(params);
        return getSearchResponse(account, queryParams);
    }

    private Map<String, String> buildQueryString(RefundsParams params) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put(FROM_DATE, params.getFromDate());
        queryParams.put(TO_DATE, params.getToDate());
        queryParams.put(PAGE, Optional.ofNullable(params.getPage()).orElse(DEFAULT_PAGE));
        queryParams.put(DISPLAY_SIZE, Optional.ofNullable(params.getDisplaySize()).orElse(DEFAULT_DISPLAY_SIZE));
        return queryParams;
    }

    private Response getSearchResponse(Account account, Map<String, String> queryParams) {
        String url = connectorUriGenerator.refundsURIWithParams(account, queryParams);
        Response connectorResponse = client
                .target(url)
                .request()
                .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                .get();
        LOGGER.info("response from connector for refunds search: {}", connectorResponse);
        if (connectorResponse.getStatus() == SC_OK) {
            return processResponse(connectorResponse);
        }
        throw new SearchRefundsException(connectorResponse);
    }

    private Response processResponse(Response connectorResponse) {
        try {
            SearchRefundsResponse searchResponse = connectorResponse.readEntity(SearchRefundsResponse.class);
            List<RefundForSearchRefundsResult> results = searchResponse.getRefunds()
                    .stream()
                    .map(refund -> RefundForSearchRefundsResult.valueOf(refund,
                            publicApiUriGenerator.getPaymentURI(refund.getChargeId()),
                            publicApiUriGenerator.getRefundsURI(refund.getChargeId(), refund.getRefundId())))
                    .collect(Collectors.toList());

            HalRepresentation.HalRepresentationBuilder halRepresentation = HalRepresentation
                    .builder()
                    .addProperty("results", results);
            return Response.ok().entity(
                    paginationDecorator.decoratePagination(halRepresentation, searchResponse, REFUNDS_PATH).build().toString())
                    .build();
        } catch (ProcessingException ex) {
            throw new SearchRefundsException(ex);
        }
    }
}
