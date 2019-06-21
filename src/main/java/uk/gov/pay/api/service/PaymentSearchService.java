package uk.gov.pay.api.service;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.search.card.SearchCardPayments;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.pay.api.validation.PaymentSearchValidator.validateSearchParameters;

public class PaymentSearchService {

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
    
    private final SearchCardPayments searchCardPayments;

    @Inject
    public PaymentSearchService(SearchCardPayments searchCardPayments) {
        this.searchCardPayments = searchCardPayments;
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
        
        return searchCardPayments.getSearchResponse(account, queryParams);
    }
}
