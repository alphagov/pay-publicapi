package uk.gov.pay.api.model.search.card;

import black.door.hate.HalRepresentation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.BadRefundsRequestException;
import uk.gov.pay.api.exception.SearchRefundsException;
import uk.gov.pay.api.model.RefundError;
import uk.gov.pay.api.model.search.SearchBase;
import uk.gov.pay.api.service.ConnectorUriGenerator;
import uk.gov.pay.api.service.PublicApiUriGenerator;

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
import static uk.gov.pay.api.service.PaymentSearchService.DISPLAY_SIZE;
import static uk.gov.pay.api.service.PaymentSearchService.FROM_DATE_KEY;
import static uk.gov.pay.api.service.PaymentSearchService.PAGE;
import static uk.gov.pay.api.service.PaymentSearchService.TO_DATE_KEY;

public class SearchRefunds extends SearchBase {

    private static final Logger logger = LoggerFactory.getLogger(SearchRefunds.class);
    private static final String REFUNDS_PATH = "/v1/refunds";
    private final PublicApiUriGenerator publicApiUriGenerator;

    public SearchRefunds(Client client,
                         PublicApiConfig configuration,
                         ConnectorUriGenerator uriGenerator,
                         ObjectMapper objectMapper, PublicApiUriGenerator publicApiUriGenerator) {
        super(client, configuration, uriGenerator, objectMapper);
        this.publicApiUriGenerator = publicApiUriGenerator;
    }

    public Response getSearchResponse(Account account, Map<String, String> queryParams) {
        validateSupportedSearchParams(queryParams);

        String url = connectorUriGenerator.refundsURIWithParams(account, queryParams);
        Response connectorResponse = client
                .target(url)
                .request()
                .header(HttpHeaders.ACCEPT, APPLICATION_JSON)
                .get();
        logger.info("response from connector for refunds search: {}", connectorResponse);
        if (connectorResponse.getStatus() == SC_OK) {
            return processResponse(connectorResponse);
        }
        throw new SearchRefundsException(connectorResponse);
    }

    private Response processResponse(Response connectorResponse) {
        try {
            String response = connectorResponse.readEntity(String.class);
            SearchRefundsResponse searchResponse = objectMapper.readValue(response, SearchRefundsResponse.class);
            List<RefundForSearchRefundsResult> results = searchResponse.getRefunds()
                    .stream()
                    .map(refund -> RefundForSearchRefundsResult.valueOf(refund,
                            publicApiUriGenerator.getPaymentURI(refund.getChargeId()),
                            publicApiUriGenerator.getRefundsURI(refund.getChargeId(), refund.getRefundId())))
                    .collect(Collectors.toList());

            HalRepresentation.HalRepresentationBuilder halRepresentation = HalRepresentation
                    .builder()
                    .addProperty("results", results);
            return Response.ok().entity(
                    decoratePagination(halRepresentation, searchResponse, REFUNDS_PATH).build().toString())
                    .build();
        } catch (IOException | ProcessingException ex) {
            throw new SearchRefundsException(ex);
        }
    }

    @Override
    protected Set<String> getSupportedSearchParams() {
        return ImmutableSet.of(FROM_DATE_KEY, TO_DATE_KEY, PAGE, DISPLAY_SIZE);
    }

    private void validateSupportedSearchParams(Map<String, String> queryParams) {
        queryParams.entrySet().stream()
                .filter(this::isUnsupportedParamWithNonBlankValue)
                .findFirst()
                .ifPresent(invalidParam -> {
                    throw new BadRefundsRequestException(RefundError
                            .aRefundError(RefundError.Code.SEARCH_REFUNDS_VALIDATION_ERROR, invalidParam.getKey()));
                });
    }
}
