package uk.gov.pay.api.service.directdebit;

import org.apache.http.HttpStatus;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.SearchMandatesException;
import uk.gov.pay.api.model.directdebit.mandates.MandateResponse;
import uk.gov.pay.api.model.links.SearchNavigationLinks;
import uk.gov.pay.api.model.search.directdebit.DirectDebitSearchMandatesParams;
import uk.gov.pay.api.model.search.directdebit.SearchMandateConnectorResponse;
import uk.gov.pay.api.model.search.directdebit.SearchMandateResponse;
import uk.gov.pay.api.service.PublicApiUriGenerator;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.pay.api.model.search.directdebit.SearchMandateResponse.SearchMandateResponseBuilder.aSearchMandateResponse;

public class DirectDebitMandateSearchService {

    private final Client client;
    private final DirectDebitConnectorUriGenerator directDebitConnectorUriGenerator;
    private final PublicApiUriGenerator publicApiUriGenerator;
    
    
    @Inject
    public DirectDebitMandateSearchService(Client client, DirectDebitConnectorUriGenerator directDebitConnectorUriGenerator, PublicApiUriGenerator publicApiUriGenerator) {
        this.client = client;
        this.directDebitConnectorUriGenerator = directDebitConnectorUriGenerator;
        this.publicApiUriGenerator = publicApiUriGenerator;
    }
    
    public SearchMandateResponse search(Account account, DirectDebitSearchMandatesParams params) {
        SearchMandateConnectorResponse connectorResponse = getMandatesFromDDConnector(account, params);

        var mandateResponse = connectorResponse.getMandates().stream()
                .map(connMandate -> new MandateResponse(connMandate, publicApiUriGenerator))
                .collect(Collectors.toUnmodifiableList());

        return aSearchMandateResponse()
                .withCount(connectorResponse.getCount())
                .withTotal(connectorResponse.getTotal())
                .withPage(connectorResponse.getPage())
                .withLinks(convertSearchLinksToPublicApiHost(connectorResponse.getLinks()))
                .withMandates(mandateResponse)
                .build();
    }
    
    SearchMandateConnectorResponse getMandatesFromDDConnector(Account account, DirectDebitSearchMandatesParams params) {
        WebTarget webTargetWithoutQuery = client.target(directDebitConnectorUriGenerator.mandatesURI(account));
        WebTarget webTargetWithQuery = addQueryParams(params.paramsAsMap(), webTargetWithoutQuery);

        Response response = webTargetWithQuery
                .request()
                .accept(APPLICATION_JSON)
                .get();

        if (response.getStatus() != HttpStatus.SC_OK) {
            throw new SearchMandatesException(response);
        }

        return response.readEntity(SearchMandateConnectorResponse.class);
    }

    private WebTarget addQueryParams(Map<String, String> params, WebTarget originalWebTarget) {
        WebTarget webTargetWithQueryParams = originalWebTarget;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            webTargetWithQueryParams = webTargetWithQueryParams.queryParam(entry.getKey(), entry.getValue());
        }
        return webTargetWithQueryParams;
    }
    
    private SearchNavigationLinks convertSearchLinksToPublicApiHost(SearchNavigationLinks originalLinks) {
        SearchNavigationLinks updatedLinks = new SearchNavigationLinks();
        if (originalLinks.getFirstPage() != null) {
            updatedLinks.withFirstLink(createPubliApiMandateSearchLinkWithQueryFrom(originalLinks.getFirstPage().getHref()));
        }

        if(originalLinks.getLastPage() != null) {
            updatedLinks.withLastLink(createPubliApiMandateSearchLinkWithQueryFrom(originalLinks.getLastPage().getHref()));
        }

        if(originalLinks.getSelf() != null) {
            updatedLinks.withSelfLink(createPubliApiMandateSearchLinkWithQueryFrom(originalLinks.getSelf().getHref()));
        }

        if(originalLinks.getNextPage() != null) {
            updatedLinks.withNextLink(createPubliApiMandateSearchLinkWithQueryFrom(originalLinks.getNextPage().getHref()));
        }

        if(originalLinks.getPrevPage() != null) {
            updatedLinks.withPrevLink(createPubliApiMandateSearchLinkWithQueryFrom(originalLinks.getPrevPage().getHref()));
        }

        return updatedLinks;
    }

    private String createPubliApiMandateSearchLinkWithQueryFrom(String ddConnectorUrl) {
        String query = UriBuilder.fromUri(ddConnectorUrl).build().getQuery();
        return publicApiUriGenerator.getSearchMandatesURIWithQueryOf(query).toString();
    }
}
