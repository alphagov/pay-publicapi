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
import uk.gov.pay.api.exception.SearchRefundsException;
import uk.gov.pay.api.model.PaymentError;
import uk.gov.pay.api.model.search.SearchRefundsBase;
import uk.gov.pay.api.service.RefundsUriGenerator;

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

public class SearchCardRefunds extends SearchRefundsBase {

    private static final Logger logger = LoggerFactory.getLogger(SearchCardRefunds.class);

    public SearchCardRefunds(Client client,
                             PublicApiConfig configuration,
                             RefundsUriGenerator refundsUriGenerator,
                             ObjectMapper objectMapper) {
        super(client, configuration, refundsUriGenerator, objectMapper);
    }

    @Override
    public Response getSearchResponse(Account account, Map<String, String> queryParams) {
        if (isNotEmpty(queryParams.get("agreement_id"))) {
            throw new BadRequestException(PaymentError
                    .aPaymentError(PaymentError.Code.SEARCH_PAYMENTS_VALIDATION_ERROR, "agreement_id"));
        }
        
        String url = refundsUriGenerator.refundsURIWithParams(account, queryParams);
        Response connectorResponse = client
                .target(url)
                .request()
                .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                .get();
        logger.info("response from connector for refunds search: " + connectorResponse);
        if (connectorResponse.getStatus() == SC_OK) {
            return processResponse(connectorResponse);
        }
        throw new SearchRefundsException(connectorResponse); 
    }

    private Response processResponse(Response connectorResponse) {
        try { 
            JsonNode responseJson = connectorResponse.readEntity(JsonNode.class);
            TypeReference<SearchRefundsResponse> typeRef = new TypeReference<SearchRefundsResponse>() {};
            SearchRefundsResponse searchResponse = objectMapper.readValue(responseJson.traverse(), typeRef);
            List<RefundsForSearchRefundsResult> refundsFromResponses = searchResponse.getRefunds()
                    .stream()
                    .map(RefundsForSearchRefundsResult::valueOf)
                    .collect(Collectors.toList());
            
            HalRepresentation.HalRepresentationBuilder halRepresentation = HalRepresentation
                    .builder()
                    .addProperty("results", refundsFromResponses);
            return Response.ok().entity(decoratePagination(halRepresentation, searchResponse).build().toString()).build(); 
        } catch (IOException | ProcessingException ex) { 
            throw new SearchRefundsException(ex); 
        } 
    }
}
