package uk.gov.pay.api.resources;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.service.GetPaymentService;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GetOnePaymentStrategyTest {
    
    @Mock
    private GetPaymentService mockGetPaymentService;

    @Captor
    ArgumentCaptor<LoggingEvent> loggingEventArgumentCaptor;

    private Appender<ILoggingEvent> mockAppender;
    
    private GetOnePaymentStrategy getOnePaymentStrategy;
    private String mockPaymentId = "some-payment-id";
    private Account mockAccountId = new Account("some-account-id", TokenPaymentType.CARD);

    @Before
    public void setUp() {
        ch.qos.logback.classic.Logger root = (Logger) LoggerFactory.getLogger(LedgerOrConnectorStrategyTemplate.class);
        mockAppender = mock(Appender.class);
        root.addAppender(mockAppender);
    }

    @Test
    public void whenNoStrategyProvidedUsesDefaultStrategy() {
        getOnePaymentStrategy = new GetOnePaymentStrategy(null, mockAccountId, mockPaymentId, mockGetPaymentService);
        
        getOnePaymentStrategy.validateAndExecute();

        verify(mockGetPaymentService, never()).getPayment(mockAccountId, mockPaymentId);
        verify(mockGetPaymentService).getConnectorCharge(mockAccountId, mockPaymentId);
    }

    @Test
    public void whenEmptyStrategyProvidedUsesDefaultStrategy() {
        getOnePaymentStrategy = new GetOnePaymentStrategy("", mockAccountId, mockPaymentId, mockGetPaymentService);

        getOnePaymentStrategy.validateAndExecute();

        verify(mockGetPaymentService, never()).getPayment(mockAccountId, mockPaymentId);
        verify(mockGetPaymentService).getConnectorCharge(mockAccountId, mockPaymentId);
    }

    @Test
    public void whenLedgerOnlyStrategyProvidedUsesLedgerStrategy() {
        getOnePaymentStrategy = new GetOnePaymentStrategy("ledger-only", mockAccountId, mockPaymentId, mockGetPaymentService);

        getOnePaymentStrategy.validateAndExecute();

        verify(mockGetPaymentService, never()).getPayment(mockAccountId, mockPaymentId);
        verify(mockGetPaymentService).getLedgerTransaction(mockAccountId, mockPaymentId);
    }

    @Test
    public void whenFutureBehaviourStrategyProvidedUsesFutureBehaviourStrategy() {
        getOnePaymentStrategy = new GetOnePaymentStrategy("future-behaviour", mockAccountId, mockPaymentId, mockGetPaymentService);

        getOnePaymentStrategy.validateAndExecute();

        verify(mockGetPaymentService).getPayment(mockAccountId, mockPaymentId);
    }

    @Test
    public void whenNotValidStrategyProvidedUsesDefaultStrategy() {
        getOnePaymentStrategy = new GetOnePaymentStrategy("not-valid-strategy-name", mockAccountId, mockPaymentId, mockGetPaymentService);

        getOnePaymentStrategy.validateAndExecute();

        verify(mockAppender, times(1)).doAppend(loggingEventArgumentCaptor.capture());
        List<LoggingEvent> loggingEvents = loggingEventArgumentCaptor.getAllValues();

        assertThat(loggingEvents.get(0).getFormattedMessage(),
                is("Not valid strategy (valid values are \"ledger-only\", \"future-behaviour\" or empty); using the default strategy"));

        verify(mockGetPaymentService, never()).getPayment(mockAccountId, mockPaymentId);
        verify(mockGetPaymentService).getConnectorCharge(mockAccountId, mockPaymentId);
    }
}
