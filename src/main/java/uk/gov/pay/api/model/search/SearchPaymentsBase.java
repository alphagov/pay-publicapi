package uk.gov.pay.api.model.search;

import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.PaymentUriGenerator;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Map;

public abstract class SearchPaymentsBase extends SearchBase {

    protected final PaymentUriGenerator paymentUriGenerator;

    public SearchPaymentsBase(Client client,
                              PublicApiConfig configuration,
                              ConnectorUriGenerator connectorUriGenerator,
                              PaymentUriGenerator paymentUriGenerator) {
        super(client, configuration, connectorUriGenerator);
        this.paymentUriGenerator = paymentUriGenerator;
    }

    public abstract Response getSearchResponse(Account account, Map<String, String> queryParams);

    protected void validateSupportedSearchParams(Map<String, String> queryParams) {
        queryParams.entrySet().stream()
                .filter(this::isUnsupportedParamWithNonBlankValue)
                .findFirst()
                .ifPresent(invalidParam -> {
                    throw new BadRequestException(PaymentError
                            .aPaymentError(PaymentError.Code.SEARCH_PAYMENTS_VALIDATION_ERROR, invalidParam.getKey()));
                });
    }
}
