package uk.gov.pay.api.model.search;

import black.door.hate.HalRepresentation;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.exception.SearchPaymentsException;
import uk.gov.pay.api.model.links.Link;
import uk.gov.pay.api.model.links.SearchNavigationLinks;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;

public class PaginationDecorator {

    private final String baseUrl;
    
    @Inject
    public PaginationDecorator(PublicApiConfig config) {
        baseUrl = config.getBaseUrl();
    }
    
    public HalRepresentation.HalRepresentationBuilder decoratePagination(HalRepresentation.HalRepresentationBuilder halRepresentationBuilder,
                                                                            SearchPagination pagination, String path) {

        HalRepresentation.HalRepresentationBuilder builder = addPaginationProperties(halRepresentationBuilder, pagination);
        SearchNavigationLinks links = pagination.getLinks();
        try {
            addLink(builder, "self", transformIntoPublicUri(baseUrl, links.getSelf(), path));
            addLink(builder, "first_page", transformIntoPublicUri(baseUrl, links.getFirstPage(), path));
            addLink(builder, "last_page", transformIntoPublicUri(baseUrl, links.getLastPage(), path));
            addLink(builder, "prev_page", transformIntoPublicUri(baseUrl, links.getPrevPage(), path));
            addLink(builder, "next_page", transformIntoPublicUri(baseUrl, links.getNextPage(), path));
        } catch (URISyntaxException ex) {
            throw new SearchPaymentsException(ex);
        }
        return builder;
    }

    private HalRepresentation.HalRepresentationBuilder addPaginationProperties(HalRepresentation.HalRepresentationBuilder halRepresentationBuilder,
                                                                               SearchPagination pagination) {
        halRepresentationBuilder
                .addProperty("count", pagination.getCount())
                .addProperty("total", pagination.getTotal())
                .addProperty("page", pagination.getPage());
        return halRepresentationBuilder;
    }

    private void addLink(HalRepresentation.HalRepresentationBuilder halRepresentationBuilder, String name, URI uri) {
        if (uri != null) {
            halRepresentationBuilder.addLink(name, uri);
        }
    }

    private URI transformIntoPublicUri(String baseUrl, Link link, String path) throws URISyntaxException {
        if (link == null)
            return null;

        return UriBuilder.fromUri(baseUrl)
                .path(path)
                .replaceQuery(new URI(link.getHref()).getQuery())
                .build();
    }
}
