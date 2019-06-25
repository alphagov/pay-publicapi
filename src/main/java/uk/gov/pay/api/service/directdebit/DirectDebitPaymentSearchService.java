package uk.gov.pay.api.service.directdebit;

import black.door.hate.HalRepresentation;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.SearchPaymentsException;
import uk.gov.pay.api.model.directdebit.mandates.DirectDebitPayment;
import uk.gov.pay.api.model.search.PaginationDecorator;
import uk.gov.pay.api.model.search.directdebit.DirectDebitSearchPaymentsParams;
import uk.gov.pay.api.model.search.directdebit.DirectDebitSearchResponse;
import uk.gov.pay.api.service.PublicApiUriGenerator;

import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_OK;

public class DirectDebitPaymentSearchService {

    private static final String PAYMENT_PATH = "v1/directdebit/payments";

    private static final String REFERENCE_KEY = "reference";
    private static final String STATE_KEY = "state";
    private static final String MANDATE_ID_KEY = "mandate_id";
    private static final String FROM_DATE_KEY = "from_date";
    private static final String TO_DATE_KEY = "to_date";
    private static final String PAGE = "page";
    private static final String DISPLAY_SIZE = "display_size";

    private final DirectDebitConnectorUriGenerator directDebitConnectorUriGenerator;
    private final Client client;
    private final PublicApiUriGenerator publicApiUriGenerator;
    private final PaginationDecorator paginationDecorator;

    @Inject
    public DirectDebitPaymentSearchService(Client client,
                                           DirectDebitConnectorUriGenerator directDebitConnectorUriGenerator,
                                           PublicApiUriGenerator publicApiUriGenerator,
                                           PaginationDecorator paginationDecorator) {
        this.client = client;
        this.paginationDecorator = paginationDecorator;
        this.directDebitConnectorUriGenerator = directDebitConnectorUriGenerator;
        this.publicApiUriGenerator = publicApiUriGenerator;
    }
    
    public Response doSearch(Account account, DirectDebitSearchPaymentsParams searchPaymentsParams) {
        Map<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put(REFERENCE_KEY, searchPaymentsParams.getReference());
        queryParams.put(STATE_KEY, searchPaymentsParams.getState());
        queryParams.put(MANDATE_ID_KEY, searchPaymentsParams.getMandateId());
        queryParams.put(FROM_DATE_KEY, searchPaymentsParams.getFromDate());
        queryParams.put(TO_DATE_KEY, searchPaymentsParams.getToDate());
        
        searchPaymentsParams.getPage()
                .ifPresent(pageNumber -> queryParams.put(PAGE, String.valueOf(pageNumber)));
        searchPaymentsParams.getDisplaySize()
                .ifPresent(displaySize -> queryParams.put(DISPLAY_SIZE, String.valueOf(displaySize)));

        return getSearchResponse(account, queryParams);
    }

    private Response getSearchResponse(Account account, Map<String, String> queryParams) {
        String url = directDebitConnectorUriGenerator.directDebitPaymentsURI(account, queryParams);
        Response connectorResponse = client
                .target(url)
                .request()
                .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                .get();

        if (connectorResponse.getStatus() == SC_OK) {
            return processResponse(connectorResponse);
        }
        throw new SearchPaymentsException(connectorResponse);
    }

    private Response processResponse(Response directDebitResponse) {
        DirectDebitSearchResponse response;
        try {
            response = directDebitResponse.readEntity(DirectDebitSearchResponse.class);
        } catch (ProcessingException ex) {
            throw new SearchPaymentsException(ex);
        }

        List<DirectDebitPayment> paymentFromResponse =
                response.getPayments()
                        .stream()
                        .map(payment -> DirectDebitPayment.from(payment, publicApiUriGenerator))
                        .collect(Collectors.toList());
        HalRepresentation.HalRepresentationBuilder halRepresentation = HalRepresentation.builder()
                .addProperty("results", paymentFromResponse);
        return Response.ok().entity(paginationDecorator.decoratePagination(halRepresentation, response, PAYMENT_PATH).build().toString()).build();
    }
}
