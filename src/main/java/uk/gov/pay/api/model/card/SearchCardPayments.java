package uk.gov.pay.api.model.card;

import black.door.hate.HalRepresentation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.exception.SearchPaymentsException;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.response.Link;
import uk.gov.pay.api.model.response.SearchNavigationLinks;
import uk.gov.pay.api.model.search.SearchPayments;
import uk.gov.pay.api.model.response.PaymentForSearchResult;
import uk.gov.pay.api.model.response.PaymentSearchResults;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.PaymentUriGenerator;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.pay.api.service.PaymentSearchService.CARDHOLDER_NAME_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.CARD_BRAND_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.DISPLAY_SIZE;
import static uk.gov.pay.api.service.PaymentSearchService.EMAIL_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.FIRST_DIGITS_CARD_NUMBER_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.FROM_DATE_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.LAST_DIGITS_CARD_NUMBER_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.PAGE;
import static uk.gov.pay.api.service.PaymentSearchService.REFERENCE_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.STATE_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.TO_DATE_KEY;

public class SearchCardPayments implements SearchPayments {

    private static final String PAYMENTS_PATH = "/v1/payments";
    private static final Logger logger = LoggerFactory.getLogger(SearchCardPayments.class);
    private final PaymentUriGenerator paymentUriGenerator;
    private final ConnectorUriGenerator connectorUriGenerator;
    private final Client client;
    private final String baseUrl;

    public SearchCardPayments(Client client,
                              PublicApiConfig configuration,
                              ConnectorUriGenerator connectorUriGenerator,
                              PaymentUriGenerator paymentUriGenerator,
                              ObjectMapper objectMapper) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
        this.baseUrl = configuration.getBaseUrl();
        this.paymentUriGenerator = paymentUriGenerator;
    }

    @Override
    public Response getSearchResponse(Account account, Map<String, String> queryParams) {
        validateSupportedSearchParams(queryParams);
        ConnectorSearchResponse connectorResponse = callConnector(account, queryParams);

        return buildSearchResults(connectorResponse);
    }


    private ConnectorSearchResponse callConnector(Account account, Map<String, String> queryParams) {
        try {
            String url = connectorUriGenerator.chargesURIWithParams(account, withTransactionType(queryParams));
            final Response connectorResponse = client
                    .target(url)
                    .request()
                    .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                    .get();

            logger.info("response from connector for transaction search: {}", connectorResponse);
            if (connectorResponse.getStatus() == SC_OK) {
                return connectorResponse.readEntity(ConnectorSearchResponse.class);
            }
            throw new SearchPaymentsException(connectorResponse);
        } catch (ProcessingException ex) {
            throw new SearchPaymentsException(ex);
        } catch (WebApplicationException ex) {
            throw new SearchPaymentsException(ex);
        }
    }

    private Map<String, String> withTransactionType(Map<String, String> queryParams) {
        HashMap<String, String> clone = new HashMap<>(queryParams);
        clone.put("transactionType", "charge");
        return clone;
    }

    private Response buildSearchResults(ConnectorSearchResponse response) {
        final List<PaymentForSearchResult> chargeFromResponses1 = response.getPayments()
                .stream()
                .map(this::onePaymentSearchResult)
                .collect(Collectors.toList());
        
        PaymentSearchResults p = new PaymentSearchResults(
                response.getTotal(),
                response.getCount(),
                response.getPage(),
                chargeFromResponses1,
                getLinks(response)
        );

        return Response.ok().entity(p).build();
    }

    private SearchNavigationLinks getLinks(ConnectorSearchResponse response) {
        ConnectorSearchNavigationLinks links = response.links;
        
        if (links == null) {
            links = new ConnectorSearchNavigationLinks();
        }
        return new SearchNavigationLinks(
            transformIntoPublicUri(links.self),
            transformIntoPublicUri(links.firstPage),
            transformIntoPublicUri(links.lastPage),
            transformIntoPublicUri(links.prevPage),
            transformIntoPublicUri(links.nextPage)
        );
    }

    private PaymentForSearchResult onePaymentSearchResult(ChargeFromResponse charge) {
        return PaymentForSearchResult.valueOf(
                charge,
                paymentUriGenerator.getPaymentURI(baseUrl, charge.getChargeId()),
                paymentUriGenerator.getPaymentEventsURI(baseUrl, charge.getChargeId()),
                paymentUriGenerator.getPaymentCancelURI(baseUrl, charge.getChargeId()),
                paymentUriGenerator.getPaymentRefundsURI(baseUrl, charge.getChargeId()),
                paymentUriGenerator.getPaymentCaptureURI(baseUrl, charge.getChargeId()));
    }

    Set<String> getSupportedSearchParams() {
        return ImmutableSet.of(REFERENCE_KEY, EMAIL_KEY, STATE_KEY, CARD_BRAND_KEY, CARDHOLDER_NAME_KEY, FIRST_DIGITS_CARD_NUMBER_KEY, LAST_DIGITS_CARD_NUMBER_KEY, FROM_DATE_KEY, TO_DATE_KEY, PAGE, DISPLAY_SIZE);

    }

    void validateSupportedSearchParams(Map<String, String> queryParams) {
        queryParams.entrySet()
                .stream()
                .filter(this::isUnsupportedParamWithNonBlankValue)
                .findFirst()
                .ifPresent(invalidParam -> {
                    throw new BadRequestException(PaymentError
                            .aPaymentError(PaymentError.Code.SEARCH_PAYMENTS_VALIDATION_ERROR, invalidParam.getKey()));
                });
    }

    boolean isUnsupportedParamWithNonBlankValue(Map.Entry<String, String> queryParam) {
        return !getSupportedSearchParams().contains(queryParam.getKey()) && isNotBlank(queryParam.getValue());
    }

    private void addLink(HalRepresentation.HalRepresentationBuilder halRepresentationBuilder, String name, URI uri) {
        if (uri != null) {
            halRepresentationBuilder.addLink(name, uri);
        }
    }

    private Link transformIntoPublicUri(ConnectorLink link) {
        if (link == null)
            return null;

        final String href;
        try {
            href = UriBuilder.fromUri(this.baseUrl)
                    .path(PAYMENTS_PATH)
                    .replaceQuery(new URI(link.href).getQuery())
                    .build()
                    .toString();
        } catch (URISyntaxException e) {
            throw new SearchPaymentsException(e);
        }
        return new Link(href, link.method);
    }

}
