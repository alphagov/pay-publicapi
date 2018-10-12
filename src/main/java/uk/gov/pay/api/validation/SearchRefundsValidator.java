package uk.gov.pay.api.validation;

import uk.gov.pay.api.exception.RefundsValidationException;
import uk.gov.pay.api.service.RefundsParams;

import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.join;
import static uk.gov.pay.api.model.RefundError.Code.SEARCH_REFUNDS_VALIDATION_ERROR;
import static uk.gov.pay.api.model.RefundError.aRefundError;


public class SearchRefundsValidator extends SearchValidator {

    public static void validateSearchParameters(RefundsParams params) {
        String pageNumber = params.getPage();
        String displaySize = params.getDisplaySize();

        List<String> validationErrors = new LinkedList<>();
        try {
            validatePageIfNotNull(pageNumber, validationErrors);
            validateDisplaySizeIfNotNull(displaySize, validationErrors);
        } catch (Exception e) {
            throw new RefundsValidationException(aRefundError(SEARCH_REFUNDS_VALIDATION_ERROR, join(validationErrors, ", "), e.getMessage()));
        }
        if (!validationErrors.isEmpty()) {
            throw new RefundsValidationException(aRefundError(SEARCH_REFUNDS_VALIDATION_ERROR, join(validationErrors, ", ")));
        }
    }
}
