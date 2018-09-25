package uk.gov.pay.api.model.search.card;

import black.door.hate.HalRepresentation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.SearchChargesException;
import uk.gov.pay.api.model.search.SearchPaymentsBase;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.PaymentUriGenerator;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.pay.api.service.PaymentSearchService.CARDHOLDER_NAME_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.CARD_BRAND_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.DISPLAY_SIZE;
import static uk.gov.pay.api.service.PaymentSearchService.EMAIL_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.FIRST_DIGITS_CARD_NUMBER_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.FROM_DATE_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.LAST_DIGITS_CARD_NUMBER_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.PAGE;
import static uk.gov.pay.api.service.PaymentSearchService.REFERENCE_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.STATE_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.TO_DATE_KEY;

public class SearchCardPayments extends SearchPaymentsBase {

    private static final Logger logger = LoggerFactory.getLogger(SearchCardPayments.class);

    public SearchCardPayments(Client client,
                              PublicApiConfig configuration,
                              ConnectorUriGenerator connectorUriGenerator,
                              PaymentUriGenerator paymentUriGenerator,
                              ObjectMapper objectMapper) {
        super(client, configuration, connectorUriGenerator, paymentUriGenerator, objectMapper);
    }

    @Override
    public Response getSearchResponse(Account account, Map<String, String> queryParams) {
        validateAllowedSearchFields(queryParams);
        queryParams.put("transactionType", "charge");

        String url = connectorUriGenerator.chargesURIWithParams(account, queryParams);
        Response connectorResponse = client
                .target(url)
                .request()
                .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                .get();
        logger.info("response from connector for transaction search: " + connectorResponse);
        if (connectorResponse.getStatus() == SC_OK) {
            return processResponse(connectorResponse);
        }
        throw new SearchChargesException(connectorResponse);
    }

    @Override
    protected Set<String> getSupportedSearchParams() {
        return ImmutableSet.of(REFERENCE_KEY, EMAIL_KEY, STATE_KEY, CARD_BRAND_KEY, CARDHOLDER_NAME_KEY, FIRST_DIGITS_CARD_NUMBER_KEY, LAST_DIGITS_CARD_NUMBER_KEY, FROM_DATE_KEY, TO_DATE_KEY, PAGE, DISPLAY_SIZE);

    }

    private Response processResponse(Response connectorResponse) {
        try {
            JsonNode responseJson = connectorResponse.readEntity(JsonNode.class);
            TypeReference<PaymentSearchResponse> typeRef = new TypeReference<PaymentSearchResponse>() {
            };
            PaymentSearchResponse searchResponse = objectMapper.readValue(responseJson.traverse(), typeRef);
            List<PaymentForSearchResult> chargeFromResponses = searchResponse.getPayments()
                    .stream()
                    .map(charge -> PaymentForSearchResult.valueOf(
                            charge,
                            paymentUriGenerator.getPaymentURI(baseUrl, charge.getChargeId()),
                            paymentUriGenerator.getPaymentEventsURI(baseUrl, charge.getChargeId()),
                            paymentUriGenerator.getPaymentCancelURI(baseUrl, charge.getChargeId()),
                            paymentUriGenerator.getPaymentRefundsURI(baseUrl, charge.getChargeId())))
                    .collect(Collectors.toList());
            HalRepresentation.HalRepresentationBuilder halRepresentation = HalRepresentation
                    .builder()
                    .addProperty("results", chargeFromResponses);

            return Response.ok().entity(decoratePagination(halRepresentation, searchResponse).build().toString()).build();
        } catch (IOException | ProcessingException ex) {
            throw new SearchChargesException(ex);
        }
    }
}
