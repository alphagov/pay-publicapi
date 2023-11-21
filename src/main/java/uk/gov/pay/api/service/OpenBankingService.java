package uk.gov.pay.api.service;

import org.apache.http.HttpResponse;
import uk.gov.pay.api.app.config.TinkConfiguration;
import uk.gov.pay.api.model.tink.TinkTokenResponse;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;

public class OpenBankingService {

    private final Client client;
    
    private final TinkConfiguration tinkConfiguration;

    @Inject
    public OpenBankingService(Client client, TinkConfiguration tinkConfiguration) {
        this.client = client;
        this.tinkConfiguration = tinkConfiguration;
    }


    /*
    Access token expire every 30 minutes so a to-do here would be to 1) refresh the token every 25 minutes on an 
    internal scheduler OR 2) refresh the token based on the expiry "expires_in"
     */
    public String getAccessToken() {
        MultivaluedMap<String, String> map = new MultivaluedHashMap<>(4);
        map.add("client_id", tinkConfiguration.getClientId());
        map.add("client_secret", tinkConfiguration.getClientSecret());
        map.add("grant_type", "client_credentials");
        map.add("scope", "payment:read,payment:write");
        Response response = client.target("https://api.tink.com/api/v1/oauth/token")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.form(map));
        
        if (response.getStatus() == OK.getStatusCode()) {
            return response.readEntity(TinkTokenResponse.class).getAccessToken();
        } else {
            throw new RuntimeException("Response from getting a Tink token was not 200.");
        }
    }
}
