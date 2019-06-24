package uk.gov.pay.api.service.directdebit;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.search.directdebit.SearchDirectDebitPayments;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

import static uk.gov.pay.api.validation.PaymentSearchValidator.validateSearchParameters;

public class DirectDebitPaymentSearchService {

    public static final String REFERENCE_KEY = "reference";
    public static final String STATE_KEY = "state";
    public static final String MANDATE_ID_KEY = "mandate_id";
    public static final String FROM_DATE_KEY = "from_date";
    public static final String TO_DATE_KEY = "to_date";
    public static final String PAGE = "page";
    public static final String DISPLAY_SIZE = "display_size";
    
    private final SearchDirectDebitPayments searchDirectDebitPayments;

    @Inject
    public DirectDebitPaymentSearchService(SearchDirectDebitPayments searchDirectDebitPayments) {
        this.searchDirectDebitPayments = searchDirectDebitPayments;
    }

    public Response doSearch(Account account, String reference, String state, String mandateId, String fromDate,
                             String toDate, String pageNumber, String displaySize) {
        // TODO: do validation in resource
        validateSearchParameters(account, state, reference, null, null, fromDate, toDate, pageNumber,
                displaySize, mandateId, null, null);
        
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put(REFERENCE_KEY, reference);
        queryParams.put(STATE_KEY, state);
        queryParams.put(MANDATE_ID_KEY, mandateId);
        queryParams.put(FROM_DATE_KEY, fromDate);
        queryParams.put(TO_DATE_KEY, toDate);
        queryParams.put(PAGE, pageNumber);
        queryParams.put(DISPLAY_SIZE, displaySize);

        return searchDirectDebitPayments.getSearchResponse(account, queryParams);
    }
}
