package uk.gov.pay.api.filter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import uk.gov.pay.logging.LoggingKeys;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RestClientLoggingFilterTest {

    private RestClientLoggingFilter loggingFilter;

    @Mock
    private ClientRequestContext clientRequestContext;

    @Mock
    private ClientResponseContext clientResponseContext;

    @Mock
    private Appender<ILoggingEvent> mockAppender;

    @Captor
    ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor;

    @BeforeEach
    public void setup() {
        loggingFilter = new RestClientLoggingFilter();
        Logger root = (Logger) LoggerFactory.getLogger(RestClientLoggingFilter.class);
        root.addAppender(mockAppender);
    }

    @Test
    public void shouldLogRestClientStartEventWithRequestId() {

        String requestId = UUID.randomUUID().toString();
        URI requestUrl = URI.create("/publicapi-request");
        String requestMethod = "GET";
        MultivaluedMap<String, Object> mockHeaders = new MultivaluedHashMap<>();

        when(clientRequestContext.getUri()).thenReturn(requestUrl);
        when(clientRequestContext.getMethod()).thenReturn(requestMethod);
        when(clientRequestContext.getHeaders()).thenReturn(mockHeaders);
        MDC.put(LoggingKeys.MDC_REQUEST_ID_KEY, requestId);

        loggingFilter.filter(clientRequestContext);

        verify(mockAppender, times(1)).doAppend(loggingEventArgumentCaptor.capture());
        List<LoggingEvent> loggingEvents = loggingEventArgumentCaptor.getAllValues();

        assertThat(loggingEvents.get(0).getFormattedMessage(), is(format("[%s] - %s to %s began", requestId, requestMethod, requestUrl)));

    }

    @Test
    public void shouldLogRestClientEndEventWithRequestIdAndElapsedTime() {

        String requestId = UUID.randomUUID().toString();
        URI requestUrl = URI.create("/publicapi-request");
        String requestMethod = "GET";

        when(clientRequestContext.getUri()).thenReturn(requestUrl);
        when(clientRequestContext.getMethod()).thenReturn(requestMethod);
        MultivaluedMap<String, Object> mockHeaders = new MultivaluedHashMap<>();
        MultivaluedMap<String, String> mockHeaders2 = new MultivaluedHashMap<>();

        when(clientRequestContext.getHeaders()).thenReturn(mockHeaders);
        when(clientResponseContext.getHeaders()).thenReturn(mockHeaders2);
        MDC.put(LoggingKeys.MDC_REQUEST_ID_KEY, requestId);
        loggingFilter.filter(clientRequestContext);

        loggingFilter.filter(clientRequestContext, clientResponseContext);

        verify(mockAppender, times(2)).doAppend(loggingEventArgumentCaptor.capture());
        List<LoggingEvent> loggingEvents = loggingEventArgumentCaptor.getAllValues();

        assertThat(loggingEvents.get(0).getFormattedMessage(), is(format("[%s] - %s to %s began", requestId, requestMethod, requestUrl)));
        String endLogMessage = loggingEvents.get(1).getFormattedMessage();
        assertThat(endLogMessage, containsString(format("[%s] - %s to %s ended - total time ", requestId, requestMethod, requestUrl)));
        String[] timeTaken = StringUtils.substringsBetween(endLogMessage, "total time ", "ms");
        assertTrue(NumberUtils.isCreatable(timeTaken[0]));

    }
}
