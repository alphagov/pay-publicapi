package uk.gov.pay.api.resources;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.service.GetPaymentEventsService;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GetPaymentEventsStrategyTest {

    @Mock
    private GetPaymentEventsService getPaymentEventsService;

    @Captor
    ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor;

    private Appender<ILoggingEvent> mockAppender;

    private GetPaymentEventsStrategy getPaymentEventsStrategy;
    private String mockPaymentId = "some-payment-id";
    private Account mockAccountId = new Account("some-account-id", TokenPaymentType.CARD);

    @Before
    public void setUp() {
        Logger root = (Logger) LoggerFactory.getLogger(LedgerOrConnectorStrategyTemplate.class);
        mockAppender = mock(Appender.class);
        root.addAppender(mockAppender);
    }

    @Test
    public void whenNoStrategyProvidedUsesDefaultStrategy() {
        getPaymentEventsStrategy = new GetPaymentEventsStrategy(null, mockAccountId, mockPaymentId, getPaymentEventsService);

        getPaymentEventsStrategy.validateAndExecute();

        verify(getPaymentEventsService, never()).getPaymentEventsFromLedger(mockAccountId, mockPaymentId);
        verify(getPaymentEventsService).getPaymentEventsFromConnector(mockAccountId, mockPaymentId);
    }

    @Test
    public void whenEmptyStrategyProvidedUsesDefaultStrategy() {
        getPaymentEventsStrategy = new GetPaymentEventsStrategy("", mockAccountId, mockPaymentId, getPaymentEventsService);

        getPaymentEventsStrategy.validateAndExecute();

        verify(getPaymentEventsService, never()).getPaymentEventsFromLedger(mockAccountId, mockPaymentId);
        verify(getPaymentEventsService).getPaymentEventsFromConnector(mockAccountId, mockPaymentId);
    }

    @Test
    public void whenLedgerOnlyStrategyProvidedUsesLedgerStrategy() {
        getPaymentEventsStrategy = new GetPaymentEventsStrategy("ledger-only", mockAccountId, mockPaymentId, getPaymentEventsService);

        getPaymentEventsStrategy.validateAndExecute();

        verify(getPaymentEventsService).getPaymentEventsFromLedger(mockAccountId, mockPaymentId);
        verify(getPaymentEventsService, never()).getPaymentEventsFromConnector(mockAccountId, mockPaymentId);
    }

    @Test
    public void whenFutureBehaviourStrategyProvidedUsesFutureBehaviourStrategy() {
        getPaymentEventsStrategy = new GetPaymentEventsStrategy("future-behaviour", mockAccountId, mockPaymentId, getPaymentEventsService);

        getPaymentEventsStrategy.validateAndExecute();

        verify(getPaymentEventsService).getPaymentEvents(mockAccountId, mockPaymentId);
    }

    @Test
    public void whenNotValidStrategyProvidedUsesDefaultStrategy() {
        getPaymentEventsStrategy = new GetPaymentEventsStrategy("not-valid-strategy-name", mockAccountId, mockPaymentId, getPaymentEventsService);

        getPaymentEventsStrategy.validateAndExecute();

        verify(mockAppender, times(1)).doAppend(loggingEventArgumentCaptor.capture());
        List<LoggingEvent> loggingEvents = loggingEventArgumentCaptor.getAllValues();

        assertThat(loggingEvents.get(0).getFormattedMessage(),
                is("Not valid strategy (valid values are \"ledger-only\", \"future-behaviour\" or empty); using the default strategy"));

        verify(getPaymentEventsService, never()).getPaymentEventsFromLedger(mockAccountId, mockPaymentId);
        verify(getPaymentEventsService).getPaymentEventsFromConnector(mockAccountId, mockPaymentId);
    }
}
