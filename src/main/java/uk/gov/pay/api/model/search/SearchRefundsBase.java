package uk.gov.pay.api.model.search;

import black.door.hate.HalRepresentation;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.exception.SearchRefundsException;
import uk.gov.pay.api.model.links.Link;
import uk.gov.pay.api.service.RefundsUriGenerator;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public abstract class SearchRefundsBase {

    private static final String REFUNDS_PATH = "/v1/refunds";
    protected final Client client;
    protected final PublicApiConfig configuration;
    protected final RefundsUriGenerator refundsUriGenerator;
    protected final ObjectMapper objectMapper;
    protected final String baseUrl;

    public SearchRefundsBase(Client client,
                             PublicApiConfig configuration,
                             RefundsUriGenerator refundsUriGenerator,
                             ObjectMapper objectMapper) {
        this.client = client;
        this.configuration = configuration;
        this.objectMapper = objectMapper;
        this.refundsUriGenerator = refundsUriGenerator;
        this.baseUrl = configuration.getBaseUrl();
    }
    
    public abstract Response getSearchResponse(Account account, Map<String, String> queryParams);

    protected HalRepresentation.HalRepresentationBuilder decoratePagination(
            HalRepresentation.HalRepresentationBuilder halRepresentationBuilder, 
            ISearchRefundsPagination pagination) {
        try {
            halRepresentationBuilder
                    .addProperty("count", pagination.getCount())
                    .addProperty("total", pagination.getTotal())
                    .addProperty("page", pagination.getPage());
            addLink(halRepresentationBuilder, "payments", transformIntoPublicUri(baseUrl, pagination.getLinks().getPaymentPage()));
            addLink(halRepresentationBuilder, "self", transformIntoPublicUri(baseUrl, pagination.getLinks().getSelf()));
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

    private URI transformIntoPublicUri(String baseUrl, Link link) throws URISyntaxException {
        if (link == null)
            return null;

        return UriBuilder.fromUri(baseUrl)
                .path(REFUNDS_PATH)
                .replaceQuery(new URI(link.getHref()).getQuery())
                .build();
    }
}
