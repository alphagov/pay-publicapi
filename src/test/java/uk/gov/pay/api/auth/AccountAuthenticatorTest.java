package uk.gov.pay.api.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.pay.api.app.config.PublicApiConfig;

import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;

public class AccountAuthenticatorTest {

    private AccountAuthenticator accountAuthenticator;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Response mockResponse;

    private final String bearerToken = "aaa";
    private final String accountId = "accountId";

    @BeforeEach
    public void setup() {
        Client publicAuthMock = mock(Client.class);
        WebTarget mockTarget = mock(WebTarget.class);
        Invocation.Builder mockRequest = mock(Invocation.Builder.class);
        mockResponse = mock(Response.class);
        PublicApiConfig mockConfiguration = mock(PublicApiConfig.class);
        when(mockConfiguration.getPublicAuthUrl()).thenReturn("");
        accountAuthenticator = new AccountAuthenticator(publicAuthMock, mockConfiguration);
        when(publicAuthMock.target("")).thenReturn(mockTarget);
        when(mockTarget.request()).thenReturn(mockRequest);
        when(mockRequest.header(AUTHORIZATION, "Bearer " + bearerToken)).thenReturn(mockRequest);
        when(mockRequest.accept(MediaType.APPLICATION_JSON)).thenReturn(mockRequest);
        when(mockRequest.get()).thenReturn(mockResponse);
    }

    @Test
    public void shouldReturnValidAccount() {
        Map<String, String> responseEntity = ImmutableMap.of(
                "account_id", accountId,
                "token_type", "DIRECT_DEBIT"
        );
        JsonNode response = objectMapper.valueToTree(responseEntity);
        when(mockResponse.getStatus()).thenReturn(OK.getStatusCode());
        when(mockResponse.readEntity(JsonNode.class)).thenReturn(response);
        Optional<Account> maybeAccount = accountAuthenticator.authenticate(bearerToken);
        assertThat(maybeAccount.get().getName(), is(accountId));
        assertThat(maybeAccount.get().getAccountId(), is(accountId));
        assertThat(maybeAccount.get().getPaymentType(), is(DIRECT_DEBIT));
    }

    @Test
    public void shouldReturnCCAccount_ifTokenTypeIsMissing() {
        Map<String, String> responseEntity = ImmutableMap.of(
                "account_id", accountId
        );
        JsonNode response = objectMapper.valueToTree(responseEntity);
        when(mockResponse.getStatus()).thenReturn(OK.getStatusCode());
        when(mockResponse.readEntity(JsonNode.class)).thenReturn(response);
        Optional<Account> maybeAccount = accountAuthenticator.authenticate(bearerToken);
        assertThat(maybeAccount.get().getName(), is(accountId));
        assertThat(maybeAccount.get().getPaymentType(), is(CARD));
    }

    @Test
    public void shouldNotReturnAccount_ifUnauthorised() {
        when(mockResponse.getStatus()).thenReturn(UNAUTHORIZED.getStatusCode());
        Optional<Account> maybeAccount = accountAuthenticator.authenticate(bearerToken);
        assertThat(maybeAccount.isPresent(), is(false));
    }

    @Test
    public void shouldThrow_ifUnknownResponse() {
        when(mockResponse.getStatus()).thenReturn(NOT_FOUND.getStatusCode());
        assertThrows(ServiceUnavailableException.class, () -> accountAuthenticator.authenticate(bearerToken));
    }
}
