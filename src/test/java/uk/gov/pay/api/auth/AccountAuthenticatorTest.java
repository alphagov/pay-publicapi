package uk.gov.pay.api.auth;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.EntityBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.ServiceUnavailableException;
import java.io.IOException;
import java.util.Optional;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.pay.api.model.TokenPaymentType.CARD;
import static uk.gov.pay.api.model.TokenPaymentType.DIRECT_DEBIT;

public class AccountAuthenticatorTest {

    private AccountAuthenticator accountAuthenticator;
    private HttpResponse mockResponse;

    private final String bearerToken = "aaa";
    private final String accountName = "accountName";


    @Before
    public void setup() throws IOException {

        HttpClient publicAuthMock = mock(HttpClient.class);

        mockResponse = mock(HttpResponse.class);

        accountAuthenticator = new AccountAuthenticator(publicAuthMock, "");

        when(publicAuthMock.execute(Matchers.any())).thenReturn(mockResponse);
    }


    @Test
    public void shouldReturnValidAccount() {

        HttpEntity httpEntity = EntityBuilder.create()
                .setText(String.format("{\"account_id\":\"%s\", \"token_type\": \"DIRECT_DEBIT\"}", accountName))
                .build();

        final StatusLine mockStatusLine = mock(StatusLine.class);

        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(OK.getStatusCode());
        when(mockResponse.getEntity()).thenReturn(httpEntity);

        Optional<Account> maybeAccount = accountAuthenticator.authenticate(bearerToken);

        assertThat(maybeAccount.get().getName(), is(accountName));
        assertThat(maybeAccount.get().getPaymentType(), is(DIRECT_DEBIT));
    }

    @Test
    public void shouldReturnCCAccount_ifTokenTypeIsMissing() {

        HttpEntity httpEntity = EntityBuilder.create()
                .setText(String.format("{\"account_id\":\"%s\"}", accountName))
                .build();

        final StatusLine mockStatusLine = mock(StatusLine.class);

        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(OK.getStatusCode());
        when(mockResponse.getEntity()).thenReturn(httpEntity);

        Optional<Account> maybeAccount = accountAuthenticator.authenticate(bearerToken);

        assertThat(maybeAccount.get().getName(), is(accountName));
        assertThat(maybeAccount.get().getPaymentType(), is(CARD));
    }

    @Test
    public void shouldNotReturnAccount_ifUnauthorised() {

        final StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(UNAUTHORIZED.getStatusCode());

        Optional<Account> maybeAccount = accountAuthenticator.authenticate(bearerToken);

        assertThat(maybeAccount.isPresent(), is(false));
    }

    @Test(expected = ServiceUnavailableException.class)
    public void shouldThrow_ifUnknownResponse() {

        final StatusLine mockStatusLine = mock(StatusLine.class);
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(NOT_FOUND.getStatusCode());

        accountAuthenticator.authenticate(bearerToken);
    }
}
