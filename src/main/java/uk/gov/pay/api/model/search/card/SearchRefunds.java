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
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.model.links.Link;
import uk.gov.pay.api.service.ConnectorUriGenerator;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_OK;

public class SearchRefunds {

    private static final Logger logger = LoggerFactory.getLogger(SearchRefunds.class);
    protected final ConnectorUriGenerator uriGenerator;
    protected final Client client;
    protected final ObjectMapper objectMapper;
    private static final String REFUNDS_PATH = "/v1/refunds";
    protected final String baseUrl;


    public SearchRefunds(Client client,
                         PublicApiConfig configuration,
                         ConnectorUriGenerator uriGenerator,
                         ObjectMapper objectMapper) {

        this.client = client;
        this.objectMapper = objectMapper;
        this.uriGenerator = uriGenerator;
        this.baseUrl = configuration.getBaseUrl();
    }

    public Response getSearchResponse(Account account, Map<String, String> queryParams) {
        if (account.getPaymentType().equals(TokenPaymentType.DIRECT_DEBIT)) {
            throw new BadRequestException(PaymentError
                    .aPaymentError(PaymentError.Code.SEARCH_PAYMENTS_VALIDATION_ERROR, "agreement_id"));
        }

        String url = uriGenerator.refundsURIWithParams(account, queryParams);
        Response connectorResponse = client
                .target(url)
                .request()
                .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                .get();
        logger.info("response from connector for refunds search: " + connectorResponse);
        if (connectorResponse.getStatus() == SC_OK) {
            return processResponse(account.getAccountId(), connectorResponse);
        }
        throw new SearchRefundsException(connectorResponse);
    }

    private Response processResponse(String accountId, Response connectorResponse) {
        try {
            JsonNode responseJson = connectorResponse.readEntity(JsonNode.class);
            TypeReference<SearchRefundsResponse> typeRef = new TypeReference<SearchRefundsResponse>() {
            };
            SearchRefundsResponse searchResponse = objectMapper.readValue(responseJson.traverse(), typeRef);
            List<RefundForSearchRefundsResult> results = searchResponse.getRefunds()
                    .stream()
                    .map(refund -> RefundForSearchRefundsResult.valueOf(refund))
                    .collect(Collectors.toList());

            HalRepresentation.HalRepresentationBuilder halRepresentation = HalRepresentation
                    .builder()
                    .addProperty("results", results);
            return Response.ok().entity(decoratePagination(Long.valueOf(accountId), halRepresentation, searchResponse)
                    .build().toString())
                    .build();
        } catch (IOException | ProcessingException ex) {
            throw new SearchRefundsException(ex);
        }
    }

    protected HalRepresentation.HalRepresentationBuilder decoratePagination(Long accountId,
                                                                            HalRepresentation.HalRepresentationBuilder halRepresentationBuilder,
                                                                            SearchRefundsResponse pagination) {
        try {
            halRepresentationBuilder
                    .addProperty("count", pagination.getCount())
                    .addProperty("total", pagination.getTotal())
                    .addProperty("page", pagination.getPage());
            addLink(halRepresentationBuilder, "self", transformIntoPublicUri(
                    baseUrl,
                    pagination.getLinks().getSelfPage(),
                    accountId
            ));
        } catch (URISyntaxException ex) {
            throw new SearchRefundsException(ex);
        }
        return halRepresentationBuilder;
    }

    private void addLink(HalRepresentation.HalRepresentationBuilder halRepresentationBuilder, String name, URI uri) {
        if (uri != null) {
            halRepresentationBuilder.addLink(name, uri);
        }
    }

    private URI transformIntoPublicUri(String baseUrl, Link link, Long accountId) throws URISyntaxException {
        if (link == null)
            return null;
        String targetPath = REFUNDS_PATH + "/account/{accountId}"
                .replace("{accountId}", String.valueOf(accountId));

        return UriBuilder.fromUri(baseUrl)
                .path(targetPath)
                .replaceQuery(new URI(link.getHref()).getQuery())
                .build();
    }
}
