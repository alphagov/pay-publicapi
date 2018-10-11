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
import uk.gov.pay.api.exception.BadRefundsRequestException;
import uk.gov.pay.api.exception.SearchRefundsException;
import uk.gov.pay.api.model.RefundError;
import uk.gov.pay.api.model.links.SearchRefundsNavigationLinks;
import uk.gov.pay.api.model.search.ISearchPagination;
import uk.gov.pay.api.model.search.SearchBase;
import uk.gov.pay.api.service.ConnectorUriGenerator;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.http.HttpStatus.SC_OK;
import static uk.gov.pay.api.service.PaymentSearchService.DISPLAY_SIZE;
import static uk.gov.pay.api.service.PaymentSearchService.PAGE;

public class SearchRefunds extends SearchBase {

    private static final Logger logger = LoggerFactory.getLogger(SearchRefunds.class);
    private static final String REFUNDS_PATH = "/v1/refunds";

    public SearchRefunds(Client client,
                         PublicApiConfig configuration,
                         ConnectorUriGenerator uriGenerator,
                         ObjectMapper objectMapper) {
        super(client, configuration, uriGenerator, objectMapper);
    }

    public Response getSearchResponse(Account account, Map<String, String> queryParams) {
        validateSupportedSearchParams(queryParams);

        String url = connectorUriGenerator.refundsURIWithParams(account, queryParams);
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

    private HalRepresentation.HalRepresentationBuilder decoratePagination(Long accountId,
                                                                          HalRepresentation.HalRepresentationBuilder halRepresentationBuilder,
                                                                          ISearchPagination pagination) {
        HalRepresentation.HalRepresentationBuilder builder = addPaginationProperties(halRepresentationBuilder, pagination);
        SearchRefundsNavigationLinks links = (SearchRefundsNavigationLinks) pagination.getLinks();
        String path = getRefundsPath(accountId);

        try {
            addLink(builder, "self", transformIntoPublicUri(baseUrl, links.getSelfPage(), path));
        } catch (URISyntaxException ex) {
            throw new SearchRefundsException(ex);
        }
        return builder;
    }

    private String getRefundsPath(Long accountId) {
        return String.format("v1/accounts/%s/refunds", accountId);
    }

    @Override
    protected Set<String> getSupportedSearchParams() {
        return ImmutableSet.of(PAGE, DISPLAY_SIZE);
    }

    protected void validateSupportedSearchParams(Map<String, String> queryParams) {
        queryParams.entrySet().stream()
                .filter(this::isUnsupportedParamWithNonBlankValue)
                .findFirst()
                .ifPresent(invalidParam -> {
                    throw new BadRefundsRequestException(RefundError
                            .aRefundError(RefundError.Code.SEARCH_REFUNDS_VALIDATION_ERROR, invalidParam.getKey()));
                });
    }
}
