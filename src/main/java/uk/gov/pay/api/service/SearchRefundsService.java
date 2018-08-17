package uk.gov.pay.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.search.card.SearchCardRefunds;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

import static uk.gov.pay.api.validation.PaymentSearchValidator.validateRefundSearchParameters;

public class SearchRefundsService {
    private static final String PAGE = "page";
    private static final String DISPLAY_SIZE = "display_size";
    private final RefundsUriGenerator refundsUriGenerator;
    private final Client client;
    private final ObjectMapper objectMapper;
    private final PublicApiConfig configuration;

    @Inject
    public SearchRefundsService(Client client,
                                PublicApiConfig configuration,
                                RefundsUriGenerator refundsUriGenerator,
                                ObjectMapper objectMapper) {

        this.client = client;
        this.configuration = configuration;
        this.refundsUriGenerator = refundsUriGenerator;
        this.objectMapper = objectMapper;
    }
    
    public Response getAllRefunds(Account account, String page, String displaySize) {
        validateRefundSearchParameters(page, displaySize);
        
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put(PAGE, page);
        queryParams.put(DISPLAY_SIZE, displaySize);

        SearchCardRefunds refundsService = new SearchCardRefunds(
                client, 
                configuration, 
                refundsUriGenerator,
                objectMapper
        );
        
        return refundsService.getSearchResponse(account, queryParams);
    }
}
