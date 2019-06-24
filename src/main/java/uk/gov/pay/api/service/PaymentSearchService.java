package uk.gov.pay.api.service;

import black.door.hate.HalRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.SearchPaymentsException;
import uk.gov.pay.api.model.search.PaginationDecorator;
import uk.gov.pay.api.model.search.card.PaymentForSearchResult;
import uk.gov.pay.api.model.search.card.PaymentSearchResponse;

import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.pay.api.validation.PaymentSearchValidator.validateSearchParameters;

public class PaymentSearchService {

    private static final String PAYMENTS_PATH = "/v1/payments";
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentSearchService.class);
    
    public static final String REFERENCE_KEY = "reference";
    public static final String EMAIL_KEY = "email";
    public static final String STATE_KEY = "state";
    public static final String CARD_BRAND_KEY = "card_brand";
    public static final String FIRST_DIGITS_CARD_NUMBER_KEY = "first_digits_card_number";
    public static final String LAST_DIGITS_CARD_NUMBER_KEY = "last_digits_card_number";
    public static final String CARDHOLDER_NAME_KEY = "cardholder_name";
    public static final String FROM_DATE_KEY = "from_date";
    public static final String TO_DATE_KEY = "to_date";
    public static final String PAGE = "page";
    public static final String DISPLAY_SIZE = "display_size";
    
    private final ConnectorUriGenerator connectorUriGenerator;
    private final Client client;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final PaginationDecorator paginationDecorator;

    @Inject
    public PaymentSearchService(Client client,
                                ConnectorUriGenerator connectorUriGenerator,
                                PublicApiUriGenerator publicApiUriGenerator,
                                PaginationDecorator paginationDecorator) {
        this.client = client;
        this.connectorUriGenerator = connectorUriGenerator;
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.paginationDecorator = paginationDecorator;
    }
    
    public Response doSearch(Account account, String reference, String email, String state, String cardBrand,
                             String fromDate, String toDate, String pageNumber, String displaySize, String agreementId, String cardHolderName, String firstDigitsCardNumber, String lastDigitsCardNumber) {
        
        validateSearchParameters(account, state, reference, email, cardBrand, fromDate, toDate, pageNumber, displaySize, agreementId, firstDigitsCardNumber, lastDigitsCardNumber);

        if (isNotBlank(cardBrand)) {
            cardBrand = cardBrand.toLowerCase();
        }
        
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put(REFERENCE_KEY, reference);
        queryParams.put(EMAIL_KEY, email);
        queryParams.put(STATE_KEY, state);
        queryParams.put(CARD_BRAND_KEY, cardBrand);
        queryParams.put(CARDHOLDER_NAME_KEY, cardHolderName);
        queryParams.put(FIRST_DIGITS_CARD_NUMBER_KEY, firstDigitsCardNumber);
        queryParams.put(LAST_DIGITS_CARD_NUMBER_KEY, lastDigitsCardNumber);
        queryParams.put(FROM_DATE_KEY, fromDate);
        queryParams.put(TO_DATE_KEY, toDate);
        queryParams.put(PAGE, pageNumber);
        queryParams.put(DISPLAY_SIZE, displaySize);
        
        return getSearchResponse(account, queryParams);
    }
    
    private Response getSearchResponse(Account account, Map<String, String> queryParams) {
        queryParams.put("transactionType", "charge");

        String url = connectorUriGenerator.chargesURIWithParams(account, queryParams);
        Response connectorResponse = client
                .target(url)
                .request()
                .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                .get();
        LOGGER.info("response from connector for transaction search: {}", connectorResponse);
        if (connectorResponse.getStatus() == SC_OK) {
            return processResponse(connectorResponse);
        }
        throw new SearchPaymentsException(connectorResponse);
    }

    private Response processResponse(Response connectorResponse) {
        PaymentSearchResponse response;
        try {
            response = connectorResponse.readEntity(PaymentSearchResponse.class);
        } catch (ProcessingException ex) {
            throw new SearchPaymentsException(ex);
        }
        List<PaymentForSearchResult> chargeFromResponses = response.getPayments()
                .stream()
                .map(charge -> PaymentForSearchResult.valueOf(
                        charge,
                        publicApiUriGenerator.getPaymentURI(charge.getChargeId()),
                        publicApiUriGenerator.getPaymentEventsURI(charge.getChargeId()),
                        publicApiUriGenerator.getPaymentCancelURI(charge.getChargeId()),
                        publicApiUriGenerator.getPaymentRefundsURI(charge.getChargeId()),
                        publicApiUriGenerator.getPaymentCaptureURI(charge.getChargeId())))
                .collect(Collectors.toList());
        HalRepresentation.HalRepresentationBuilder halRepresentation = HalRepresentation
                .builder()
                .addProperty("results", chargeFromResponses);

        return Response.ok().entity(paginationDecorator.decoratePagination(halRepresentation, response, PAYMENTS_PATH).build().toString()).build();
    }
}
