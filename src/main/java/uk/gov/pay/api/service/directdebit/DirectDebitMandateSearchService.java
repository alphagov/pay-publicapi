package uk.gov.pay.api.service.directdebit;

import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.search.directdebit.DirectDebitSearchMandateResponse;
import uk.gov.pay.api.model.search.directdebit.DirectDebitSearchMandatesParams;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class DirectDebitMandateSearchService {

    private final Client client;
    private final DirectDebitConnectorUriGenerator uriGenerator;
    
    
    @Inject
    public DirectDebitMandateSearchService(Client client, DirectDebitConnectorUriGenerator uriGenerator) {
        this.client = client;
        this.uriGenerator = uriGenerator;
    }
    
    public DirectDebitSearchMandateResponse searchMandates(Account account, DirectDebitSearchMandatesParams params) {
        Map<String, String> mappedParams = parseParamsFromParamsObject(params);
        WebTarget searchRequestBuilder = client
                .target(uriGenerator.mandatesURIWithParams(account, parseParamsFromParamsObject(params)));
        addQueryParamsToRequestFromMap(searchRequestBuilder, mappedParams);
        return searchRequestBuilder.request()
                .accept(APPLICATION_JSON)
                .get()
                .readEntity(DirectDebitSearchMandateResponse.class);
    }
    
    private void addQueryParamsToRequestFromMap(WebTarget targetObject, Map<String, String> map) {
        map.forEach(targetObject::queryParam);
    }
    
    private Map<String, String> parseParamsFromParamsObject(DirectDebitSearchMandatesParams params) {
        Map<String, String> returnableMap = new HashMap<>();
        params.getBankStatementReference().ifPresent(x -> returnableMap.put("bank_statement_reference", x));
        params.getEmail().ifPresent(x -> returnableMap.put("email", x));
        params.getName().ifPresent(x -> returnableMap.put("name", x));
        params.getFromDate().ifPresent(x -> returnableMap.put("from_date", String.valueOf(x)));
        params.getToDate().ifPresent(x -> returnableMap.put("to_date", String.valueOf(x)));
        params.getReference().ifPresent(x -> returnableMap.put("reference", x));
        params.getState().ifPresent(x -> returnableMap.put("state", x.getStatus()));
        returnableMap.put("display_size", String.valueOf(params.getDisplaySize()));
        returnableMap.put("page", String.valueOf(params.getPage()));
        return returnableMap;
    }
    
}
