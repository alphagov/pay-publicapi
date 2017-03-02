package uk.gov.pay.api.auth;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

public class AccountAuthenticator implements Authenticator<String, Account> {
    private static Logger logger = LoggerFactory.getLogger(AccountAuthenticator.class);

    private final Client client;
    private final String publicAuthUrl;

    public AccountAuthenticator(Client client, String publicAuthUrl) {
        this.client = client;
        this.publicAuthUrl = publicAuthUrl;
    }

    @Override
    public Optional<Account> authenticate(String bearerToken) throws AuthenticationException {
        Response response = client.target(publicAuthUrl).request()
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .accept(MediaType.APPLICATION_JSON)
                .get();
        if (response.getStatus() == OK.getStatusCode()) {
            String accountId = response.readEntity(JsonNode.class).get("account_id").asText();
            return Optional.of(new Account(accountId));
        } else if (response.getStatus() == UNAUTHORIZED.getStatusCode()) {
            response.close();
            return Optional.empty();
        } else {
            response.close();
            logger.warn("Unexpected status code " + response.getStatus() + " from auth.");
            throw new ServiceUnavailableException();
        }
    }
}
