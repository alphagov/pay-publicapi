package uk.gov.pay.api.model.search;

import black.door.hate.HalRepresentation;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.exception.SearchException;
import uk.gov.pay.api.model.links.Link;
import uk.gov.pay.api.model.links.SearchNavigationLinks;
import uk.gov.pay.api.service.ConnectorUriGenerator;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class SearchBase {

    protected final ConnectorUriGenerator connectorUriGenerator;
    protected final Client client;
    protected final ObjectMapper objectMapper;
    protected final String baseUrl;

    public SearchBase(Client client,
                      PublicApiConfig configuration,
                      ConnectorUriGenerator connectorUriGenerator,
                      ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.connectorUriGenerator = connectorUriGenerator;
        this.baseUrl = configuration.getBaseUrl();
    }

    protected abstract Set<String> getSupportedSearchParams();

    protected boolean isUnsupportedParamWithNonBlankValue(Map.Entry<String, String> queryParam) {
        return !getSupportedSearchParams().contains(queryParam.getKey()) && isNotBlank(queryParam.getValue());
    }

    private HalRepresentation.HalRepresentationBuilder addPaginationProperties(HalRepresentation.HalRepresentationBuilder halRepresentationBuilder,
                                                                                 ISearchPagination pagination) {
        halRepresentationBuilder
                .addProperty("count", pagination.getCount())
                .addProperty("total", pagination.getTotal())
                .addProperty("page", pagination.getPage());
        return halRepresentationBuilder;
    }

    protected void addLink(HalRepresentation.HalRepresentationBuilder halRepresentationBuilder, String name, URI uri) {
        if (uri != null) {
            halRepresentationBuilder.addLink(name, uri);
        }
    }

    protected URI transformIntoPublicUri(String baseUrl, Link link, String PATH) throws URISyntaxException {
        if (link == null)
            return null;

        return UriBuilder.fromUri(baseUrl)
                .path(PATH)
                .replaceQuery(new URI(link.getHref()).getQuery())
                .build();
    }
    protected HalRepresentation.HalRepresentationBuilder decoratePagination(HalRepresentation.HalRepresentationBuilder halRepresentationBuilder,
                                                                            ISearchPagination pagination, String path) {
        
        HalRepresentation.HalRepresentationBuilder builder = addPaginationProperties(halRepresentationBuilder, pagination);
        SearchNavigationLinks links = (SearchNavigationLinks) pagination.getLinks();
        try {
            addLink(builder, "self", transformIntoPublicUri(baseUrl, links.getSelf(), path));
            addLink(builder, "first_page", transformIntoPublicUri(baseUrl, links.getFirstPage(), path));
            addLink(builder, "last_page", transformIntoPublicUri(baseUrl, links.getLastPage(), path));
            addLink(builder, "prev_page", transformIntoPublicUri(baseUrl, links.getPrevPage(), path));
            addLink(builder, "next_page", transformIntoPublicUri(baseUrl, links.getNextPage(), path));
        } catch (URISyntaxException ex) {
            throw new SearchException(ex);
        }
        return builder;
    }
}
