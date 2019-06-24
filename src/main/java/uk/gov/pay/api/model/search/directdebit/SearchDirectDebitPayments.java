package uk.gov.pay.api.model.search.directdebit;

import black.door.hate.HalRepresentation;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.SearchPaymentsException;
import uk.gov.pay.api.model.search.SearchPaymentsBase;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.PaymentUriGenerator;
import uk.gov.pay.api.service.directdebit.DirectDebitConnectorUriGenerator;

import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.pay.api.service.PaymentSearchService.DISPLAY_SIZE;
import static uk.gov.pay.api.service.PaymentSearchService.EMAIL_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.FROM_DATE_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.PAGE;
import static uk.gov.pay.api.service.PaymentSearchService.REFERENCE_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.STATE_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.TO_DATE_KEY;
import static uk.gov.pay.api.service.directdebit.DirectDebitPaymentSearchService.MANDATE_ID_KEY;

public class SearchDirectDebitPayments extends SearchPaymentsBase {

    private static final String PAYMENT_PATH = "v1/payments";
    private static final Logger logger = LoggerFactory.getLogger(SearchDirectDebitPayments.class);

    private DirectDebitConnectorUriGenerator directDebitConnectorUriGenerator;

    @Inject
    public SearchDirectDebitPayments(Client client,
                                     PublicApiConfig configuration,
                                     ConnectorUriGenerator connectorUriGenerator,
                                     DirectDebitConnectorUriGenerator directDebitConnectorUriGenerator,
                                     PaymentUriGenerator paymentUriGenerator) {
        super(client, configuration, connectorUriGenerator, paymentUriGenerator);
        this.directDebitConnectorUriGenerator = directDebitConnectorUriGenerator;
    }
    
    @Override
    public Response getSearchResponse(Account account, Map<String, String> queryParams) {
        validateSupportedSearchParams(queryParams);
        String url = directDebitConnectorUriGenerator.directDebitPaymentsURI(account, queryParams);
        Response connectorResponse = client
                .target(url)
                .request()
                .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                .get();
        logger.info("response from dd connector for payment search: {}", connectorResponse);
        if (connectorResponse.getStatus() == SC_OK) {
            return processResponse(connectorResponse);
        }
        throw new SearchPaymentsException(connectorResponse);
    }

    @Override
    protected Set<String> getSupportedSearchParams() {
        return ImmutableSet.of(REFERENCE_KEY, EMAIL_KEY, STATE_KEY, MANDATE_ID_KEY, FROM_DATE_KEY, TO_DATE_KEY, PAGE, DISPLAY_SIZE);
    }

    private Response processResponse(Response directDebitResponse) {
        try {
            DirectDebitSearchResponse searchResponse = directDebitResponse.readEntity(DirectDebitSearchResponse.class);
            List<DirectDebitPaymentForSearch> paymentFromResponse =
                    searchResponse
                            .getPayments()
                            .stream()
                            .map(payment -> DirectDebitPaymentForSearch.valueOf(
                                    payment,
                                    paymentUriGenerator.getPaymentURI(baseUrl, payment.getPaymentId())
                            )).collect(Collectors.toList());
            HalRepresentation.HalRepresentationBuilder halRepresentation = HalRepresentation.builder()
                    .addProperty("results", paymentFromResponse);
            return Response.ok().entity(decoratePagination(halRepresentation, searchResponse, PAYMENT_PATH).build().toString()).build();
        } catch (ProcessingException ex) {
            throw new SearchPaymentsException(ex);
        }
    }
}
