package uk.gov.pay.api.model.search;

import black.door.hate.HalRepresentation;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.exception.SearchChargesException;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.links.PaymentSearchNavigationLinks;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.PaymentUriGenerator;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;
import java.util.Map;

public abstract class SearchPaymentsBase extends SearchBase {

    private static final String PAYMENTS_PATH = "/v1/payments";
    protected final PaymentUriGenerator paymentUriGenerator;

    public SearchPaymentsBase(Client client,
                              PublicApiConfig configuration,
                              ConnectorUriGenerator connectorUriGenerator,
                              PaymentUriGenerator paymentUriGenerator,
                              ObjectMapper objectMapper) {
        super(client, configuration, connectorUriGenerator, objectMapper);
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

    protected HalRepresentation.HalRepresentationBuilder decoratePagination(HalRepresentation.HalRepresentationBuilder halRepresentationBuilder,
                                                                            ISearchPagination pagination) {
        HalRepresentation.HalRepresentationBuilder builder = addPaginationProperties(halRepresentationBuilder, pagination);
        PaymentSearchNavigationLinks links = (PaymentSearchNavigationLinks) pagination.getLinks();
        try {
            addLink(builder, "self", transformIntoPublicUri(baseUrl, links.getSelf(), PAYMENTS_PATH));
            addLink(builder, "first_page", transformIntoPublicUri(baseUrl, links.getFirstPage(), PAYMENTS_PATH));
            addLink(builder, "last_page", transformIntoPublicUri(baseUrl, links.getLastPage(), PAYMENTS_PATH));
            addLink(builder, "prev_page", transformIntoPublicUri(baseUrl, links.getPrevPage(), PAYMENTS_PATH));
            addLink(builder, "next_page", transformIntoPublicUri(baseUrl, links.getNextPage(), PAYMENTS_PATH));
        } catch (URISyntaxException ex) {
            throw new SearchChargesException(ex);
        }
        return builder;
    }
}
