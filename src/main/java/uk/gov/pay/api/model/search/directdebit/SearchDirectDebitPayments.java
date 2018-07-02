package uk.gov.pay.api.model.search.directdebit;

import black.door.hate.HalRepresentation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_OK;

public class SearchDirectDebitPayments extends SearchPaymentsBase {

    private static final Logger logger = LoggerFactory.getLogger(SearchDirectDebitPayments.class);
    
    public SearchDirectDebitPayments(Client client,
                                     PublicApiConfig configuration,
                                     ConnectorUriGenerator connectorUriGenerator,
                                     PaymentUriGenerator paymentUriGenerator,
                                     ObjectMapper objectMapper) {
        super(client, configuration, connectorUriGenerator, paymentUriGenerator, objectMapper);
    }

    @Override
    public Response getSearchResponse(Account account, List<Pair<String, String>> queryParams) {
        if (queryParams.stream()
                .anyMatch(queryParam -> "card_brand".equals(queryParam.getLeft()) && queryParam.getRight() != null)) {
            throw new BadRequestException(PaymentError
                    .aPaymentError(PaymentError.Code.SEARCH_PAYMENTS_VALIDATION_ERROR, "card_brand"));
        }
        String url = connectorUriGenerator.directDebitTransactionsURI(account, queryParams);
        Response connectorResponse = client
                .target(url)
                .request()
                .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                .get();
        logger.info("response from dd connector for transaction search: " + connectorResponse);
        if (connectorResponse.getStatus() == SC_OK) {
            return processResponse(connectorResponse);
        }
        throw new SearchChargesException(connectorResponse);
    }

    private Response processResponse(Response directDebitResponse) {
        try {
            JsonNode responseJson = directDebitResponse.readEntity(JsonNode.class);
            TypeReference<DDSearchResponse> typeRef = new TypeReference<DDSearchResponse>() {};
            DDSearchResponse searchResponse = objectMapper.readValue(responseJson.traverse(), typeRef);
            List<DDTransactionForSearch> transactionFromResponse =
                    searchResponse
                            .getPayments()
                            .stream()
                            .map(transaction -> DDTransactionForSearch.valueOf(
                                    transaction,
                                    paymentUriGenerator.getPaymentURI(baseUrl, transaction.getTransactionId())
                            )).collect(Collectors.toList());
            HalRepresentation.HalRepresentationBuilder halRepresentation = HalRepresentation.builder()
                    .addProperty("results", transactionFromResponse);
            return Response.ok().entity(decoratePagination(halRepresentation, searchResponse).build().toString()).build();
        } catch (IOException | ProcessingException ex) {
            throw new SearchChargesException(ex);
        }
    }
}
