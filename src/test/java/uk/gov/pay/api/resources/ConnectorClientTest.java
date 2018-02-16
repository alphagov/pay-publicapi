package uk.gov.pay.api.resources;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.TokenPaymentType;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ConnectorClientTest {

    private static final String connectorUrl = "http://connector.local";
    private static final String connectorDDUrl = "http://connector.dd.local";


    private final HttpClient mockHttpClient = mock(HttpClient.class);

    private final ConnectorClient connectorClient = new ConnectorClient(mockHttpClient, connectorUrl, connectorDDUrl);


    @Test
    public void getPayment_shouldReturnAnExistingCharge() throws IOException {

        HttpResponse mockResponse = mock(HttpResponse.class);
        StatusLine mockStatusLine = mock(StatusLine.class);

        when(mockHttpClient.execute(argThat(aRequestWith(
                "http://connector.local/v1/api/accounts/my-account/charges/a1234",
                "GET"
        )))).thenReturn(mockResponse);
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(200);
        when(mockResponse.getEntity()).thenReturn(new StringEntity("{\"charge_id\":\"999999\"}"));

        Response response = connectorClient.getPayment("a1234", new Account("my-account", TokenPaymentType.CARD));

        assertThat(response.getStatus(), is(200));
        assertThat(response.getEntity(), is(notNullValue()));
    }

    @Test
    public void getPayment_shouldReturn404IfPaymentIsNotFound() throws IOException {

        HttpResponse mockResponse = mock(HttpResponse.class);
        StatusLine mockStatusLine = mock(StatusLine.class);

        when(mockHttpClient.execute(argThat(aRequestWith(
                "http://connector.local/v1/api/accounts/my-account/charges/a000000",
                "GET"
        )))).thenReturn(mockResponse);
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(404);

        Response response = connectorClient.getPayment("a000000", new Account("my-account", TokenPaymentType.CARD));

        assertThat(response.getStatus(), is(404));
    }

    @Ignore("WIP")
    @Test
    public void getPaymentEvents_shouldReturnAllEventsForAGivenPayment() throws IOException {

        HttpResponse mockResponse = mock(HttpResponse.class);
        StatusLine mockStatusLine = mock(StatusLine.class);

        when(mockHttpClient.execute(argThat(aRequestWith(
                "http://connector.local/v1/api/accounts/my-account/charges/a1234/events",
                "GET"
        )))).thenReturn(mockResponse);
        when(mockResponse.getStatusLine()).thenReturn(mockStatusLine);
        when(mockStatusLine.getStatusCode()).thenReturn(404);

        Response response = connectorClient.getPayment("a1234", new Account("my-account", TokenPaymentType.CARD));

        assertThat(response.getStatus(), is(404));
    }




    private ArgumentMatcher<HttpUriRequest> aRequestWith(String uri, String method) {
        return httpUriRequest -> httpUriRequest.getMethod().equals(method) &&
                httpUriRequest.getURI().toString().equals(uri);
    }

}
