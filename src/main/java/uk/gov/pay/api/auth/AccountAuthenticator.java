package uk.gov.pay.api.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.jackson.Jackson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.model.TokenPaymentType;

import javax.ws.rs.ServiceUnavailableException;
import java.io.IOException;
import java.util.Optional;

import static javax.ws.rs.core.HttpHeaders.ACCEPT;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;
import static uk.gov.pay.api.model.TokenPaymentType.fromString;

public class AccountAuthenticator implements Authenticator<String, Account> {

    private static Logger LOGGER = LoggerFactory.getLogger(AccountAuthenticator.class);

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    private final HttpClient client;
    private final String publicAuthUrl;


    public AccountAuthenticator(HttpClient client, String publicAuthUrl) {
        this.client = client;
        this.publicAuthUrl = publicAuthUrl;
    }


    @Override
    public Optional<Account> authenticate(String bearerToken) {

        try {

            HttpResponse response = client.execute(RequestBuilder
                    .get()
                    .setUri(publicAuthUrl)
                    .setHeader(ACCEPT, APPLICATION_JSON)
                    .setHeader(AUTHORIZATION, "Bearer " + bearerToken)
                    .build());

            if (response.getStatusLine().getStatusCode() == OK.getStatusCode()) {

                return getAccountFromResponse(response);

            } else if (response.getStatusLine().getStatusCode() == UNAUTHORIZED.getStatusCode()) {
                return Optional.empty();
            } else {
                LOGGER.warn("Unexpected status code " + response.getStatusLine().getStatusCode() + " from auth.");
                throw new ServiceUnavailableException();
            }

        } catch (IOException e) {
            throw new RuntimeException("There was an error trying to call public auth for authentication.", e);
        }
    }


    private Optional<Account> getAccountFromResponse(HttpResponse response) throws IOException {
        String json = EntityUtils.toString(response.getEntity());
        JsonNode responseEntity = MAPPER.readTree(json);
        String accountId = responseEntity.get("account_id").asText();
        String tokenType = Optional.ofNullable(responseEntity.get("token_type"))
                .map(JsonNode::asText).orElse(CARD.toString());
        TokenPaymentType tokenPaymentType = fromString(tokenType);
        return Optional.of(new Account(accountId, tokenPaymentType));
    }
}
