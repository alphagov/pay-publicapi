package uk.gov.pay.api.service;

import black.door.hate.HalRepresentation;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.ChargeFromResponse;
import uk.gov.pay.api.model.TransactionResponse;
import uk.gov.pay.api.model.search.PaginationDecorator;
import uk.gov.pay.api.model.search.card.PaymentForSearchResult;
import uk.gov.pay.api.model.search.card.PaymentSearchResponse;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static uk.gov.pay.api.validation.PaymentSearchValidator.validateSearchParameters;

public class PaymentSearchService {

    private static final String PAYMENTS_PATH = "/v1/payments";

    private final PublicApiUriGenerator publicApiUriGenerator;
    private final PaginationDecorator paginationDecorator;
    private ConnectorService connectorService;
    private LedgerService ledgerService;

    @Inject
    public PaymentSearchService(PublicApiUriGenerator publicApiUriGenerator,
                                PaginationDecorator paginationDecorator,
                                ConnectorService connectorService,
                                LedgerService ledgerService) {
        this.publicApiUriGenerator = publicApiUriGenerator;
        this.paginationDecorator = paginationDecorator;
        this.connectorService = connectorService;
        this.ledgerService = ledgerService;
    }

    public Response searchConnectorPayments(Account account, PaymentSearchParams searchParams) {
        validateSearchParameters(account, searchParams);
        Map<String, String> queryParams = searchParams.getParamsAsMap();
        queryParams.put("transactionType", "charge");

        PaymentSearchResponse<ChargeFromResponse> paymentSearchResponse =
                connectorService.searchPayments(account, queryParams);

        return processResponse(paymentSearchResponse);
    }

    public Response searchLedgerPayments(Account account, PaymentSearchParams searchParams) {
        validateSearchParameters(account, searchParams);

        PaymentSearchResponse<TransactionResponse> paymentSearchResponse =
                ledgerService.searchPayments(account, searchParams.getParamsAsMap());
        return processLedgerResponse(paymentSearchResponse);
    }

    private Response processLedgerResponse(PaymentSearchResponse<TransactionResponse> paymentSearchResponse) {
        List<PaymentForSearchResult> chargeFromResponses = paymentSearchResponse.getPayments()
                .stream()
                .map(t -> PaymentForSearchResult.valueOf(
                        t,
                        publicApiUriGenerator.getPaymentURI(t.getTransactionId()),
                        publicApiUriGenerator.getPaymentEventsURI(t.getTransactionId()),
                        publicApiUriGenerator.getPaymentCancelURI(t.getTransactionId()),
                        publicApiUriGenerator.getPaymentRefundsURI(t.getTransactionId()),
                        publicApiUriGenerator.getPaymentCaptureURI(t.getTransactionId())))
                .collect(toList());

        HalRepresentation.HalRepresentationBuilder halRepresentation = HalRepresentation
                .builder()
                .addProperty("results", chargeFromResponses);

        return Response.ok()
                .entity(paginationDecorator
                        .decoratePagination(halRepresentation, paymentSearchResponse, PAYMENTS_PATH)
                        .build()
                        .toString())
                .build();
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
                .collect(toList());
        HalRepresentation.HalRepresentationBuilder halRepresentation = HalRepresentation
                .builder()
                .addProperty("results", chargeFromResponses);

        return Response.ok().entity(paginationDecorator.decoratePagination(halRepresentation, response, PAYMENTS_PATH).build().toString()).build();
    }
}
