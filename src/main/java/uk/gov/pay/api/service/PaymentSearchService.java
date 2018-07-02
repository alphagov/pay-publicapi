package uk.gov.pay.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.search.PaymentSearchFactory;
import uk.gov.pay.api.model.search.SearchPaymentsBase;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.List;

import static java.util.Arrays.asList;
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
    private final String baseUrl;
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
        this.baseUrl = configuration.getBaseUrl();
    }
    
    public Response doSearch(Account account, String reference, String email, String state, String cardBrand,
                             String fromDate, String toDate, String pageNumber, String displaySize, String agreementId) {
        
        validateSearchParameters(state, reference, email, cardBrand, fromDate, toDate, pageNumber, displaySize, agreementId);

        if (isNotBlank(cardBrand)) {
            cardBrand = cardBrand.toLowerCase();
        }
        List<Pair<String, String>> queryParams = asList(
                Pair.of(REFERENCE_KEY, reference),
                Pair.of(EMAIL_KEY, email),
                Pair.of(STATE_KEY, state),
                Pair.of(CARD_BRAND_KEY, cardBrand),
                Pair.of(AGREEMENT_KEY, agreementId),
                Pair.of(FROM_DATE_KEY, fromDate),
                Pair.of(TO_DATE_KEY, toDate),
                Pair.of(PAGE, pageNumber),
                Pair.of(DISPLAY_SIZE, displaySize)
        );

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
