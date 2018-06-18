package uk.gov.pay.api.service;

import black.door.hate.HalRepresentation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.exception.SearchChargesException;
import uk.gov.pay.api.model.IPaymentSearchPagination;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.PaymentForSearchResult;
import uk.gov.pay.api.model.PaymentSearchResponse;
import uk.gov.pay.api.model.directdebit.search.DDSearchResponse;
import uk.gov.pay.api.model.directdebit.search.DDTransactionForSearch;
import uk.gov.pay.api.model.links.Link;

import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;
import static uk.gov.pay.api.validation.PaymentSearchValidator.validateSearchParameters;

public class PaymentSearchService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentSearchService.class);
    private static final String PAYMENTS_PATH = "/v1/payments";
    private static final String REFERENCE_KEY = "reference";
    private static final String EMAIL_KEY = "email";
    private static final String STATE_KEY = "state";
    private static final String CARD_BRAND_KEY = "card_brand";
    private static final String FROM_DATE_KEY = "from_date";
    private static final String TO_DATE_KEY = "to_date";
    private static final String PAGE = "page";
    private static final String DISPLAY_SIZE = "display_size";
    private static final String TRANSACTION_TYPE_KEY = "transactionType";
    private static final String TRANSACTION_TYPE_KEY_VALUE = "charge";
    private static final String MANDATE_KEY = "mandate";
    private final ConnectorUriGenerator connectorUriGenerator;
    private final Client client;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final PaymentUriGenerator paymentUriGenerator;

    @Inject
    public PaymentSearchService(Client client,
                                PublicApiConfig configuration,
                                ConnectorUriGenerator connectorUriGenerator,
                                PaymentUriGenerator paymentUriGenerator,
                                ObjectMapper objectMapper) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
        this.paymentUriGenerator = paymentUriGenerator;
        this.objectMapper = objectMapper;
        this.baseUrl = configuration.getBaseUrl();
    }
    
    public HalRepresentation doSearch(Account account, String reference, String email, String state, String cardBrand,
                             String fromDate, String toDate, String pageNumber, String displaySize, String mandateId) {

        if (!isDirectDebitAccount(account)) {
            validateSearchParameters(state, reference, email, cardBrand, fromDate, toDate, pageNumber, displaySize);
            if (mandateId != null) {
                throw new BadRequestException(PaymentError
                        .aPaymentError(PaymentError.Code.SEARCH_PAYMENTS_VALIDATION_ERROR, "agreement"));
            }
            if (isNotBlank(cardBrand)) {
                cardBrand = cardBrand.toLowerCase();
            }

            List<Pair<String, String>> queryParams = asList(
                    Pair.of(REFERENCE_KEY, reference),
                    Pair.of(EMAIL_KEY, email),
                    Pair.of(STATE_KEY, state),
                    Pair.of(CARD_BRAND_KEY, cardBrand),
                    Pair.of(FROM_DATE_KEY, fromDate),
                    Pair.of(TO_DATE_KEY, toDate),
                    Pair.of(TRANSACTION_TYPE_KEY, TRANSACTION_TYPE_KEY_VALUE),
                    Pair.of(PAGE, pageNumber),
                    Pair.of(DISPLAY_SIZE, displaySize)
            );
            Response connectorResponse = client
                    .target(connectorUriGenerator.chargesURI(
                            account,
                            queryParams))
                    .request()
                    .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                    .get();

            logger.info("response from connector form charge search: " + connectorResponse);

            if (connectorResponse.getStatus() == SC_OK) {
                return processConnectorResponse(connectorResponse);
            }
            throw new SearchChargesException(connectorResponse);
        }
        if (mandateId != null) {
            List<Pair<String, String>> queryParams = asList(
                    Pair.of(MANDATE_KEY, mandateId));
            Response response = client
                    .target(connectorUriGenerator.directDebitTransactionsURI(account, queryParams))
                    .request()
                    .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                    .get();
            if (response.getStatus() == SC_OK) {
                return processDirectDebitMandateResponse(response);
            }
            throw new SearchChargesException(response);
        }
        return processDirectDebitSearchResponse(null);
    }
    
    private HalRepresentation.HalRepresentationBuilder decoratePagination(HalRepresentation.HalRepresentationBuilder halRepresentationBuilder, IPaymentSearchPagination pagination) {
        try {
            halRepresentationBuilder
                    .addProperty("count", pagination.getCount())
                    .addProperty("total", pagination.getTotal())
                    .addProperty("page", pagination.getPage());
            addLink(halRepresentationBuilder, "self", transformIntoPublicUri(baseUrl, pagination.getLinks().getSelf()));
            addLink(halRepresentationBuilder, "first_page", transformIntoPublicUri(baseUrl, pagination.getLinks().getFirstPage()));
            addLink(halRepresentationBuilder, "last_page", transformIntoPublicUri(baseUrl, pagination.getLinks().getLastPage()));
            addLink(halRepresentationBuilder, "prev_page", transformIntoPublicUri(baseUrl, pagination.getLinks().getPrevPage()));
            addLink(halRepresentationBuilder, "next_page", transformIntoPublicUri(baseUrl, pagination.getLinks().getNextPage()));
        } catch (URISyntaxException ex) {
            throw new SearchChargesException(ex);
        }
        return halRepresentationBuilder;
    }
    
    private HalRepresentation processConnectorResponse(Response connectorResponse) {
        try {
            JsonNode responseJson = connectorResponse.readEntity(JsonNode.class);
            TypeReference<PaymentSearchResponse> typeRef = new TypeReference<PaymentSearchResponse>() {};
            PaymentSearchResponse searchResponse = objectMapper.readValue(responseJson.traverse(), typeRef);
            List<PaymentForSearchResult> chargeFromResponses = searchResponse.getPayments()
                    .stream()
                    .map(charge -> PaymentForSearchResult.valueOf(
                            charge,
                            paymentUriGenerator.getPaymentURI(baseUrl, charge.getChargeId()),
                            paymentUriGenerator.getPaymentEventsURI(baseUrl, charge.getChargeId()),
                            paymentUriGenerator.getPaymentCancelURI(baseUrl, charge.getChargeId()),
                            paymentUriGenerator.getPaymentRefundsURI(baseUrl, charge.getChargeId())))
                    .collect(Collectors.toList());
            HalRepresentation.HalRepresentationBuilder halRepresentation = HalRepresentation.builder()
                    .addProperty("results", chargeFromResponses);

            return decoratePagination(halRepresentation, searchResponse).build();
        } catch (IOException | ProcessingException ex) {
            throw new SearchChargesException(ex);
        }
    }
    
    private HalRepresentation processDirectDebitMandateResponse(Response directDebitConnectorMandateResponse) {
        try {
            JsonNode responseJson = directDebitConnectorMandateResponse.readEntity(JsonNode.class);
            TypeReference<DDSearchResponse> typeRef =
                    new TypeReference<DDSearchResponse>() {
                    };
            DDSearchResponse searchResponse = objectMapper.readValue(responseJson.traverse(), typeRef);
            List<DDTransactionForSearch> transactionFromResponse =
                    searchResponse
                            .getPayments()
                            .stream()
                            .map(transaction -> DDTransactionForSearch.valueOf(
                                    transaction,
                                    paymentUriGenerator.getPaymentURI(baseUrl, transaction.getTransactionId())
                            )).collect(Collectors.toList());
            HalRepresentation.HalRepresentationBuilder halRepresentation = HalRepresentation.builder()
                    .addProperty("payer", searchResponse.getPayer())
                    .addProperty("results", transactionFromResponse);
            return decoratePagination(halRepresentation, searchResponse).build();
        } catch (IOException | ProcessingException ex) {
            throw new SearchChargesException(ex);
        }
    }
    
    private HalRepresentation processDirectDebitSearchResponse(Response response) {
        throw new NotImplementedException("Direct Debit search is not yet implemented");
    }

    private boolean isDirectDebitAccount(Account account) {
        return account.getPaymentType().equals(DIRECT_DEBIT);
    }

    private void addLink(HalRepresentation.HalRepresentationBuilder halRepresentationBuilder, String name, URI uri) {
        if (uri != null) {
            halRepresentationBuilder.addLink(name, uri); 
        }
    }

    private URI transformIntoPublicUri(String baseUrl, Link link) throws URISyntaxException {
        if (link == null)
            return null;

        return UriBuilder.fromUri(baseUrl)
                .path(PAYMENTS_PATH)
                .replaceQuery(new URI(link.getHref()).getQuery())
                .build();
    }
}
