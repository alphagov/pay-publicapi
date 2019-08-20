package uk.gov.pay.api.service;

import black.door.hate.HalRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.SearchPaymentsException;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.search.PaginationDecorator;
import uk.gov.pay.api.model.search.card.PaymentForSearchResult;
import uk.gov.pay.api.model.search.card.PaymentSearchResponse;

import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.pay.api.validation.PaymentSearchValidator.validateSearchParameters;

public class PaymentSearchService {

    private static final String PAYMENTS_PATH = "/v1/payments";
    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentSearchService.class);

    private final PublicApiUriGenerator publicApiUriGenerator;
    private final PaginationDecorator paginationDecorator;
    private ConnectorService connectorService;

    @Inject
    public PaymentSearchService(PublicApiUriGenerator publicApiUriGenerator,
                                PaginationDecorator paginationDecorator,
                                ConnectorService connectorService) {
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.paginationDecorator = paginationDecorator;
        this.connectorService = connectorService;
    }

    public Response doSearch(Account account, PaymentSearchParams searchParams) {
        validateSearchParameters(account, searchParams);

        Map<String, String> queryParams = searchParams.getParamsAsMap();

        return getSearchResponse(account, queryParams);
    }

    private Response getSearchResponse(Account account, Map<String, String> queryParams) {
        queryParams.put("transactionType", "charge");

        PaymentSearchResponse<ChargeFromResponse> paymentSearchResponse =
                connectorService.searchPayments(account, queryParams);

        return processResponse(paymentSearchResponse);
    }

    private Response processResponse(PaymentSearchResponse<ChargeFromResponse> response) {
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
