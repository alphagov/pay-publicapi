package uk.gov.pay.api.model.directdebit.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.exception.SearchPaymentsException;
import uk.gov.pay.api.model.response.HalLink;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.response.Link;
import uk.gov.pay.api.model.response.PaymentForSearchResult;
import uk.gov.pay.api.model.response.PaymentSearchResults;
import uk.gov.pay.api.model.response.PaymentState;
import uk.gov.pay.api.model.response.SearchNavigationLinks;
import uk.gov.pay.api.model.search.SearchPayments;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.PaymentUriGenerator;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.pay.api.service.PaymentSearchService.AGREEMENT_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.DISPLAY_SIZE;
import static uk.gov.pay.api.service.PaymentSearchService.EMAIL_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.FROM_DATE_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.PAGE;
import static uk.gov.pay.api.service.PaymentSearchService.REFERENCE_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.STATE_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.TO_DATE_KEY;

public class SearchDirectDebitPayments implements SearchPayments {

    private static final String PAYMENT_PATH = "v1/payments";
    private static final Logger logger = LoggerFactory.getLogger(SearchDirectDebitPayments.class);
    protected final PaymentUriGenerator paymentUriGenerator;
    protected final ConnectorUriGenerator connectorUriGenerator;
    protected final Client client;
    protected final ObjectMapper objectMapper;
    protected final String baseUrl;

    public SearchDirectDebitPayments(Client client,
                                     PublicApiConfig configuration,
                                     ConnectorUriGenerator connectorUriGenerator,
                                     PaymentUriGenerator paymentUriGenerator,
                                     ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.connectorUriGenerator = connectorUriGenerator;
        this.baseUrl = configuration.getBaseUrl();
        this.paymentUriGenerator = paymentUriGenerator;
    }
    
    @Override
    public Response getSearchResponse(Account account, Map<String, String> queryParams) {
        validateSupportedSearchParams(queryParams);
        DirectDebitSearchResponse searchResponse = callDDConnector(account, queryParams);
        return buildSearchResults(searchResponse);
    }

    private Response buildSearchResults(DirectDebitSearchResponse searchResponse) {
        List<PaymentForSearchResult> results = searchResponse
                .results
                .stream()
                .map(this::oneDirectDebitSearchResult)
                .collect(Collectors.toList());

        PaymentSearchResults p = new PaymentSearchResults(
                searchResponse.total,
                searchResponse.count,
                searchResponse.page,
                results,
                getLinks(searchResponse)
        );

        return Response.ok().entity(p).build();
    }

    private PaymentForSearchResult oneDirectDebitSearchResult(DirectDebitTransactionFromResponse transaction) {
//        transaction.getName(),
//        transaction.getAgreementId()
        
        final URI selfLink = paymentUriGenerator.getPaymentURI(baseUrl, transaction.getTransactionId());
        
        
        return new PaymentForSearchResult(
                transaction.getTransactionId(),
                transaction.getAmount(),
                PaymentState.from(transaction.getState()),
                null,
                transaction.getDescription(),
                transaction.getReference(),
                transaction.getEmail(),
                null,
                transaction.getCreatedDate(),
                null,
                false,
                null,
                null,
                null,
                transaction.getLinks(),
                selfLink,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private DirectDebitSearchResponse callDDConnector(Account account, Map<String, String> queryParams) {
        String url = connectorUriGenerator.directDebitTransactionsURI(account, queryParams);
        Response connectorResponse = client
                .target(url)
                .request()
                .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                .get();
        logger.info("response from dd connector for transaction search: {}", connectorResponse);
        if (connectorResponse.getStatus() != SC_OK) {
            throw new SearchPaymentsException(connectorResponse);
        }
        return getDirectDebitSearchResponse(connectorResponse);
    }

    private DirectDebitSearchResponse getDirectDebitSearchResponse(Response connectorResponse) {
        try {
            return connectorResponse.readEntity(DirectDebitSearchResponse.class);
        } catch (ProcessingException ex) {
            throw new SearchPaymentsException(ex);
        }
    }

    public Set<String> getSupportedSearchParams() {
        return ImmutableSet.of(REFERENCE_KEY, EMAIL_KEY, STATE_KEY, AGREEMENT_KEY, FROM_DATE_KEY, TO_DATE_KEY, PAGE, DISPLAY_SIZE);
    }

    public void validateSupportedSearchParams(Map<String, String> queryParams) {
        queryParams.entrySet().stream()
                .filter(this::isUnsupportedParamWithNonBlankValue)
                .findFirst()
                .ifPresent(invalidParam -> {
                    throw new BadRequestException(PaymentError
                            .aPaymentError(PaymentError.Code.SEARCH_PAYMENTS_VALIDATION_ERROR, invalidParam.getKey()));
                });
    }

    public boolean isUnsupportedParamWithNonBlankValue(Map.Entry<String, String> queryParam) {
        return !getSupportedSearchParams().contains(queryParam.getKey()) && isNotBlank(queryParam.getValue());
    }

    private Link transformIntoPublicUri(DirectDebitConnectorLink link) {
        if (link == null)
            return null;

        final String href;
        try {
            href = UriBuilder.fromUri(this.baseUrl)
                    .path(PAYMENT_PATH)
                    .replaceQuery(new URI(link.href).getQuery())
                    .build()
                    .toString();
        } catch (URISyntaxException e) {
            throw new SearchPaymentsException(e);
        }
        return new Link(href, link.method);
    }
    
    private SearchNavigationLinks getLinks(DirectDebitSearchResponse response) {
        DirectDebitSearchNavigationLinks links = response.links;

        if (links == null) {
            links = new DirectDebitSearchNavigationLinks();
        }
        return new SearchNavigationLinks(
                transformIntoPublicUri(links.self),
                transformIntoPublicUri(links.firstPage),
                transformIntoPublicUri(links.lastPage),
                transformIntoPublicUri(links.prevPage),
                transformIntoPublicUri(links.nextPage)
        );
    }
}
