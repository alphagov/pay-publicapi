package uk.gov.pay.api.service.directdebit;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.search.directdebit.DirectDebitSearchMandatesParams;
import uk.gov.pay.api.model.search.directdebit.SearchMandateConnectorResponse;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class DirectDebitMandateSearchService {

    private final Client client;
    private final DirectDebitConnectorUriGenerator directDebitConnectorUriGenerator;
    
    
    @Inject
    public DirectDebitMandateSearchService(Client client, DirectDebitConnectorUriGenerator directDebitConnectorUriGenerator) {
        this.client = client;
        this.directDebitConnectorUriGenerator = directDebitConnectorUriGenerator;
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
