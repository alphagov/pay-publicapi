package uk.gov.pay.api.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.AuthenticationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ServiceUnavailableException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.Response.Status.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.model.TokenPaymentType.CREDIT_CARD;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;

public class AccountAuthenticatorTest {

    private AccountAuthenticator accountAuthenticator;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Response mockResponse;

    private final String bearerToken = "aaa";
    private final String accountName = "accountName";

    @Before
    public void setup() {
        Client publicAuthMock = mock(Client.class);
        WebTarget mockTarget = mock(WebTarget.class);
        Invocation.Builder mockRequest = mock(Invocation.Builder.class);
        mockResponse = mock(Response.class);
        accountAuthenticator = new AccountAuthenticator(publicAuthMock, "");
        when(publicAuthMock.target("")).thenReturn(mockTarget);
        when(mockTarget.request()).thenReturn(mockRequest);
        when(mockRequest.header(AUTHORIZATION, "Bearer " + bearerToken)).thenReturn(mockRequest);
        when(mockRequest.accept(MediaType.APPLICATION_JSON)).thenReturn(mockRequest);
        when(mockRequest.get()).thenReturn(mockResponse);
    }

    @Test
    public void shouldReturnValidAccount() throws AuthenticationException {
        Map<String, String> responseEntity = ImmutableMap.of(
                "account_id", accountName,
                "token_type", "DIRECT_DEBIT"
        );
        JsonNode response = objectMapper.valueToTree(responseEntity);
        when(mockResponse.getStatus()).thenReturn(OK.getStatusCode());
        when(mockResponse.readEntity(JsonNode.class)).thenReturn(response);
        Optional<Account> maybeAccount = accountAuthenticator.authenticate(bearerToken);
        Assert.assertThat(maybeAccount.get().getName(), is(accountName));
        Assert.assertThat(maybeAccount.get().getPaymentType(), is(DIRECT_DEBIT));
    }

    @Test
    public void shouldReturnCCAccount_ifTokenTypeIsMissing() throws AuthenticationException {
        Map<String, String> responseEntity = ImmutableMap.of(
                "account_id", accountName
        );
        JsonNode response = objectMapper.valueToTree(responseEntity);
        when(mockResponse.getStatus()).thenReturn(OK.getStatusCode());
        when(mockResponse.readEntity(JsonNode.class)).thenReturn(response);
        Optional<Account> maybeAccount = accountAuthenticator.authenticate(bearerToken);
        Assert.assertThat(maybeAccount.get().getName(), is(accountName));
        Assert.assertThat(maybeAccount.get().getPaymentType(), is(CREDIT_CARD));
    }

    @Test
    public void shouldNotReturnAccount_ifUnauthorised() throws AuthenticationException {
        when(mockResponse.getStatus()).thenReturn(UNAUTHORIZED.getStatusCode());
        Optional<Account> maybeAccount = accountAuthenticator.authenticate(bearerToken);
        Assert.assertThat(maybeAccount.isPresent(), is(false));
    }

    @Test(expected = ServiceUnavailableException.class)
    public void shouldThrow_ifUnknownResponse() throws AuthenticationException {
        when(mockResponse.getStatus()).thenReturn(NOT_FOUND.getStatusCode());
        accountAuthenticator.authenticate(bearerToken);
    }
}