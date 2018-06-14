package uk.gov.pay.api.auth;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.auth.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.app.config.PublicApiConfig;
import uk.gov.pay.api.model.TokenPaymentType;

import javax.inject.Inject;
import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;
import static uk.gov.pay.api.model.TokenPaymentType.fromString;

public class AccountAuthenticator implements Authenticator<String, Account> {
    private static Logger logger = LoggerFactory.getLogger(AccountAuthenticator.class);

    private final Client client;
    private final String publicAuthUrl;
    
    @Inject
    public AccountAuthenticator(Client client, PublicApiConfig configuration) {
        this.client = client;
        this.publicAuthUrl = configuration.getPublicAuthUrl();
    }

    @Override
    public Optional<Account> authenticate(String bearerToken) {

        Response response = client.target(publicAuthUrl).request()
                .header(AUTHORIZATION, "Bearer " + bearerToken)
                .accept(MediaType.APPLICATION_JSON)
                .get();

        if (response.getStatus() == OK.getStatusCode()) {
            return readAccountFromResponse(response);
        } else if (response.getStatus() == UNAUTHORIZED.getStatusCode()) {
            response.close();
            return Optional.empty();
        } else {
            response.close();
            logger.warn("Unexpected status code " + response.getStatus() + " from auth.");
            throw new ServiceUnavailableException();
        }
    }

    private Optional<Account> readAccountFromResponse(Response response) {
        JsonNode responseEntity = response.readEntity(JsonNode.class);
        String accountId = responseEntity.get("account_id").asText();
        String tokenType = Optional.ofNullable(responseEntity.get("token_type"))
                .map(JsonNode::asText).orElse(CARD.toString());
        TokenPaymentType tokenPaymentType = fromString(tokenType);
        return Optional.of(new Account(accountId, tokenPaymentType));
    }
}
