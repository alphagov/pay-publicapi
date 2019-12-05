package uk.gov.pay.api.resources;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import uk.gov.pay.api.auth.Account;
import uk.gov.pay.api.model.TokenPaymentType;
import uk.gov.pay.api.service.GetPaymentEventsService;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(JUnitParamsRunner.class)
public class GetPaymentEventsStrategyTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private GetPaymentEventsService getPaymentEventsService;

    private GetPaymentEventsStrategy getPaymentEventsStrategy;
    private String mockPaymentId = "some-payment-id";
    private Account mockAccountId = new Account("some-account-id", TokenPaymentType.CARD);

    @Test
    public void validateAndExecuteUsesLedgerOnlyStrategy() {
        String strategy = "ledger-only";
        getPaymentEventsStrategy = new GetPaymentEventsStrategy(strategy, mockAccountId, mockPaymentId, getPaymentEventsService);
        getPaymentEventsStrategy.validateAndExecute();

        verify(getPaymentEventsService).getPaymentEventsFromLedger(mockAccountId, mockPaymentId);
        verify(getPaymentEventsService, never()).getPaymentEventsFromConnector(mockAccountId, mockPaymentId);
        verify(getPaymentEventsService, never()).getPaymentEvents(mockAccountId, mockPaymentId);
    }

    @Test
    @Parameters({"", "unknown"})
    public void validateAndExecuteUsesDefaultStrategy(String strategy) {
        getPaymentEventsStrategy = new GetPaymentEventsStrategy(strategy, mockAccountId, mockPaymentId, getPaymentEventsService);
        getPaymentEventsStrategy.validateAndExecute();

        verify(getPaymentEventsService).getPaymentEvents(mockAccountId, mockPaymentId);
    }

    @Test
    
    public void validateAndExecuteShouldUseConnectorOnlyStrategy() {
        String strategy = "connector-only";
        getPaymentEventsStrategy = new GetPaymentEventsStrategy(strategy, mockAccountId, mockPaymentId, getPaymentEventsService);

        getPaymentEventsStrategy.validateAndExecute();

        verify(getPaymentEventsService).getPaymentEventsFromConnector(mockAccountId, mockPaymentId);
        verify(getPaymentEventsService, never()).getPaymentEventsFromLedger(mockAccountId, mockPaymentId);
        verify(getPaymentEventsService, never()).getPaymentEvents(mockAccountId, mockPaymentId);
    }
}
