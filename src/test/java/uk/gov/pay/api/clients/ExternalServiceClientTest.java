package uk.gov.pay.api.clients;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.MDC;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExternalServiceClientTest {

    private ExternalServiceClient externalServiceClient;

    @Mock
    private Client client;
    @Mock
    private WebTarget webTarget;
    @Mock
    private Invocation.Builder builder;
    @Captor
    private ArgumentCaptor<String> headerNameArgumentCaptor;
    @Captor
    private ArgumentCaptor<Object> headerValueArgumentCaptor;

    @Before
    public void setUp() {
        externalServiceClient = new ExternalServiceClient(client);
        when(client.target(anyString())).thenReturn(webTarget);
        when(webTarget.request()).thenReturn(builder);
        when(builder.header(anyString(), any())).thenReturn(builder);
        when(builder.accept(MediaType.APPLICATION_JSON)).thenReturn(builder);
    }
    
    @Test
    public void assert_x_request_id_in_request_header_for_get() {
        MDC.put("x_request_id", "abc");
        externalServiceClient.get("http://example123.com");
        
        verify(builder).header(headerNameArgumentCaptor.capture(), headerValueArgumentCaptor.capture());
        assertThat(headerValueArgumentCaptor.getValue()).isEqualTo("abc");
        assertThat(headerNameArgumentCaptor.getValue()).isEqualTo("X-Request-Id");
    }
    
    @Test
    public void assert_x_request_id_in_request_header_for_post() {
        MDC.put("x_request_id", "123-easy-as-abc");
        externalServiceClient.post("http://example123.com");

        verify(builder).header(headerNameArgumentCaptor.capture(), headerValueArgumentCaptor.capture());
        assertThat(headerValueArgumentCaptor.getValue()).isEqualTo("123-easy-as-abc");
        assertThat(headerNameArgumentCaptor.getValue()).isEqualTo("X-Request-Id");
    }
    
    @Test
    public void assert_empty_headers_in_post_request_when_mdc_is_empty() {
        MDC.clear();
        externalServiceClient.post("http://example123.com");
        verify(builder, never()).header(anyString(), any());
    }

    @Test
    public void assert_empty_headers_in_get_request_when_mdc_is_empty() {
        MDC.clear();
        externalServiceClient.get("http://example123.com");
        verify(builder, never()).header(anyString(), any());
    }
}
