package uk.gov.pay.api.exception;

import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ConnectorResponseErrorExceptionTest {

    @Test
    public void whenCreated_shouldCallCloseConnection_soWeMakeSureThatTheConnectionIsClosedWhenConnectorRespondsWithAnUnexpectedResponse() {
        // It doesn't matter if the connection was closed before,
        // but anyway we make sure it is when an exception of this nature is raised

        Response mockResponse = mock(Response.class);
        when(mockResponse.getStatus())
                .thenReturn(400);

        ConnectorResponseErrorException exception = new ConnectorResponseErrorException(mockResponse);

        assertThat(exception.getErrorStatus(), is(400));

        verify(mockResponse).getStatus();
        verify(mockResponse).close();
        verifyNoMoreInteractions(mockResponse);
    }
}
