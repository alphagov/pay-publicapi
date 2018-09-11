package uk.gov.pay.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.ValidationException;
import uk.gov.pay.api.model.search.card.SearchCardRefunds;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.join;
import static org.eclipse.jetty.util.StringUtil.isNotBlank;
import static uk.gov.pay.api.model.PaymentError.Code.SEARCH_PAYMENTS_VALIDATION_ERROR;
import static uk.gov.pay.api.model.PaymentError.aPaymentError;

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

    public void validateRefundSearchParameters(String page, String displaySize) {
        List<String> validationErrors = new LinkedList<>();
        try {
            validatePageIfNotNull(page, validationErrors);
            validateDisplaySizeIfNotNull(displaySize, validationErrors);
        } catch (Exception e) {
            throw new ValidationException(aPaymentError(SEARCH_PAYMENTS_VALIDATION_ERROR, join(validationErrors, ", "), e.getMessage()));
        }
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(aPaymentError(SEARCH_PAYMENTS_VALIDATION_ERROR, join(validationErrors, ", ")));
        }
    }

    private void validatePageIfNotNull(String pageNumber, List<String> validationErrors) {
        if (isNotBlank(pageNumber) && (!StringUtils.isNumeric(pageNumber) || Integer.valueOf(pageNumber) < 1)) {
            validationErrors.add("page");
        }
    }

    private void validateDisplaySizeIfNotNull(String displaySize, List<String> validationErrors) {
        if (isNotBlank(displaySize) && (!StringUtils.isNumeric(displaySize) || Integer.valueOf(displaySize) < 1 || Integer.valueOf(displaySize) > 500)) {
            validationErrors.add("display_size");
        }
    }

}
