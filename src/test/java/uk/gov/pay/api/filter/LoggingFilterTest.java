package uk.gov.pay.api.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.pay.api.filter.LoggingFilter.HEADER_REQUEST_ID;

@RunWith(MockitoJUnitRunner.class)
public class LoggingFilterTest {

    private LoggingFilter loggingFilter;

    @Mock
    HttpServletRequest mockRequest;

    @Mock
    HttpServletResponse mockResponse;

    @Mock
    FilterChain mockFilterChain;

    private Appender<ILoggingEvent> mockAppender;

    @Captor
    ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor;

    @Before
    public void setup() {
        loggingFilter = new LoggingFilter();
        Logger root = (Logger) LoggerFactory.getLogger(LoggingFilter.class);
        mockAppender = mockAppender();
        root.addAppender(mockAppender);
    }

    @Test
    public void shouldLogEntryAndExitPointsOfEndPoints() throws Exception {

        String requestUrl = "/publicapi-request";
        String requestId = UUID.randomUUID().toString();
        String requestMethod = "GET";

        when(mockRequest.getRequestURI()).thenReturn(requestUrl);
        when(mockRequest.getMethod()).thenReturn(requestMethod);
        when(mockRequest.getHeader(HEADER_REQUEST_ID)).thenReturn(requestId);

        loggingFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockAppender, times(2)).doAppend(loggingEventArgumentCaptor.capture());
        List<LoggingEvent> loggingEvents = loggingEventArgumentCaptor.getAllValues();

        assertThat(loggingEvents.get(0).getFormattedMessage(), is(format("[%s] - %s to %s began", requestId, requestMethod, requestUrl)));
        String endLogMessage = loggingEvents.get(1).getFormattedMessage();
        assertThat(endLogMessage, containsString(format("[%s] - %s to %s ended - total time ", requestId, requestMethod, requestUrl)));
        String[] timeTaken = StringUtils.substringsBetween(endLogMessage, "total time ", "ms");
        assertTrue(NumberUtils.isNumber(timeTaken[0]));
        verify(mockFilterChain).doFilter(mockRequest, mockResponse);

    }

    @Test
    public void shouldLogEntryAndExitPointsEvenIfRequestIdDoesNotExist() throws Exception {

        String requestUrl = "/publicapi-request";
        String requestMethod = "GET";

        when(mockRequest.getRequestURI()).thenReturn(requestUrl);
        when(mockRequest.getMethod()).thenReturn(requestMethod);

        loggingFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockAppender, times(2)).doAppend(loggingEventArgumentCaptor.capture());
        List<LoggingEvent> loggingEvents = loggingEventArgumentCaptor.getAllValues();

        assertThat(loggingEvents.get(0).getFormattedMessage(), is(format("[%s] - %s to %s began", "", requestMethod, requestUrl)));
        String endLogMessage = loggingEvents.get(1).getFormattedMessage();
        assertThat(endLogMessage, containsString(format("[%s] - %s to %s ended - total time ", "", requestMethod, requestUrl)));
        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
    }

    @Test
    public void shouldLogEntryAndExitPointsEvenWhenFilterChainingThrowsException() throws Exception {
        String requestUrl = "/publicapi-url-with-exception";
        String requestMethod = "GET";
        String requestId = UUID.randomUUID().toString();

        when(mockRequest.getRequestURI()).thenReturn(requestUrl);
        when(mockRequest.getMethod()).thenReturn(requestMethod);
        when(mockRequest.getHeader(HEADER_REQUEST_ID)).thenReturn(requestId);

        IOException exception = new IOException("Failed request");
        doThrow(exception).when(mockFilterChain).doFilter(mockRequest, mockResponse);

        loggingFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        verify(mockAppender, times(3)).doAppend(loggingEventArgumentCaptor.capture());
        List<LoggingEvent> loggingEvents = loggingEventArgumentCaptor.getAllValues();

        assertThat(loggingEvents.get(0).getFormattedMessage(), is(format("[%s] - %s to %s began", requestId, requestMethod, requestUrl)));
        assertThat(loggingEvents.get(1).getFormattedMessage(), is("Exception - publicapi request - "+ requestUrl + " - exception - "+ exception.getMessage()));
        assertThat(loggingEvents.get(1).getLevel(), is(Level.ERROR));
        assertThat(loggingEvents.get(1).getThrowableProxy().getMessage(), is("Failed request"));
        String endLogMessage = loggingEvents.get(2).getFormattedMessage();
        assertThat(endLogMessage, containsString(format("[%s] - %s to %s ended - total time ", requestId, requestMethod, requestUrl)));
        String[] timeTaken = StringUtils.substringsBetween(endLogMessage, "total time ", "ms");
        assertTrue(NumberUtils.isNumber(timeTaken[0]));
    }

    @SuppressWarnings("unchecked")
    private <T> Appender<T> mockAppender() {
        return mock(Appender.class);
    }

}
