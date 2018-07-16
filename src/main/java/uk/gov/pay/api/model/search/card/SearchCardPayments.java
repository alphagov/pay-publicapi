package uk.gov.pay.api.model.search.card;

import black.door.hate.HalRepresentation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.BadRequestException;
import uk.gov.pay.api.exception.SearchChargesException;
import uk.gov.pay.api.model.PaymentError;
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
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.http.HttpStatus.SC_OK;

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
        if (isNotEmpty(queryParams.get("agreement_id"))) {
            throw new BadRequestException(PaymentError
                    .aPaymentError(PaymentError.Code.SEARCH_PAYMENTS_VALIDATION_ERROR, "agreement_id"));
        }
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
