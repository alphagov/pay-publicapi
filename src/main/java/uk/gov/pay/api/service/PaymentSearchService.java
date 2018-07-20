package uk.gov.pay.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.search.PaymentSearchFactory;
import uk.gov.pay.api.model.search.SearchPaymentsBase;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.pay.api.validation.PaymentSearchValidator.validateSearchParameters;

public class PaymentSearchService {

    private static final String REFERENCE_KEY = "reference";
    private static final String EMAIL_KEY = "email";
    private static final String STATE_KEY = "state";
    private static final String CARD_BRAND_KEY = "card_brand";
    private static final String FROM_DATE_KEY = "from_date";
    private static final String TO_DATE_KEY = "to_date";
    private static final String PAGE = "page";
    private static final String DISPLAY_SIZE = "display_size";
    private static final String AGREEMENT_KEY = "agreement_id";
    private final ConnectorUriGenerator connectorUriGenerator;
    private final Client client;
    private final ObjectMapper objectMapper;
    private final PaymentUriGenerator paymentUriGenerator;
    private final PublicApiConfig configuration;

    @Inject
    public PaymentSearchService(Client client,
                                PublicApiConfig configuration,
                                ConnectorUriGenerator connectorUriGenerator,
                                PaymentUriGenerator paymentUriGenerator,
                                ObjectMapper objectMapper) {
        this.client = client;
        this.configuration = configuration;
        this.connectorUriGenerator = connectorUriGenerator;
        this.paymentUriGenerator = paymentUriGenerator;
        this.objectMapper = objectMapper;
    }
    
    public Response doSearch(Account account, String reference, String email, String state, String cardBrand,
                             String fromDate, String toDate, String pageNumber, String displaySize, String agreementId) {
        
        validateSearchParameters(account, state, reference, email, cardBrand, fromDate, toDate, pageNumber, displaySize, agreementId);

        if (isNotBlank(cardBrand)) {
            cardBrand = cardBrand.toLowerCase();
        }
        
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put(REFERENCE_KEY, reference);
        queryParams.put(EMAIL_KEY, email);
        queryParams.put(STATE_KEY, state);
        queryParams.put(CARD_BRAND_KEY, cardBrand);
        queryParams.put(AGREEMENT_KEY, agreementId);
        queryParams.put(FROM_DATE_KEY, fromDate);
        queryParams.put(TO_DATE_KEY, toDate);
        queryParams.put(PAGE, pageNumber);
        queryParams.put(DISPLAY_SIZE, displaySize);
        
        SearchPaymentsBase paymentsService = PaymentSearchFactory.getPaymentService(
                                account,
                                client,
                                configuration, 
                                connectorUriGenerator,
                                paymentUriGenerator,
                                objectMapper);
        
        return paymentsService.getSearchResponse(account, queryParams);
    }
}
