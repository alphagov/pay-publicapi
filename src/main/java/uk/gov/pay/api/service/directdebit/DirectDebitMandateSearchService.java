package uk.gov.pay.api.service.directdebit;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.directdebit.mandates.MandateResponse;
import uk.gov.pay.api.model.search.directdebit.DirectDebitSearchMandatesParams;
import uk.gov.pay.api.model.search.directdebit.SearchMandateConnectorResponse;
import uk.gov.pay.api.model.search.directdebit.SearchMandateResponse;
import uk.gov.pay.api.service.PublicApiUriGenerator;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
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
                .withLinks(connectorResponse.getLinks())
                .withMandates(mandateResponse)
                .build();
    }
    
    SearchMandateConnectorResponse getMandatesFromDDConnector(Account account, DirectDebitSearchMandatesParams params) {
        WebTarget webTargetWithoutQuery = client.target(directDebitConnectorUriGenerator.mandatesURI(account));
        WebTarget webTargetWithQuery = addQueryParams(params.paramsAsMap(), webTargetWithoutQuery);

        return webTargetWithQuery
                .request()
                .accept(APPLICATION_JSON)
                .get()
                .readEntity(SearchMandateConnectorResponse.class);
    }

    private WebTarget addQueryParams(Map<String, String> params, WebTarget originalWebTarget) {
        WebTarget webTargetWithQueryParams = originalWebTarget;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            webTargetWithQueryParams = webTargetWithQueryParams.queryParam(entry.getKey(), entry.getValue());
        }
        return webTargetWithQueryParams;
    }
}
